package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.enums.TipoMovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.interfaces.Retornable;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.Marca;
import ar.edu.utn.tpfinalsoderia.models.MovimientoCajon;
import ar.edu.utn.tpfinalsoderia.models.MovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.StockCajon;
import ar.edu.utn.tpfinalsoderia.models.StockEnvase;
import ar.edu.utn.tpfinalsoderia.models.StockProducto;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ServicioInventario {

    private final Connection conexion;
    private final InventarioRepositoryJdbc inventarioRepository;

    public ServicioInventario(Connection conexion, InventarioRepositoryJdbc inventarioRepository) {
        this.conexion = Objects.requireNonNull(conexion, "La conexión no puede ser nula.");
        this.inventarioRepository = Objects.requireNonNull(inventarioRepository, "El repositorio de inventario no puede ser nulo.");
    }

    public void recibirVacios(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, PersistenciaException {
        validarCantidadPositiva(cantidad);
        validarEnvase(envase);

        try {
            conexion.setAutoCommit(false);

            StockEnvase stock = obtenerOCrearStockEnvase(cliente, envase);
            stock.setSucios(stock.getSucios() + cantidad);
            inventarioRepository.guardarStockEnvase(stock);

            inventarioRepository.registrarMovimientoEnvase(new MovimientoEnvase(
                    UUID.randomUUID().toString(),
                    cliente,
                    envase,
                    TipoMovimientoEnvase.INGRESO,
                    cantidad,
                    LocalDateTime.now()
            ));

            conexion.commit();
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error al registrar la recepción de envases vacíos.", e);
        } finally {
            restaurarAutoCommit();
        }
    }

    public void registrarRecepcionVacios(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, PersistenciaException {
        recibirVacios(cliente, envase, cantidad);
    }

    public void registrarDevolucionEnvases(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, PersistenciaException {
        validarCantidadPositiva(cantidad);
        validarEnvase(envase);

        try {
            conexion.setAutoCommit(false);

            StockEnvase stock = obtenerOCrearStockEnvase(cliente, envase);
            stock.setSucios(stock.getSucios() + cantidad);
            inventarioRepository.guardarStockEnvase(stock);

            inventarioRepository.registrarMovimientoEnvase(new MovimientoEnvase(
                    UUID.randomUUID().toString(),
                    cliente,
                    envase,
                    TipoMovimientoEnvase.RETORNO,
                    cantidad,
                    LocalDateTime.now()
            ));

            conexion.commit();
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error al registrar la devolución de envases.", e);
        } finally {
            restaurarAutoCommit();
        }
    }

    public void lavarEnvases(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, StockInsuficienteException, PersistenciaException {
        validarCantidadPositiva(cantidad);
        validarEnvase(envase);

        try {
            conexion.setAutoCommit(false);

            StockEnvase stock = obtenerOCrearStockEnvase(cliente, envase);
            if (stock.getSucios() < cantidad) {
                throw new StockInsuficienteException(
                        "Envases sucios insuficientes para lavar. Disponibles: " + stock.getSucios()
                );
            }

            stock.setSucios(stock.getSucios() - cantidad);
            stock.setAptos(stock.getAptos() + cantidad);
            inventarioRepository.guardarStockEnvase(stock);

            inventarioRepository.registrarMovimientoEnvase(new MovimientoEnvase(
                    UUID.randomUUID().toString(),
                    cliente,
                    envase,
                    TipoMovimientoEnvase.LAVADO,
                    cantidad,
                    LocalDateTime.now()
            ));

            conexion.commit();
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error al registrar el lavado de envases.", e);
        } finally {
            restaurarAutoCommit();
        }
    }

    public void registrarLavado(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, StockInsuficienteException, PersistenciaException {
        lavarEnvases(cliente, envase, cantidad);
    }

    public void registrarProduccion(Cliente cliente, Presentacion presentacion, int cantidad)
            throws ReglaNegocioException, StockInsuficienteException, PersistenciaException {
        validarCantidadPositiva(cantidad);
        validarPresentacion(presentacion);
        validarTitularidadMarca(cliente, presentacion.getMarca());

        try {
            conexion.setAutoCommit(false);

            Envase envase = presentacion.getEnvase();
            if (envase != null) {
                StockEnvase stockEnv = obtenerOCrearStockEnvase(cliente, envase);
                if (stockEnv.getAptos() < cantidad) {
                    throw new StockInsuficienteException(
                            "Envases aptos insuficientes para producción. Disponibles: " + stockEnv.getAptos()
                    );
                }
                stockEnv.setAptos(stockEnv.getAptos() - cantidad);
                inventarioRepository.guardarStockEnvase(stockEnv);

                inventarioRepository.registrarMovimientoEnvase(new MovimientoEnvase(
                        UUID.randomUUID().toString(),
                        cliente,
                        envase,
                        TipoMovimientoEnvase.PRODUCCION,
                        cantidad,
                        LocalDateTime.now()
                ));
            }

            StockProducto stockProd = inventarioRepository.buscarStockProducto(cliente, presentacion);
            if (stockProd == null) {
                stockProd = new StockProducto(UUID.randomUUID().toString(), cliente, presentacion, 0);
            }
            stockProd.setDisponibles(stockProd.getDisponibles() + cantidad);
            inventarioRepository.guardarStockProducto(stockProd);

            conexion.commit();
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error al registrar la producción.", e);
        } finally {
            restaurarAutoCommit();
        }
    }

    public void registrarMovimientoCajon(Cliente cliente, String tipoCajonId, int cantidad, boolean esRetorno)
            throws ReglaNegocioException, StockInsuficienteException, PersistenciaException {
        validarCantidadPositiva(cantidad);
        if (tipoCajonId == null || tipoCajonId.isBlank()) {
            throw new ReglaNegocioException("Debe indicar el ID del tipo de cajón.");
        }

        try {
            conexion.setAutoCommit(false);

            TipoCajon tipoCajon = new TipoCajon();
            tipoCajon.setId(tipoCajonId);

            int saldoActual = inventarioRepository.saldoCajones(cliente, tipoCajon);
            if (!esRetorno && saldoActual < cantidad) {
                throw new StockInsuficienteException(
                        "Cajones insuficientes para entregar. Disponibles: " + saldoActual
                );
            }

            int nuevoSaldo = esRetorno ? (saldoActual + cantidad) : (saldoActual - cantidad);
            inventarioRepository.guardarStockCajon(new StockCajon(tipoCajon, Math.max(0, nuevoSaldo)));

            inventarioRepository.registrarMovimientoCajon(new MovimientoCajon(
                    UUID.randomUUID().toString(),
                    cliente,
                    tipoCajon,
                    cantidad,
                    LocalDateTime.now(),
                    esRetorno
            ));

            conexion.commit();
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error al registrar el movimiento de cajón.", e);
        } finally {
            restaurarAutoCommit();
        }
    }

    public List<StockProducto> consultarStock(Cliente cliente) throws PersistenciaException {
        try {
            StockProducto unico = inventarioRepository.buscarStockProducto(cliente, null);
            return unico == null ? List.of() : List.of(unico);
        } catch (RuntimeException e) {
            throw new PersistenciaException("Error al consultar el stock de productos.", e);
        }
    }

    public List<StockEnvase> listarStockEnvases() throws PersistenciaException {
        return List.of();
    }

    public List<StockProducto> listarStockProductos() throws PersistenciaException {
        return List.of();
    }

    public List<MovimientoEnvase> listarMovimientosEnvases() throws PersistenciaException {
        return List.of();
    }

    public List<StockCajon> listarStockCajones() throws PersistenciaException {
        return List.of();
    }

    public List<MovimientoCajon> listarMovimientosCajones() throws PersistenciaException {
        return List.of();
    }

    public String describir(Retornable elemento) {
        if (elemento == null) {
            return "";
        }
        return elemento.getCodigo() + " - " + elemento.getDescripcion();
    }

    private StockEnvase obtenerOCrearStockEnvase(Cliente cliente, Envase envase) {
        StockEnvase stock = inventarioRepository.buscarStockEnvase(cliente, envase);
        if (stock == null) {
            stock = new StockEnvase(UUID.randomUUID().toString(), cliente, envase, 0, 0);
        }
        return stock;
    }

    private void validarCantidadPositiva(int cantidad) throws ReglaNegocioException {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a cero.");
        }
    }

    private void validarEnvase(Envase envase) throws ReglaNegocioException {
        if (envase == null) {
            throw new ReglaNegocioException("Debe indicar un envase válido.");
        }
    }

    private void validarPresentacion(Presentacion presentacion) throws ReglaNegocioException {
        if (presentacion == null) {
            throw new ReglaNegocioException("Debe indicar una presentación válida.");
        }
    }

    private void validarTitularidadMarca(Cliente clienteOperacion, Marca marca) throws ReglaNegocioException {
        if (marca == null) {
            return;
        }
        if (marca.isEsPropiaFabrica()) {
            if (clienteOperacion != null) {
                throw new ReglaNegocioException("La marca es propia de fábrica y no corresponde a un cliente fasón.");
            }
        } else {
            if (clienteOperacion == null) {
                throw new ReglaNegocioException("La marca es de fasón y requiere un cliente titular.");
            }
            if (marca.getTitular() != null
                    && marca.getTitular().getId() != null
                    && clienteOperacion.getId() != null
                    && !Objects.equals(marca.getTitular().getId(), clienteOperacion.getId())) {
                throw new ReglaNegocioException("La marca pertenece a otro cliente titular.");
            }
        }
    }

    private void hacerRollback() {
        try {
            conexion.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restaurarAutoCommit() {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}