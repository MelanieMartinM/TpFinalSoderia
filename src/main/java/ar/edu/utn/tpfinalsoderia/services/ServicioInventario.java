package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.TransaccionJdbc;
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
        if (conexion != inventarioRepository.getConexion())
            throw new IllegalArgumentException("Servicio y repositorio deben compartir la misma conexión.");
    }

    public void recibirVacios(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, PersistenciaException {
        validarTitular(cliente);
        validarCantidadPositiva(cantidad);
        validarEnvase(envase);

        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {

            StockEnvase stock = obtenerOCrearStockEnvase(cliente, envase);
            stock.setSucios(Math.addExact(stock.getSucios(), cantidad));
            inventarioRepository.guardarStockEnvase(stock);

            inventarioRepository.registrarMovimientoEnvase(new MovimientoEnvase(
                    UUID.randomUUID().toString(),
                    cliente,
                    envase,
                    TipoMovimientoEnvase.INGRESO,
                    cantidad,
                    LocalDateTime.now()
            ));

            tx.confirmar();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar la recepción de envases vacíos.", e);
        }
    }

    public void registrarRecepcionVacios(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, PersistenciaException {
        recibirVacios(cliente, envase, cantidad);
    }

    public void registrarDevolucionEnvases(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, PersistenciaException {
        validarTitular(cliente);
        validarCantidadPositiva(cantidad);
        validarEnvase(envase);

        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {

            StockEnvase stock = obtenerOCrearStockEnvase(cliente, envase);
            stock.setSucios(Math.addExact(stock.getSucios(), cantidad));
            inventarioRepository.guardarStockEnvase(stock);

            inventarioRepository.registrarMovimientoEnvase(new MovimientoEnvase(
                    UUID.randomUUID().toString(),
                    cliente,
                    envase,
                    TipoMovimientoEnvase.RETORNO,
                    cantidad,
                    LocalDateTime.now()
            ));

            tx.confirmar();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar la devolución de envases.", e);
        }
    }

    public void lavarEnvases(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, StockInsuficienteException, PersistenciaException {
        validarTitular(cliente);
        validarCantidadPositiva(cantidad);
        validarEnvase(envase);

        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {

            StockEnvase stock = obtenerOCrearStockEnvase(cliente, envase);
            if (stock.getSucios() < cantidad) {
                throw new StockInsuficienteException(
                        "Envases sucios insuficientes para lavar. Disponibles: " + stock.getSucios()
                );
            }

            stock.setSucios(stock.getSucios() - cantidad);
            stock.setAptos(Math.addExact(stock.getAptos(), cantidad));
            inventarioRepository.guardarStockEnvase(stock);

            inventarioRepository.registrarMovimientoEnvase(new MovimientoEnvase(
                    UUID.randomUUID().toString(),
                    cliente,
                    envase,
                    TipoMovimientoEnvase.LAVADO,
                    cantidad,
                    LocalDateTime.now()
            ));

            tx.confirmar();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar el lavado de envases.", e);
        }
    }

    public void registrarLavado(Cliente cliente, Envase envase, int cantidad)
            throws ReglaNegocioException, StockInsuficienteException, PersistenciaException {
        lavarEnvases(cliente, envase, cantidad);
    }

    public void registrarProduccion(Cliente cliente, Presentacion presentacion, int cantidad)
            throws ReglaNegocioException, StockInsuficienteException, PersistenciaException {
        validarTitular(cliente);
        validarCantidadPositiva(cantidad);
        validarPresentacion(presentacion);
        presentacion = cargarPresentacion(presentacion.getId());

        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {

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
                stockProd = new StockProducto(cliente, presentacion, 0);
            }
            stockProd.setDisponibles(Math.addExact(stockProd.getDisponibles(), cantidad));
            inventarioRepository.guardarStockProducto(stockProd);

            tx.confirmar();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar la producción.", e);
        }
    }

    public void registrarMovimientoCajon(Cliente cliente, String tipoCajonId, int cantidad, boolean esRetorno)
            throws ReglaNegocioException, StockInsuficienteException, PersistenciaException {
        validarTitular(cliente);
        validarCantidadPositiva(cantidad);
        if (tipoCajonId == null || tipoCajonId.isBlank()) {
            throw new ReglaNegocioException("Debe indicar el ID del tipo de cajón.");
        }

        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {

            TipoCajon tipoCajon = new TipoCajon();
            tipoCajon.setId(tipoCajonId);

            int saldoActual = inventarioRepository.saldoCajones(cliente, tipoCajon);
            if (!esRetorno && saldoActual < cantidad) {
                throw new StockInsuficienteException(
                        "Cajones insuficientes para entregar. Disponibles: " + saldoActual
                );
            }

            int nuevoSaldo = esRetorno ? Math.addExact(saldoActual, cantidad) : (saldoActual - cantidad);
            inventarioRepository.guardarStockCajon(new StockCajon(cliente, tipoCajon, nuevoSaldo));

            inventarioRepository.registrarMovimientoCajon(new MovimientoCajon(
                    UUID.randomUUID().toString(),
                    cliente,
                    tipoCajon,
                    cantidad,
                    LocalDateTime.now(),
                    esRetorno
            ));

            tx.confirmar();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar el movimiento de cajón.", e);
        }
    }

    public List<StockProducto> consultarStock(Cliente cliente) throws PersistenciaException {
        if (cliente == null || cliente.getId() == null || cliente.getId().isBlank())
            throw new IllegalArgumentException("Debe indicar un repartidor.");
        return inventarioRepository.listarStockProductos().stream()
                .filter(s -> s.getTitular() != null && cliente.getId().equals(s.getTitular().getId())).toList();
    }

    public List<StockEnvase> listarStockEnvases() throws PersistenciaException {
        return inventarioRepository.listarStockEnvases();
    }

    public List<StockProducto> listarStockProductos() throws PersistenciaException {
        return inventarioRepository.listarStockProductos();
    }

    public List<MovimientoEnvase> listarMovimientosEnvases() throws PersistenciaException {
        return inventarioRepository.listarMovimientosEnvases();
    }

    public List<StockCajon> listarStockCajones() throws PersistenciaException {
        return inventarioRepository.listarStockCajones();
    }

    public List<MovimientoCajon> listarMovimientosCajones() throws PersistenciaException {
        return inventarioRepository.listarMovimientosCajones();
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
            stock = new StockEnvase(cliente, envase, 0, 0);
        }
        return stock;
    }

    private void validarCantidadPositiva(int cantidad) throws ReglaNegocioException {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a cero.");
        }
    }

    private void validarEnvase(Envase envase) throws ReglaNegocioException {
        if (envase == null || envase.getId() == null || envase.getId().isBlank()) {
            throw new ReglaNegocioException("Debe indicar un envase válido.");
        }
    }

    private void validarPresentacion(Presentacion presentacion) throws ReglaNegocioException {
        if (presentacion == null || presentacion.getId() == null || presentacion.getId().isBlank()) {
            throw new ReglaNegocioException("Debe indicar una presentación válida.");
        }
    }

    private void validarTitular(Cliente cliente) throws ReglaNegocioException {
        if (cliente == null || cliente.getId() == null || cliente.getId().isBlank())
            throw new ReglaNegocioException("Debe indicar el repartidor titular.");
    }

    private Presentacion cargarPresentacion(String id) throws ReglaNegocioException {
        String sql = "SELECT p.envase_id, p.marca_id, m.titular_id, m.es_propia_fabrica " +
                "FROM presentaciones p LEFT JOIN marcas m ON m.id=p.marca_id WHERE p.id=? AND p.activa=TRUE";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getString("envase_id") == null)
                    throw new ReglaNegocioException("La presentación no existe o no tiene envase.");
                Presentacion p = new Presentacion(); p.setId(id);
                Envase e = new Envase(); e.setId(rs.getString("envase_id")); p.setEnvase(e);
                if (rs.getString("marca_id") != null) {
                    Marca m = new Marca(); m.setId(rs.getString("marca_id"));
                    m.setEsPropiaFabrica(rs.getBoolean("es_propia_fabrica"));
                    if (rs.getString("titular_id") != null) {
                        Cliente c = new Cliente(); c.setId(rs.getString("titular_id")); m.setTitular(c);
                    }
                    p.setMarca(m);
                }
                return p;
            }
        } catch (SQLException e) { throw new PersistenciaException("Error al cargar presentación.", e); }
    }

}
