package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.EstadoPedido;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.*;

import java.sql.*;
import java.util.List;
import java.util.UUID;


/// +-------------------------------------------------------------------+
/// |                       PedidoRepositoryJdbc                        |
/// +-------------------------------------------------------------------+
/// |                                                                   |
/// |  [1.PEDIDO-tabla pedidos]                                      |
/// |   - guardar(Pedido)      --> INSERT pedidos -> toma ID generado   |
/// |   - buscarPorId(id)      --> SELECT pedidos                       |
/// |   - listar()             --> SELECT pedidos                       |
/// |   - actualizar(Pedido)   --> UPDATE pedidos                       |
/// |   - eliminar(id)         --> UPDATE estado = CANCELADO            |
/// |                                                                   |
/// |  [2.DETALLES-tabla detalle_pedido]                             |
/// |   - insertarDetalles()   --> INSERT por cada DetallePedido        |
/// |   - cargarDetalles()     --> SELECT detalles WHERE pedido_id = ?  |
/// |                                                                   |
/// |  [3. SOPORTE /// TRANSACCIONES]                                     |
/// |   - mapear(ResultSet)          --> fila pedidos  -> Pedido        |
/// |   - mapearDetalle(ResultSet)   --> fila detalle  -> DetallePedido |
/// |   - rollbackSilencioso()       --> deshace si falla algo          |
/// +-------------------------------------------------------------------+






public class PedidoRepositoryJdbc extends RepositorioJdbc<Pedido> {




    private static final String SQL_INSERTAR_PEDIDO =
            "INSERT INTO pedidos (id, cliente_id, fecha, estado) VALUES (?, ?, ?, ?)";
    private static final String SQL_INSERTAR_DETALLE =
            "INSERT INTO detalle_pedido (id, pedido_id, presentacion_id, cantidad_unidades, precio_unitario_pactado, cantidad_cajones, tipo_cajon_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR_PEDIDO =
            "UPDATE pedidos SET cliente_id = ?, fecha = ?, estado = ? WHERE id = ?";
    private static final String SQL_ELIMINAR_DETALLES =
            "DELETE FROM detalle_pedido WHERE pedido_id = ?";
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, cliente_id, fecha, estado FROM pedidos WHERE id = ?";
    private static final String SQL_BUSCAR_DETALLES =
            "SELECT id, presentacion_id, cantidad_unidades, precio_unitario_pactado, cantidad_cajones, tipo_cajon_id " +
                    "FROM detalle_pedido WHERE pedido_id = ?";
    private static final String SQL_LISTAR =
            "SELECT id, cliente_id, fecha, estado FROM pedidos";
    private static final String SQL_CANCELAR =
            "UPDATE pedidos SET estado = ? WHERE id = ?";





    public PedidoRepositoryJdbc() throws PersistenciaException{
        super(null);
        conectar();
    }

    private void conectar() throws PersistenciaException{
        try {
            this.conexion=ConexionFactory.abrirConexion();
        } catch (SQLException e) {
            throw new PersistenciaException("Error.No se puedo conectar a la base de datos",e);
        }

    }



    @Override
    protected Pedido mapear(ResultSet resultado) throws SQLException {
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
            pedido.setEstado(EstadoPedido.valueOf(estadoStr));
        }

        return pedido;
    }

    private DetallePedido mapearDetalle(ResultSet rs) throws SQLException {
        DetallePedido detalle = new DetallePedido();
        detalle.setId(rs.getString("id"));
        detalle.setCantidadUnidades(rs.getInt("cantidad_unidades"));
        detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario_pactado"));
        detalle.setCantidadCajones(rs.getInt("cantidad_cajones"));

        Presentacion presentacion = new Presentacion();
        presentacion.setId(rs.getString("presentacion_id"));
        detalle.setPresentacion(presentacion);

        String tipoCajonId = rs.getString("tipo_cajon_id");
        if (!rs.wasNull()) {
            TipoCajon tipoCajon = new TipoCajon();
            tipoCajon.setId(tipoCajonId);
            detalle.setTipoCajon(tipoCajon);
        }

        return detalle;
    }

    @Override
    public void guardar(Pedido pedido) throws PersistenciaException {
        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {
            InsertarPedido(pedido);
            insertarDetalles(pedido);
            tx.confirmar();
        } catch (SQLException | RuntimeException e) {
            throw new PersistenciaException("Error al guardar el pedido.", e);
        }
    }

    @Override
    public Pedido buscarPorId(String id) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
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
            throw new PersistenciaException("Error al buscar el pedido id: " + id, e);
        }
    }

    @Override
    public List<Pedido> listar() throws PersistenciaException {
        List<Pedido> pedidos = new java.util.ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pedido pedido = mapear(rs);
                cargarDetallesDePedido(pedido);
                pedidos.add(pedido);
            }
            return pedidos;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar los pedidos.", e);
        }
    }

    @Override
    public void actualizar(Pedido pedido) throws PersistenciaException {
        try (TransaccionJdbc tx = new TransaccionJdbc(conexion)) {
            try (PreparedStatement ps = conexion.prepareStatement(
                    "SELECT estado FROM pedidos WHERE id = ? FOR UPDATE")) {
                ps.setString(1, pedido.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new PersistenciaException("El pedido no existe.");
                    if (!EstadoPedido.BORRADOR.name().equals(rs.getString(1)))
                        throw new PersistenciaException("Solo se pueden editar pedidos en borrador.");
                }
            }
            actualizarPedido(pedido);
            eliminarDetallesPorPedido(pedido.getId());
            insertarDetalles(pedido);
            tx.confirmar();
        } catch (SQLException | RuntimeException e) {
            throw new PersistenciaException("Error al actualizar el pedido.", e);
        }
    }

    @Override
    public void eliminar(String id) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_CANCELAR)) {
            ps.setString(1, EstadoPedido.CANCELADO.name());
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cancelar el pedido id: " + id, e);
        }
    }
    /// insertar,actualizar,eliminar,cargarDetallesDePedido

    private String InsertarPedido(Pedido pedido) throws PersistenciaException {
        if (pedido.getId() == null || pedido.getId().isBlank()) {
            pedido.setId(UUID.randomUUID().toString());
        }
        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR_PEDIDO)) {
            ps.setString(1, pedido.getId());
            ps.setString(2, pedido.getCliente().getId());
            ps.setTimestamp(3, Timestamp.valueOf(pedido.getFecha()));
            ps.setString(4, pedido.getEstado().name());
            ps.executeUpdate();
            return pedido.getId();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar el pedido.", e);
        }
    }

    private void insertarDetalles(Pedido pedido) throws PersistenciaException {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            return;
        }

        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR_DETALLE)) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                if (detalle.getId() == null || detalle.getId().isBlank()) {
                    detalle.setId(UUID.randomUUID().toString());
                }
                ps.setString(1, detalle.getId());
                ps.setString(2, pedido.getId());
                ps.setString(3, detalle.getPresentacion().getId());
                ps.setInt(4, detalle.getCantidadUnidades());
                ps.setBigDecimal(5, detalle.getPrecioUnitario());
                ps.setInt(6, detalle.getCantidadCajones());

                if (detalle.getTipoCajon() != null && detalle.getTipoCajon().getId() !=null) {
                    ps.setString(7, detalle.getTipoCajon().getId());
                } else {
                    ps.setNull(7, Types.VARCHAR );
                }

                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar los detalles del pedido id: " + pedido.getId(), e);
        }
    }

    private void actualizarPedido(Pedido pedido) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR_PEDIDO)) {
            ps.setString(1, pedido.getCliente().getId());
            ps.setTimestamp(2, Timestamp.valueOf(pedido.getFecha()));
            ps.setString(3, pedido.getEstado().name());
            ps.setString(4, pedido.getId());
            ps.executeUpdate();
        }
    }

    private void eliminarDetallesPorPedido(String pedidoId) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR_DETALLES)) {
            ps.setString(1, pedidoId);
            ps.executeUpdate();
        }
    }


    private void cargarDetallesDePedido(Pedido pedido) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_DETALLES)) {
            ps.setString(1, pedido.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedido.agregarDetalle(mapearDetalle(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cargar los detalles del pedido id: " + pedido.getId(), e);
        }
    }


    /// Mapeo y manejo de transacciones









    @Override
    public void desactivar(String id) {
        eliminar(id); // Para esta entidad, eliminar ya realiza la baja lógica.
    }
}
