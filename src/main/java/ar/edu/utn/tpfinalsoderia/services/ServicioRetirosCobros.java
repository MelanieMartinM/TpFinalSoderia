package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.RetiroCobroRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.TransaccionJdbc;
import ar.edu.utn.tpfinalsoderia.enums.EstadoPedido;
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
import ar.edu.utn.tpfinalsoderia.utils.SesionUsuario;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ServicioRetirosCobros {

    private final Connection conexion;
    private final RetiroCobroRepositoryJdbc retiroCobroRepo;
    private final InventarioRepositoryJdbc inventarioRepo;

    public ServicioRetirosCobros(
            Connection conexion,
            RetiroCobroRepositoryJdbc retiroCobroRepo,
            InventarioRepositoryJdbc inventarioRepo) {

        this.conexion = Objects.requireNonNull(
                conexion, "La conexión no puede ser nula.");

        this.retiroCobroRepo = Objects.requireNonNull(
                retiroCobroRepo, "El repositorio de retiros no puede ser nulo.");

        this.inventarioRepo = Objects.requireNonNull(
                inventarioRepo, "El repositorio de inventario no puede ser nulo.");

        if (conexion != inventarioRepo.getConexion()
                || conexion != retiroCobroRepo.getConexion()) {

            throw new IllegalArgumentException(
                    "Todos los repositorios deben compartir la conexión.");
        }
    }

    // =====================================================
    // REGISTRAR RETIRO Y COBRO
    // =====================================================

    public void registrarRetiroYCobro(Retiro retiro, Cobro cobro)
            throws ReglaNegocioException, StockInsuficienteException {

        verificarSesion();

        if (retiro == null
                || retiro.getDetalles() == null
                || retiro.getDetalles().isEmpty()) {

            throw new ReglaNegocioException(
                    "El retiro debe tener al menos un detalle.");
        }

        Cliente titular = retiro.getCliente();

        if (titular == null
                || titular.getId() == null
                || titular.getId().isBlank()) {

            throw new ReglaNegocioException(
                    "Debe indicar el repartidor titular.");
        }

        for (DetalleRetiro detalle : retiro.getDetalles()) {
            validarDetalle(detalle);
        }

        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {

            Set<String> detallesProcesados = new HashSet<>();

            for (DetalleRetiro detalle : retiro.getDetalles()) {

                validarPedidoYPrecio(
                        retiro,
                        detalle,
                        detallesProcesados
                );
            }

            BigDecimal totalRetiro = retiro.calcularTotal();

            validarCobroInicial(cobro, totalRetiro);

            // Descontar stock de productos y cajones.

            for (DetalleRetiro detalle : retiro.getDetalles()) {

                descontarStockProducto(titular, detalle);

                if (detalle.getCantidadCajones() > 0) {
                    descontarStockCajones(titular, detalle);
                }
            }

            if (retiro.getId() == null || retiro.getId().isBlank()) {
                retiro.setId(UUID.randomUUID().toString());
            }

            if (retiro.getFecha() == null) {
                retiro.setFecha(LocalDateTime.now());
            }

            retiroCobroRepo.guardarRetiro(retiro);

            // Guardar tanto los cobros efectivos como
            // las autorizaciones de retiro sin pago.

            if (cobro != null) {

                prepararCobro(cobro);

                retiro.setCobro(cobro);

                retiroCobroRepo.guardarCobro(
                        retiro.getId(),
                        cobro
                );
            }

            // Actualizar el estado del pedido.

            if (retiro.getPedidoId() != null
                    && !retiro.getPedidoId().isBlank()) {

                actualizarEstadoPedido(retiro.getPedidoId());
            }

            tx.confirmar();

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error en la transacción de retiro y cobro.", e);
        }
    }

    // =====================================================
    // VALIDACIÓN DE DETALLES
    // =====================================================

    private void validarDetalle(DetalleRetiro detalle)
            throws ReglaNegocioException {

        if (detalle == null) {
            throw new ReglaNegocioException(
                    "Hay un detalle nulo en el retiro.");
        }

        if (detalle.getCantidadRetirada() <= 0) {
            throw new ReglaNegocioException(
                    "La cantidad retirada debe ser mayor a cero.");
        }

        if (detalle.getPrecioAplicado() == null
                || detalle.getPrecioAplicado().signum() < 0
                || detalle.getPrecioAplicado().scale() > 2) {

            throw new ReglaNegocioException(
                    "El precio debe ser válido, no negativo "
                            + "y tener hasta dos decimales.");
        }

        if (detalle.getCantidadCajones() < 0) {
            throw new ReglaNegocioException(
                    "La cantidad de cajones no puede ser negativa.");
        }

        Presentacion presentacion = detalle.getPresentacion();

        if (presentacion == null
                || presentacion.getId() == null
                || presentacion.getId().isBlank()) {

            throw new ReglaNegocioException(
                    "Cada detalle debe indicar una presentación.");
        }
    }

    // =====================================================
    // STOCK DE PRODUCTOS
    // =====================================================

    private void descontarStockProducto(
            Cliente titular,
            DetalleRetiro detalle)
            throws StockInsuficienteException {

        StockProducto stockProducto =
                inventarioRepo.buscarStockProducto(
                        titular,
                        detalle.getPresentacion()
                );

        int disponibles = stockProducto == null
                ? 0
                : stockProducto.getDisponibles();

        if (disponibles < detalle.getCantidadRetirada()) {

            throw new StockInsuficienteException(
                    "Stock insuficiente para '"
                            + detalle.getPresentacion().getNombre()
                            + "'. Disponible: " + disponibles
                            + ", solicitado: "
                            + detalle.getCantidadRetirada()
            );
        }

        stockProducto.setDisponibles(
                disponibles - detalle.getCantidadRetirada()
        );

        inventarioRepo.guardarStockProducto(stockProducto);
    }

    // =====================================================
    // STOCK DE CAJONES
    // =====================================================

    private void descontarStockCajones(
            Cliente titular,
            DetalleRetiro detalle)
            throws ReglaNegocioException, StockInsuficienteException {

        TipoCajon tipoCajon = detalle.getTipoCajon();

        if (tipoCajon == null
                || tipoCajon.getId() == null
                || tipoCajon.getId().isBlank()) {

            throw new ReglaNegocioException(
                    "Debe indicar el tipo de cajón.");
        }

        int disponibles =
                inventarioRepo.saldoCajones(titular, tipoCajon);

        if (disponibles < detalle.getCantidadCajones()) {

            throw new StockInsuficienteException(
                    "Cajones insuficientes para '"
                            + tipoCajon.getDescripcion()
                            + "'. Disponibles: " + disponibles
            );
        }

        inventarioRepo.guardarStockCajon(
                new StockCajon(
                        titular,
                        tipoCajon,
                        disponibles - detalle.getCantidadCajones()
                )
        );

        inventarioRepo.registrarMovimientoCajon(
                new MovimientoCajon(
                        UUID.randomUUID().toString(),
                        titular,
                        tipoCajon,
                        detalle.getCantidadCajones(),
                        LocalDateTime.now(),
                        false
                )
        );
    }

    // =====================================================
    // VALIDAR PEDIDO Y PRECIO
    // =====================================================

    private void validarPedidoYPrecio(
            Retiro retiro,
            DetalleRetiro detalle,
            Set<String> vistos)
            throws ReglaNegocioException {

        String detalleId = detalle.getDetallePedidoId();
        String pedidoId = retiro.getPedidoId();

        boolean tienePedido =
                pedidoId != null && !pedidoId.isBlank();

        boolean tieneDetalle =
                detalleId != null && !detalleId.isBlank();

        if (!tienePedido && !tieneDetalle) {

            validarPrecioCatalogo(detalle);

            return;
        }

        if (!tienePedido || !tieneDetalle) {

            throw new ReglaNegocioException(
                    "Debe indicar el pedido y su detalle.");
        }

        if (!vistos.add(detalleId)) {

            throw new ReglaNegocioException(
                    "El mismo detalle aparece dos veces en el retiro.");
        }

        String sql =
                "SELECT p.cliente_id, p.estado, "
                        + "d.presentacion_id, d.cantidad_unidades, "
                        + "d.precio_unitario_pactado "
                        + "FROM pedidos p "
                        + "JOIN detalle_pedido d ON d.pedido_id = p.id "
                        + "WHERE p.id = ? AND d.id = ? FOR UPDATE";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, pedidoId);
            ps.setString(2, detalleId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new ReglaNegocioException(
                            "El detalle no pertenece al pedido indicado.");
                }

                if (!retiro.getCliente().getId()
                        .equals(rs.getString("cliente_id"))) {

                    throw new ReglaNegocioException(
                            "El pedido pertenece a otro repartidor.");
                }

                String estado = rs.getString("estado");

                if (!EstadoPedido.CONFIRMADO.name().equals(estado)
                        && !EstadoPedido.PARCIAL.name().equals(estado)) {

                    throw new ReglaNegocioException(
                            "El pedido no está confirmado o ya fue cerrado.");
                }

                if (!detalle.getPresentacion().getId()
                        .equals(rs.getString("presentacion_id"))) {

                    throw new ReglaNegocioException(
                            "La presentación no coincide con el pedido.");
                }

                int pedida = rs.getInt("cantidad_unidades");

                int retirada =
                        retiroCobroRepo.obtenerCantidadYaRetirada(
                                detalleId
                        );

                if ((long) retirada
                        + detalle.getCantidadRetirada() > pedida) {

                    throw new ReglaNegocioException(
                            "La cantidad supera el saldo del pedido.");
                }

                detalle.setPedidoId(pedidoId);
                detalle.setCantidadPedida(pedida);

                detalle.setPrecioAplicado(
                        rs.getBigDecimal("precio_unitario_pactado")
                );
            }

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error al verificar el detalle del pedido.", e);
        }
    }

    // =====================================================
    // VALIDAR PRECIO DE RETIRO DIRECTO
    // =====================================================

    private void validarPrecioCatalogo(DetalleRetiro detalle)
            throws ReglaNegocioException {

        String sql =
                "SELECT precio_llenado, activa "
                        + "FROM presentaciones WHERE id = ? FOR UPDATE";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(
                    1,
                    detalle.getPresentacion().getId()
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next() || !rs.getBoolean("activa")) {

                    throw new ReglaNegocioException(
                            "La presentación no existe o está desactivada.");
                }

                BigDecimal precioCatalogo =
                        rs.getBigDecimal("precio_llenado");

                if (precioCatalogo.compareTo(
                        detalle.getPrecioAplicado()) != 0) {

                    throw new ReglaNegocioException(
                            "El precio aplicado debe coincidir "
                                    + "con el precio vigente del catálogo.");
                }
            }

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "No se pudo verificar el precio del catálogo.", e);
        }
    }

    // =====================================================
    // ACTUALIZAR ESTADO DEL PEDIDO
    // =====================================================

    private void actualizarEstadoPedido(String pedidoId)
            throws SQLException {

        String sql =
                "SELECT d.cantidad_unidades, "
                        + "COALESCE(SUM(dr.cantidad_retirada), 0) "
                        + "AS cantidad_retirada "
                        + "FROM detalle_pedido d "
                        + "LEFT JOIN detalle_retiro dr "
                        + "ON dr.detalle_pedido_id = d.id "
                        + "WHERE d.pedido_id = ? "
                        + "GROUP BY d.id, d.cantidad_unidades";

        boolean tieneDetalles = false;
        boolean todosCompletos = true;
        boolean tieneRetiros = false;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, pedidoId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    tieneDetalles = true;

                    long pedidas =
                            rs.getLong("cantidad_unidades");

                    long retiradas =
                            rs.getLong("cantidad_retirada");

                    if (retiradas > 0) {
                        tieneRetiros = true;
                    }

                    if (retiradas < pedidas) {
                        todosCompletos = false;
                    }
                }
            }
        }

        if (!tieneDetalles) {
            return;
        }

        EstadoPedido nuevoEstado;

        if (todosCompletos) {
            nuevoEstado = EstadoPedido.COMPLETADO;
        } else if (tieneRetiros) {
            nuevoEstado = EstadoPedido.PARCIAL;
        } else {
            nuevoEstado = EstadoPedido.CONFIRMADO;
        }

        String actualizar =
                "UPDATE pedidos SET estado = ? WHERE id = ?";

        try (PreparedStatement ps =
                     conexion.prepareStatement(actualizar)) {

            ps.setString(1, nuevoEstado.name());
            ps.setString(2, pedidoId);

            ps.executeUpdate();
        }
    }

    // =====================================================
    // VALIDAR COBRO INICIAL
    // =====================================================

    private void validarCobroInicial(
            Cobro cobro,
            BigDecimal totalRetiro)
            throws ReglaNegocioException {

        if (cobro == null) {

            throw new ReglaNegocioException(
                    "Debe registrar el pago o una autorización.");
        }

        if (cobro.isSinCobroAutorizado()) {

            validarAutorizacion(cobro);

            return;
        }

        validarCobroEfectivo(cobro);

        if (cobro.getMonto().compareTo(totalRetiro) > 0) {

            throw new ReglaNegocioException(
                    "El monto cobrado supera el total del retiro.");
        }

        if (cobro.getMonto().compareTo(totalRetiro) < 0) {

            throw new ReglaNegocioException(
                    "Debe abonar el total del retiro o registrar "
                            + "una autorización sin cobro.");
        }
    }

    // =====================================================
    // AUTORIZACIÓN SIN PAGO
    // =====================================================

    private void validarAutorizacion(Cobro cobro)
            throws ReglaNegocioException {

        if (!SesionUsuario.esAdministrador()) {

            throw new ReglaNegocioException(
                    "Solo un administrador puede autorizar "
                            + "retiros sin pago.");
        }

        if (cobro.getMonto() == null
                || cobro.getMonto().signum() != 0) {

            throw new ReglaNegocioException(
                    "Una autorización sin pago debe tener monto cero.");
        }

        if (cobro.getMotivoAutorizacion() == null
                || cobro.getMotivoAutorizacion().isBlank()) {

            throw new ReglaNegocioException(
                    "Debe indicar el motivo de la autorización.");
        }

        String motivo = cobro.getMotivoAutorizacion().trim();

        if (motivo.length() > 255) {

            throw new ReglaNegocioException(
                    "El motivo no puede superar los 255 caracteres.");
        }

        cobro.setMotivoAutorizacion(motivo);

        cobro.setResponsableAutorizacion(
                SesionUsuario.getUsuarioActual().getNombreUsuario()
        );

        cobro.setMonto(BigDecimal.ZERO);
        cobro.setMedioPago(null);
    }

    // =====================================================
    // REGISTRAR PAGO POSTERIOR
    // =====================================================

    public void registrarCobroPosterior(
            String retiroId,
            Cobro cobro)
            throws ReglaNegocioException {

        verificarSesion();

        if (retiroId == null || retiroId.isBlank()) {

            throw new ReglaNegocioException(
                    "Debe indicar el ID del retiro.");
        }

        validarCobroEfectivo(cobro);

        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {

            BigDecimal totalRetiro =
                    obtenerTotalRetiro(retiroId);

            BigDecimal totalCobrado =
                    obtenerTotalCobrado(retiroId);

            BigDecimal deuda =
                    totalRetiro.subtract(totalCobrado);

            if (deuda.signum() <= 0) {

                throw new ReglaNegocioException(
                        "El retiro no tiene deuda pendiente.");
            }

            if (cobro.getMonto().compareTo(deuda) > 0) {

                throw new ReglaNegocioException(
                        "El monto supera la deuda pendiente de $"
                                + deuda.toPlainString()
                );
            }

            prepararCobro(cobro);

            retiroCobroRepo.guardarCobro(retiroId, cobro);

            tx.confirmar();

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error al registrar el pago posterior.", e);
        }
    }

    private BigDecimal obtenerTotalRetiro(String retiroId)
            throws SQLException, ReglaNegocioException {

        String sql =
                "SELECT total FROM retiros WHERE id = ? FOR UPDATE";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, retiroId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new ReglaNegocioException(
                            "El retiro indicado no existe.");
                }

                return rs.getBigDecimal("total");
            }
        }
    }

    private BigDecimal obtenerTotalCobrado(String retiroId)
            throws SQLException {

        String sql =
                "SELECT COALESCE(SUM(monto), 0) AS total_cobrado "
                        + "FROM cobros WHERE retiro_id = ? "
                        + "AND sin_cobro_autorizado = FALSE";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, retiroId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getBigDecimal("total_cobrado");
                }

                return BigDecimal.ZERO;
            }
        }
    }

    // =====================================================
    // VALIDACIONES GENERALES DE COBROS
    // =====================================================

    private void validarCobroEfectivo(Cobro cobro)
            throws ReglaNegocioException {

        if (cobro == null || cobro.isSinCobroAutorizado()) {

            throw new ReglaNegocioException(
                    "Debe indicar un cobro efectivo.");
        }

        if (cobro.getMedioPago() == null) {

            throw new ReglaNegocioException(
                    "Debe seleccionar un medio de pago.");
        }

        if (cobro.getMonto() == null
                || cobro.getMonto().signum() <= 0
                || cobro.getMonto().scale() > 2) {

            throw new ReglaNegocioException(
                    "El monto debe ser positivo y tener "
                            + "hasta dos decimales.");
        }

        cobro.setResponsableAutorizacion(null);
        cobro.setMotivoAutorizacion(null);
    }

    private void prepararCobro(Cobro cobro) {

        if (cobro.getId() == null || cobro.getId().isBlank()) {
            cobro.setId(UUID.randomUUID().toString());
        }

        if (cobro.getFecha() == null) {
            cobro.setFecha(LocalDateTime.now());
        }
    }

    // =====================================================
    // CONSULTAS Y SESIÓN
    // =====================================================

    public List<Cobro> listarCobros() {

        verificarSesion();

        return retiroCobroRepo.listarCobros();
    }

    private void verificarSesion() {

        if (!SesionUsuario.haySesionActiva()) {

            throw new IllegalStateException(
                    "No hay una sesión activa.");
        }
    }
}