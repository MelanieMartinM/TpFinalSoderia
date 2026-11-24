package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.EstadoPedido;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.*;

import java.sql.*;
import java.util.List;


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
            "INSERT INTO pedidos (cliente_id, fecha, estado) VALUES (?, ?, ?)";
    private static final String SQL_INSERTAR_DETALLE =
            "INSERT INTO detalle_pedido (pedido_id, presentacion_id, cantidad_unidades, precio_unitario_pactado, cantidad_cajones, tipo_cajon_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR_PEDIDO =
            "UPDATE pedidos SET cliente_id = ?, fecha = ?, estado = ? WHERE id = ?";
    private static final String SQL_ELIMINAR_DETALLES =
            "DELETE FROM detalle_pedido WHERE pedido_id = ?";
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, cliente_id, fecha, estado FROM pedidos WHERE id = ?";
    private static final String SQL_BUSCAR_DETALLES =
            "SELECT presentacion_id, cantidad_unidades, precio_unitario_pactado, cantidad_cajones, tipo_cajon_id " +
                    "FROM detalle_pedido WHERE pedido_id = ?";
    private static final String SQL_LISTAR =
            "SELECT id, cliente_id, fecha, estado FROM pedidos";
    private static final String SQL_CANCELAR =
            "UPDATE pedidos SET estado = ? WHERE id = ?";





    public PedidoRepositoryJdbc(Connection conexion) throws PersistenciaException{
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
    public void guardar(Pedido pedido) throws PersistenciaException{
        try{
            conexion.setAutoCommit(false);//desactiva el autoguardado
            String idPedido=  InsertarPedido(pedido);
            pedido.setId(idPedido);
            insertarDetalles(pedido);

            conexion.commit();

        }catch(SQLException |PersistenciaException e){
            revertirTransaccion();//rollback si hay error
            throw new PersistenciaException("Error al guardar el pedido id: " + pedido.getId(),e);

        }finally {
            restaurarAutocommit();
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
        } catch (NumberFormatException e) {
            throw new PersistenciaException("El id del pedido no es un número válido: " + id, e);
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
        try {
            conexion.setAutoCommit(false);
            actualizarPedido(pedido);
            eliminarDetallesPorPedido(pedido.getId());
            insertarDetalles(pedido);
            conexion.commit();
        } catch (SQLException | PersistenciaException e) {
            revertirTransaccion();
            throw new PersistenciaException("Error al actualizar el pedido id: " + pedido.getId(), e);
        } finally {
            restaurarAutocommit();
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
        } catch (NumberFormatException e) {
            throw new PersistenciaException("El id del pedido no es un número válido: " + id, e);
        }
    }
    /// insertar,actualizar,eliminar,cargarDetallesDePedido

    private String InsertarPedido(Pedido pedido) throws PersistenciaException{
        try(PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR_PEDIDO, Statement.RETURN_GENERATED_KEYS)) { //retorna el id del pedido generado

            ps.setString(1,pedido.getCliente().getId());
            ps.setTimestamp(2,Timestamp.valueOf(pedido.getFecha()));
            ps.setString (3 , pedido.getEstado().name());
            ps.executeUpdate();

            try(ResultSet keys = ps.getGeneratedKeys()){ /// devuelve el ID generado
                if(keys.next()){
                    return String.valueOf(keys.getInt(1));
                }
            } throw new PersistenciaException("No se pudo obtener el ID generado para el pedido");
        } catch (SQLException e) {
            throw new PersistenciaException("Error,Al obtener el ID generado para el pedido.",e);
        }
    }

    private void insertarDetalles(Pedido pedido) throws PersistenciaException {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            return;
        }

        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR_DETALLE)) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                ps.setString(1, pedido.getId());
                ps.setInt(2, detalle.getPresentacion().getId());
                ps.setInt(3, detalle.getCantidadUnidades());
                ps.setBigDecimal(4, detalle.getPrecioUnitario());
                ps.setInt(5, detalle.getCantidadCajones());

                if (detalle.getTipoCajon() != null && detalle.getTipoCajon().getId() !=null) {
                    ps.setString(6, detalle.getTipoCajon().getId());
                } else {
                    ps.setNull(6, Types.INTEGER);
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









    private void revertirTransaccion() throws PersistenciaException{
        if (conexion!= null){
            try{
                conexion.rollback();//deshace todos los cambios hechos en la DB desde que inicio esta transaccion
            } catch (SQLException e) {
                throw new PersistenciaException("Error. al revertir la transaccion ",e);
            }
        }
    }


    private  void restaurarAutocommit() throws PersistenciaException{
        if(conexion != null){
            try{
                conexion.setAutoCommit(true);//activa el autoguardado
            } catch (SQLException e) {
                throw new PersistenciaException("Error. Al reiniciar el Autocommit",e);
            }
        }

    }





}