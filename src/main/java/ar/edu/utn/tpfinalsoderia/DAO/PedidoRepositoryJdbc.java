package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.EstadoPedido;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PedidoRepositoryJdbc extends RepositorioJdbc<Pedido> {

    // =====================================================
    // CONSULTAS SQL
    // =====================================================

    private static final String SQL_INSERTAR_PEDIDO =
            "INSERT INTO pedidos "
                    + "(id, cliente_id, fecha, estado, total) "
                    + "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_INSERTAR_DETALLE =
            "INSERT INTO detalle_pedido "
                    + "(id, pedido_id, presentacion_id, "
                    + "cantidad_unidades, precio_unitario_pactado, "
                    + "cantidad_cajones, tipo_cajon_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR_PEDIDO =
            "UPDATE pedidos SET cliente_id = ?, "
                    + "fecha = ?, estado = ?, total = ? "
                    + "WHERE id = ?";

    private static final String SQL_ELIMINAR_DETALLES =
            "DELETE FROM detalle_pedido WHERE pedido_id = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, cliente_id, fecha, estado "
                    + "FROM pedidos WHERE id = ?";

    private static final String SQL_BUSCAR_DETALLES =
            "SELECT id, presentacion_id, cantidad_unidades, "
                    + "precio_unitario_pactado, cantidad_cajones, "
                    + "tipo_cajon_id "
                    + "FROM detalle_pedido WHERE pedido_id = ?";

    private static final String SQL_LISTAR =
            "SELECT id, cliente_id, fecha, estado "
                    + "FROM pedidos ORDER BY fecha DESC";

    private static final String SQL_CANCELAR =
            "UPDATE pedidos SET estado = ? WHERE id = ? "
                    + "AND estado IN ('BORRADOR', 'CONFIRMADO') "
                    + "AND NOT EXISTS ("
                    + "SELECT 1 FROM retiros r "
                    + "WHERE r.pedido_id = pedidos.id)";

    // =====================================================
    // CONSTRUCTOR Y CONEXIÓN
    // =====================================================

    public PedidoRepositoryJdbc() throws PersistenciaException {

        super(null);

        conectar();
    }

    private void conectar() throws PersistenciaException {

        try {

            this.conexion = ConexionFactory.abrirConexion();

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "No se pudo conectar a la base de datos.", e
            );
        }
    }

    // =====================================================
    // MAPEO DE PEDIDOS
    // =====================================================

    @Override
    protected Pedido mapear(ResultSet resultado)
            throws SQLException {

        Pedido pedido = new Pedido();

        pedido.setId(resultado.getString("id"));

        String clienteId = resultado.getString("cliente_id");

        if (clienteId != null) {

            Cliente cliente = new Cliente();

            cliente.setId(clienteId);

            pedido.setCliente(cliente);
        }

        Timestamp tiempo = resultado.getTimestamp("fecha");

        if (tiempo != null) {

            pedido.setFecha(tiempo.toLocalDateTime());
        }

        String estadoStr = resultado.getString("estado");

        if (estadoStr != null) {

            pedido.setEstado(
                    EstadoPedido.valueOf(estadoStr)
            );
        }

        return pedido;
    }

    private DetallePedido mapearDetalle(ResultSet rs)
            throws SQLException {

        DetallePedido detalle = new DetallePedido();

        detalle.setId(rs.getString("id"));

        detalle.setCantidadUnidades(
                rs.getInt("cantidad_unidades")
        );

        detalle.setPrecioUnitario(
                rs.getBigDecimal("precio_unitario_pactado")
        );

        detalle.setCantidadCajones(
                rs.getInt("cantidad_cajones")
        );

        Presentacion presentacion = new Presentacion();

        presentacion.setId(
                rs.getString("presentacion_id")
        );

        detalle.setPresentacion(presentacion);

        String tipoCajonId = rs.getString("tipo_cajon_id");

        if (tipoCajonId != null) {

            TipoCajon tipoCajon = new TipoCajon();

            tipoCajon.setId(tipoCajonId);

            detalle.setTipoCajon(tipoCajon);
        }

        return detalle;
    }

    // =====================================================
    // GUARDAR PEDIDO
    // =====================================================

    @Override
    public void guardar(Pedido pedido)
            throws PersistenciaException {

        if (pedido == null) {

            throw new IllegalArgumentException(
                    "El pedido no puede ser nulo."
            );
        }

        try (TransaccionJdbc tx =
                     new TransaccionJdbc(conexion)) {

            insertarPedido(pedido);

            insertarDetalles(pedido);

            tx.confirmar();

        } catch (SQLException | RuntimeException e) {

            throw new PersistenciaException(
                    "Error al guardar el pedido.", e
            );
        }
    }

    private String insertarPedido(Pedido pedido)
            throws PersistenciaException {

        if (pedido.getId() == null
                || pedido.getId().isBlank()) {

            pedido.setId(UUID.randomUUID().toString());
        }

        try (PreparedStatement ps =
                     conexion.prepareStatement(
                             SQL_INSERTAR_PEDIDO)) {

            ps.setString(1, pedido.getId());

            ps.setString(
                    2,
                    pedido.getCliente().getId()
            );

            ps.setTimestamp(
                    3,
                    Timestamp.valueOf(pedido.getFecha())
            );

            ps.setString(
                    4,
                    pedido.getEstado().name()
            );

            // Guardar el total calculado del pedido.

            ps.setBigDecimal(
                    5,
                    pedido.calcularTotal()
            );

            ps.executeUpdate();

            return pedido.getId();

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error al insertar el pedido.", e
            );
        }
    }

    // =====================================================
    // INSERTAR DETALLES
    // =====================================================

    private void insertarDetalles(Pedido pedido)
            throws PersistenciaException {

        if (pedido.getDetalles() == null
                || pedido.getDetalles().isEmpty()) {

            return;
        }

        try (PreparedStatement ps =
                     conexion.prepareStatement(
                             SQL_INSERTAR_DETALLE)) {

            for (DetallePedido detalle :
                    pedido.getDetalles()) {

                if (detalle.getId() == null
                        || detalle.getId().isBlank()) {

                    detalle.setId(
                            UUID.randomUUID().toString()
                    );
                }

                ps.setString(1, detalle.getId());

                ps.setString(2, pedido.getId());

                ps.setString(
                        3,
                        detalle.getPresentacion().getId()
                );

                ps.setInt(
                        4,
                        detalle.getCantidadUnidades()
                );

                ps.setBigDecimal(
                        5,
                        detalle.getPrecioUnitario()
                );

                ps.setInt(
                        6,
                        detalle.getCantidadCajones()
                );

                if (detalle.getTipoCajon() != null
                        && detalle.getTipoCajon().getId() != null
                        && !detalle.getTipoCajon()
                        .getId().isBlank()) {

                    ps.setString(
                            7,
                            detalle.getTipoCajon().getId()
                    );

                } else {

                    ps.setNull(7, Types.VARCHAR);
                }

                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error al insertar los detalles "
                            + "del pedido: " + pedido.getId(),
                    e
            );
        }
    }

    // =====================================================
    // BUSCAR PEDIDO
    // =====================================================

    @Override
    public Pedido buscarPorId(String id)
            throws PersistenciaException {

        try (PreparedStatement ps =
                     conexion.prepareStatement(
                             SQL_BUSCAR_POR_ID)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Pedido pedido = mapear(rs);

                    cargarDetallesDePedido(pedido);

                    return pedido;
                }
            }

            return null;

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error al buscar el pedido: " + id,
                    e
            );
        }
    }

    // =====================================================
    // LISTAR PEDIDOS
    // =====================================================

    @Override
    public List<Pedido> listar()
            throws PersistenciaException {

        List<Pedido> pedidos = new ArrayList<>();

        try (PreparedStatement ps =
                     conexion.prepareStatement(SQL_LISTAR);

             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Pedido pedido = mapear(rs);

                cargarDetallesDePedido(pedido);

                pedidos.add(pedido);
            }

            return pedidos;

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error al listar los pedidos.", e
            );
        }
    }

    // =====================================================
    // ACTUALIZAR PEDIDO
    // =====================================================

    @Override
    public void actualizar(Pedido pedido)
            throws PersistenciaException {

        if (pedido == null
                || pedido.getId() == null
                || pedido.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar un pedido válido."
            );
        }

        try (TransaccionJdbc tx =
                     new TransaccionJdbc(conexion)) {

            verificarEstadoParaActualizar(pedido);

            actualizarPedido(pedido);

            eliminarDetallesPorPedido(pedido.getId());

            insertarDetalles(pedido);

            tx.confirmar();

        } catch (SQLException | RuntimeException e) {

            throw new PersistenciaException(
                    "Error al actualizar el pedido: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private void verificarEstadoParaActualizar(Pedido pedido)
            throws SQLException {

        String sql =
                "SELECT estado FROM pedidos "
                        + "WHERE id = ? FOR UPDATE";

        try (PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setString(1, pedido.getId());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new PersistenciaException(
                            "El pedido no existe."
                    );
                }

                EstadoPedido estadoActual =
                        EstadoPedido.valueOf(
                                rs.getString("estado")
                        );

                if (estadoActual != EstadoPedido.BORRADOR) {

                    throw new PersistenciaException(
                            "Solo se pueden editar pedidos "
                                    + "en estado BORRADOR."
                    );
                }

                if (pedido.getEstado() != EstadoPedido.BORRADOR
                        && pedido.getEstado()
                        != EstadoPedido.CONFIRMADO) {

                    throw new PersistenciaException(
                            "La transición de estado no es válida."
                    );
                }

                if (pedido.getEstado()
                        == EstadoPedido.CONFIRMADO
                        && pedido.getDetalles().isEmpty()) {

                    throw new PersistenciaException(
                            "No se puede confirmar un pedido "
                                    + "sin detalles."
                    );
                }
            }
        }
    }

    private void actualizarPedido(Pedido pedido)
            throws SQLException {

        try (PreparedStatement ps =
                     conexion.prepareStatement(
                             SQL_ACTUALIZAR_PEDIDO)) {

            ps.setString(
                    1,
                    pedido.getCliente().getId()
            );

            ps.setTimestamp(
                    2,
                    Timestamp.valueOf(pedido.getFecha())
            );

            ps.setString(
                    3,
                    pedido.getEstado().name()
            );

            // Actualizar el total calculado del pedido.

            ps.setBigDecimal(
                    4,
                    pedido.calcularTotal()
            );

            ps.setString(5, pedido.getId());

            ps.executeUpdate();
        }
    }

    private void eliminarDetallesPorPedido(String pedidoId)
            throws SQLException {

        try (PreparedStatement ps =
                     conexion.prepareStatement(
                             SQL_ELIMINAR_DETALLES)) {

            ps.setString(1, pedidoId);

            ps.executeUpdate();
        }
    }

    // =====================================================
    // CARGAR DETALLES
    // =====================================================

    private void cargarDetallesDePedido(Pedido pedido)
            throws PersistenciaException {

        try (PreparedStatement ps =
                     conexion.prepareStatement(
                             SQL_BUSCAR_DETALLES)) {

            ps.setString(1, pedido.getId());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    pedido.agregarDetalle(
                            mapearDetalle(rs)
                    );
                }
            }

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error al cargar los detalles "
                            + "del pedido: " + pedido.getId(),
                    e
            );
        }
    }

    // =====================================================
    // CANCELACIÓN Y BAJA LÓGICA
    // =====================================================

    @Override
    public void eliminar(String id)
            throws PersistenciaException {

        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException(
                    "El ID del pedido es obligatorio."
            );
        }

        try (TransaccionJdbc tx =
                     new TransaccionJdbc(conexion)) {

            try (PreparedStatement bloqueo =
                         conexion.prepareStatement(
                                 "SELECT estado FROM pedidos "
                                         + "WHERE id = ? FOR UPDATE")) {

                bloqueo.setString(1, id);

                try (ResultSet rs = bloqueo.executeQuery()) {

                    if (!rs.next()) {

                        throw new PersistenciaException(
                                "El pedido no existe."
                        );
                    }
                }
            }

            try (PreparedStatement ps =
                         conexion.prepareStatement(
                                 SQL_CANCELAR)) {

                ps.setString(
                        1,
                        EstadoPedido.CANCELADO.name()
                );

                ps.setString(2, id);

                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas == 0) {

                    throw new PersistenciaException(
                            "Solo se pueden cancelar pedidos "
                                    + "en BORRADOR o CONFIRMADO "
                                    + "que no tengan retiros registrados."
                    );
                }
            }

            tx.confirmar();

        } catch (SQLException e) {

            throw new PersistenciaException(
                    "Error al cancelar el pedido: " + id,
                    e
            );
        }
    }

    @Override
    public void desactivar(String id) {

        eliminar(id);
    }
}