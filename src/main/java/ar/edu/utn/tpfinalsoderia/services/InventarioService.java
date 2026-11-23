package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.MovimientoEnvaseDAO;
import ar.edu.utn.tpfinalsoderia.DAO.StockEnvaseDAO;
import ar.edu.utn.tpfinalsoderia.DAO.StockProductoDAO;
import ar.edu.utn.tpfinalsoderia.enums.TipoMovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.models.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InventarioService {

    private final Connection conexion;
    private final StockEnvaseDAO stockEnvaseDAO;
    private final StockProductoDAO stockProductoDAO;
    private final MovimientoEnvaseDAO movimientoEnvaseDAO;

    public InventarioService(Connection conexion,
                             StockEnvaseDAO stockEnvaseDAO,
                             StockProductoDAO stockProductoDAO,
                             MovimientoEnvaseDAO movimientoEnvaseDAO) {
        this.conexion = conexion;
        this.stockEnvaseDAO = stockEnvaseDAO;
        this.stockProductoDAO = stockProductoDAO;
        this.movimientoEnvaseDAO = movimientoEnvaseDAO;
    }

    /**
     * Registra la recepción de envases vacíos (ingresan como SUCIOS) dentro de una transacción.
     */
    public void registrarRecepcionVacios(Cliente titular, Envase envase, int cantidad) throws ReglaNegocioException {
        validarCantidadPositiva(cantidad);
        Cliente titularValidado = validarYCompletarTitular(titular);
        Envase envaseValidado = validarYCompletarEnvase(envase);

        try {
            ejecutarEnTransaccion(() -> {
                String clienteId = (titularValidado != null) ? titularValidado.getId() : null;
                StockEnvase stock = stockEnvaseDAO.buscarPorTitularYEnvase(clienteId, envaseValidado.getId());

                if (stock == null) {
                    stock = new StockEnvase(UUID.randomUUID().toString(), titularValidado, envaseValidado, 0, cantidad);
                    stockEnvaseDAO.guardar(stock);
                } else {
                    stock.acreditarVacios(cantidad);
                    stockEnvaseDAO.actualizar(stock);
                }

                registrarMovimiento(titularValidado, envaseValidado, TipoMovimientoEnvase.INGRESO, cantidad);
            });
        } catch (StockInsuficienteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pasa envases de estado SUCIO a APTO tras el lavado y sanitizado dentro de una transacción.
     */
    public void registrarLavado(Cliente titular, Envase envase, int cantidad)
            throws ReglaNegocioException, StockInsuficienteException {
        validarCantidadPositiva(cantidad);
        Cliente titularValidado = validarYCompletarTitular(titular);
        Envase envaseValidado = validarYCompletarEnvase(envase);

        ejecutarEnTransaccion(() -> {
            String clienteId = (titularValidado != null) ? titularValidado.getId() : null;
            StockEnvase stock = stockEnvaseDAO.buscarPorTitularYEnvase(clienteId, envaseValidado.getId());

            if (stock == null || stock.getSucios() < cantidad) {
                int disponibles = (stock == null) ? 0 : stock.getSucios();
                throw new StockInsuficienteException("No hay suficientes envases sucios para lavar. Sucios disponibles: " + disponibles);
            }

            stock.setSucios(stock.getSucios() - cantidad);
            stock.setAptos(stock.getAptos() + cantidad);
            stockEnvaseDAO.actualizar(stock);

            registrarMovimiento(titularValidado, envaseValidado, TipoMovimientoEnvase.LAVADO, cantidad);
        });
    }

    /**
     * Consume envases APTOS y genera stock de producto lleno (Presentacion) dentro de una transacción.
     * Obtiene el envase desde la presentación persistida y valida titularidad de la marca.
     */
    public void registrarProduccion(Cliente titular, Presentacion presentacion, int cantidad)
            throws ReglaNegocioException, StockInsuficienteException {
        validarCantidadPositiva(cantidad);
        Cliente titularValidado = validarYCompletarTitular(titular);

        if (presentacion == null || presentacion.getId() == null || presentacion.getId().isBlank()) {
            throw new ReglaNegocioException("Debe seleccionar una presentación válida.");
        }

        Presentacion presCompleta = stockProductoDAO.buscarPresentacionCompleta(presentacion.getId());
        if (presCompleta == null) {
            throw new ReglaNegocioException("La presentación indicada no existe en la base de datos.");
        }
        if (!presCompleta.isActiva()) {
            throw new ReglaNegocioException("La presentación '" + presCompleta.getNombre() + "' se encuentra inactiva.");
        }

        Envase envase = presCompleta.getEnvase();
        if (envase == null || envase.getId() == null) {
            throw new ReglaNegocioException("La presentación no tiene un envase válido asociado.");
        }

        validarTitularidadMarca(titularValidado, presCompleta.getMarca());

        ejecutarEnTransaccion(() -> {
            String clienteId = (titularValidado != null) ? titularValidado.getId() : null;
            StockEnvase stockEnvase = stockEnvaseDAO.buscarPorTitularYEnvase(clienteId, envase.getId());

            if (stockEnvase == null) {
                throw new StockInsuficienteException("No existe stock de envases aptos para " + envase.getDescripcion());
            }

            stockEnvase.usarAptos(cantidad);
            stockEnvaseDAO.actualizar(stockEnvase);

            StockProducto stockProd = stockProductoDAO.buscarPorTitularYPresentacion(clienteId, presCompleta.getId());
            if (stockProd == null) {
                stockProd = new StockProducto(UUID.randomUUID().toString(), titularValidado, presCompleta, cantidad);
                stockProductoDAO.guardar(stockProd);
            } else {
                stockProd.sumarProduccion(cantidad);
                stockProductoDAO.actualizar(stockProd);
            }

            registrarMovimiento(titularValidado, envase, TipoMovimientoEnvase.PRODUCCION, cantidad);
        });
    }

    public List<StockEnvase> listarStockEnvases() {
        return stockEnvaseDAO.listar();
    }

    public List<StockProducto> listarStockProductos() {
        return stockProductoDAO.listar();
    }

    public List<MovimientoEnvase> listarMovimientosEnvases() {
        return movimientoEnvaseDAO.listar();
    }

    private Cliente validarYCompletarTitular(Cliente titular) throws ReglaNegocioException {
        if (titular == null || titular.getId() == null || titular.getId().isBlank()) {
            return null; // Producción o stock propio de la fábrica
        }
        Cliente encontrado = stockEnvaseDAO.buscarClientePorId(titular.getId());
        if (encontrado == null) {
            throw new ReglaNegocioException("El cliente fasón con ID '" + titular.getId() + "' no existe.");
        }
        if (!encontrado.isActivo()) {
            throw new ReglaNegocioException("El cliente '" + encontrado.getNombre() + "' se encuentra inactivo.");
        }
        return encontrado;
    }

    private Envase validarYCompletarEnvase(Envase envase) throws ReglaNegocioException {
        if (envase == null || envase.getId() == null || envase.getId().isBlank()) {
            throw new ReglaNegocioException("Debe seleccionar un envase válido.");
        }
        Envase encontrado = stockEnvaseDAO.buscarEnvasePorId(envase.getId());
        if (encontrado == null) {
            throw new ReglaNegocioException("El envase con ID '" + envase.getId() + "' no existe.");
        }
        return encontrado;
    }

    private void validarTitularidadMarca(Cliente titularOperacion, Marca marca) throws ReglaNegocioException {
        if (marca == null) {
            return;
        }
        if (marca.isEsPropiaFabrica()) {
            if (titularOperacion != null) {
                throw new ReglaNegocioException(
                        "La marca '" + marca.getNombre() + "' es propia de la fábrica; no puede producirse a nombre de un cliente fasón."
                );
            }
        } else {
            if (titularOperacion == null) {
                throw new ReglaNegocioException(
                        "La marca '" + marca.getNombre() + "' es de fasón; debe seleccionar el cliente titular."
                );
            }
            if (marca.getTitular() != null && marca.getTitular().getId() != null
                    && !Objects.equals(marca.getTitular().getId(), titularOperacion.getId())) {
                throw new ReglaNegocioException(
                        "La marca '" + marca.getNombre() + "' pertenece a otro cliente titular."
                );
            }
        }
    }

    private void registrarMovimiento(Cliente titular, Envase envase, TipoMovimientoEnvase tipo, int cantidad) {
        MovimientoEnvase mov = new MovimientoEnvase(
                UUID.randomUUID().toString(),
                titular,
                envase,
                tipo,
                cantidad,
                LocalDateTime.now()
        );
        movimientoEnvaseDAO.guardar(mov);
    }

    private void validarCantidadPositiva(int cantidad) throws ReglaNegocioException {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a cero.");
        }
    }

    @FunctionalInterface
    private interface OperacionTransaccional {
        void ejecutar() throws ReglaNegocioException, StockInsuficienteException;
    }

    private void ejecutarEnTransaccion(OperacionTransaccional operacion)
            throws ReglaNegocioException, StockInsuficienteException {
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            operacion.ejecutar();

            conexion.commit();
        } catch (ReglaNegocioException | StockInsuficienteException e) {
            hacerRollback();
            throw e;
        } catch (RuntimeException e) {
            hacerRollback();
            throw e;
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error en la transacción de inventario", e);
        } finally {
            try {
                conexion.setAutoCommit(autoCommitOriginal);
            } catch (SQLException ignored) {
            }
        }
    }

    private void hacerRollback() {
        try {
            conexion.rollback();
        } catch (SQLException ignored) {
        }
    }
}