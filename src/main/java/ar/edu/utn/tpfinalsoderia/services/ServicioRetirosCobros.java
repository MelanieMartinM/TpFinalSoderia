package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.RetiroCobroRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.TransaccionJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Cobro;
import ar.edu.utn.tpfinalsoderia.models.DetalleRetiro;
import ar.edu.utn.tpfinalsoderia.models.MovimientoCajon;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.Retiro;
import ar.edu.utn.tpfinalsoderia.models.StockCajon;
import ar.edu.utn.tpfinalsoderia.models.StockProducto;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;

import java.sql.Connection;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ServicioRetirosCobros {

    private final Connection conexion;
    private final RetiroCobroRepositoryJdbc retiroCobroRepo;
    private final InventarioRepositoryJdbc inventarioRepo;

    public ServicioRetirosCobros(
            Connection conexion,
            RetiroCobroRepositoryJdbc retiroCobroRepo,
            InventarioRepositoryJdbc inventarioRepo
    ) {
        this.conexion = Objects.requireNonNull(conexion, "La conexión no puede ser nula.");
        this.retiroCobroRepo = Objects.requireNonNull(retiroCobroRepo, "El repositorio de retiros y cobros no puede ser nulo.");
        this.inventarioRepo = Objects.requireNonNull(inventarioRepo, "El repositorio de inventario no puede ser nulo.");
        if (conexion != inventarioRepo.getConexion() || conexion != retiroCobroRepo.getConexion())
            throw new IllegalArgumentException("Todos los repositorios deben compartir la conexión.");
    }

    public void registrarRetiroYCobro(Retiro retiro, Cobro cobro)
            throws ReglaNegocioException, StockInsuficienteException {
        if (retiro == null || retiro.getDetalles() == null || retiro.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("El retiro debe tener al menos un detalle.");
        }

        Cliente titular = retiro.getCliente();
        if (titular == null || titular.getId() == null || titular.getId().isBlank())
            throw new ReglaNegocioException("Debe indicar el repartidor titular.");

        for (DetalleRetiro det : retiro.getDetalles()) {
            if (det == null) {
                throw new ReglaNegocioException("Hay un detalle nulo en el retiro.");
            }
            if (det.getCantidadRetirada() <= 0) {
                throw new ReglaNegocioException("La cantidad retirada debe ser mayor a cero.");
            }
            if (det.getPrecioAplicado() == null || det.getPrecioAplicado().signum() < 0 || det.getPrecioAplicado().scale() > 2) {
                throw new ReglaNegocioException("El precio aplicado no puede ser negativo.");
            }
            if (det.getCantidadCajones() < 0)
                throw new ReglaNegocioException("La cantidad de cajones no puede ser negativa.");
            Presentacion pres = det.getPresentacion();
            if (pres == null || pres.getId() == null || pres.getId().isBlank()) {
                throw new ReglaNegocioException("Cada detalle del retiro debe indicar una presentación.");
            }


        }

        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {
            java.util.Set<String> detallesProcesados = new java.util.HashSet<>();
            for (DetalleRetiro det : retiro.getDetalles()) {
                validarPedidoYPrecio(retiro, det, detallesProcesados);
            }
            BigDecimal totalRetiro = retiro.calcularTotal();
            validarCobro(cobro, totalRetiro);

            for (DetalleRetiro det : retiro.getDetalles()) {
                StockProducto stockProd = inventarioRepo.buscarStockProducto(titular, det.getPresentacion());
                int disponibles = (stockProd == null) ? 0 : stockProd.getDisponibles();

                if (disponibles < det.getCantidadRetirada()) {
                    throw new StockInsuficienteException(
                            "Stock insuficiente para '" + det.getPresentacion().getNombre()
                                    + "'. Disponible: " + disponibles + ", solicitado: " + det.getCantidadRetirada()
                    );
                }

                stockProd.setDisponibles(disponibles - det.getCantidadRetirada());
                inventarioRepo.guardarStockProducto(stockProd);

                if (det.getCantidadCajones() > 0) {
                    TipoCajon tipoCajon = det.getTipoCajon();
                    if (tipoCajon == null || tipoCajon.getId() == null || tipoCajon.getId().isBlank()) {
                        throw new ReglaNegocioException("Debe indicar el tipo de cajón cuando retira cajones.");
                    }

                    int cajonesDisp = inventarioRepo.saldoCajones(titular, tipoCajon);
                    if (cajonesDisp < det.getCantidadCajones()) {
                        throw new StockInsuficienteException(
                                "Cajones insuficientes para '" + tipoCajon.getDescripcion()
                                        + "'. Disponibles: " + cajonesDisp
                        );
                    }

                    inventarioRepo.guardarStockCajon(new StockCajon(titular, tipoCajon, cajonesDisp - det.getCantidadCajones()));
                    inventarioRepo.registrarMovimientoCajon(new MovimientoCajon(
                            UUID.randomUUID().toString(),
                            titular,
                            tipoCajon,
                            det.getCantidadCajones(),
                            LocalDateTime.now(),
                            false
                    ));
                }
            }

            if (retiro.getId() == null || retiro.getId().isBlank()) {
                retiro.setId(UUID.randomUUID().toString());
            }
            if (retiro.getFecha() == null) {
                retiro.setFecha(LocalDateTime.now());
            }

            retiroCobroRepo.guardarRetiro(retiro);

            if (cobro != null && !cobro.isSinCobroAutorizado()) {
                if (cobro.getId() == null || cobro.getId().isBlank()) {
                    cobro.setId(UUID.randomUUID().toString());
                }
                if (cobro.getFecha() == null) cobro.setFecha(LocalDateTime.now());
                retiro.setCobro(cobro);
                retiroCobroRepo.guardarCobro(retiro.getId(), cobro);
            }
            tx.confirmar();
        } catch (SQLException e) {
            throw new PersistenciaException("Error en la transacción de retiro y cobro.", e);
        }
    }

    private void validarPedidoYPrecio(Retiro retiro, DetalleRetiro det, java.util.Set<String> vistos)
            throws ReglaNegocioException {
        String detalleId = det.getDetallePedidoId();
        String pedidoId = retiro.getPedidoId();
        boolean tienePedido = pedidoId != null && !pedidoId.isBlank();
        boolean tieneDetalle = detalleId != null && !detalleId.isBlank();
        if (!tienePedido && !tieneDetalle) return;
        if (!tienePedido || !tieneDetalle)
            throw new ReglaNegocioException("Para retirar de un pedido se necesitan los ID del pedido y detalle.");
        if (!vistos.add(detalleId))
            throw new ReglaNegocioException("El mismo detalle de pedido aparece dos veces en el retiro.");
        String sql = "SELECT p.cliente_id, p.estado, d.presentacion_id, d.cantidad_unidades, " +
                "d.precio_unitario_pactado FROM pedidos p JOIN detalle_pedido d ON d.pedido_id=p.id " +
                "WHERE p.id=? AND d.id=? FOR UPDATE";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, pedidoId); ps.setString(2, detalleId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new ReglaNegocioException("El detalle no pertenece al pedido indicado.");
                if (!retiro.getCliente().getId().equals(rs.getString("cliente_id")))
                    throw new ReglaNegocioException("El pedido pertenece a otro repartidor.");
                String estado = rs.getString("estado");
                if (!"CONFIRMADO".equals(estado) && !"PARCIAL".equals(estado))
                    throw new ReglaNegocioException("El pedido no está confirmado o ya fue cerrado.");
                if (!det.getPresentacion().getId().equals(rs.getString("presentacion_id")))
                    throw new ReglaNegocioException("La presentación no coincide con el detalle del pedido.");
                int pedida = rs.getInt("cantidad_unidades");
                int retirada = retiroCobroRepo.obtenerCantidadYaRetirada(detalleId);
                if ((long) retirada + det.getCantidadRetirada() > pedida)
                    throw new ReglaNegocioException("La cantidad supera el saldo del pedido.");
                det.setPedidoId(pedidoId);
                det.setCantidadPedida(pedida);
                det.setPrecioAplicado(rs.getBigDecimal("precio_unitario_pactado"));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar el detalle del pedido.", e);
        }
    }

    public List<Cobro> listarCobros() {
        return retiroCobroRepo.listarCobros();
    }

    /** Registra un pago posterior asociado a un retiro ya guardado. */
    public void registrarCobroPosterior(String retiroId, Cobro cobro) throws ReglaNegocioException {
        if (retiroId == null || retiroId.isBlank())
            throw new ReglaNegocioException("Debe indicar el ID del retiro.");
        if (cobro == null || cobro.isSinCobroAutorizado())
            throw new ReglaNegocioException("Debe indicar un cobro efectivo.");
        validarCobro(cobro, BigDecimal.ZERO);
        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {
            try (java.sql.PreparedStatement ps = conexion.prepareStatement(
                    "SELECT id FROM retiros WHERE id = ? FOR UPDATE")) {
                ps.setString(1, retiroId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new ReglaNegocioException("El retiro indicado no existe.");
                }
            }
            retiroCobroRepo.guardarCobro(retiroId, cobro);
            tx.confirmar();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar el pago posterior.", e);
        }
    }

    private void validarCobro(Cobro cobro, BigDecimal totalRetiro) throws ReglaNegocioException {
        // Un retiro puede quedar pendiente. El estado no implica autorización adicional.
        if (cobro == null || cobro.isSinCobroAutorizado()) return;
        if (cobro.getMedioPago() == null)
            throw new ReglaNegocioException("Debe seleccionar un medio de pago.");
        if (cobro.getMonto() == null || cobro.getMonto().signum() <= 0 || cobro.getMonto().scale() > 2)
            throw new ReglaNegocioException("El monto cobrado debe ser positivo.");
    }
}
