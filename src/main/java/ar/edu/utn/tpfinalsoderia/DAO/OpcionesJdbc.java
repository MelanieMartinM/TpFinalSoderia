package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Carga las opciones visibles; la selección conserva el objeto y su ID internamente. */
public class OpcionesJdbc {
    private final Connection conexion;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public OpcionesJdbc(Connection conexion) {
        this.conexion = Objects.requireNonNull(conexion);
    }

    public static final class Opcion<T> {
        private final T valor;
        private final String etiqueta;
        public Opcion(T valor, String etiqueta) { this.valor = valor; this.etiqueta = etiqueta; }
        public T getValor() { return valor; }
        @Override public String toString() { return etiqueta; }
    }

    public static final class PedidoOpcion {
        public final String id;
        public PedidoOpcion(String id) { this.id = id; }
    }

    public static final class DetalleOpcion {
        public final String id;
        public final Presentacion presentacion;
        public final int pedida;
        public final int pendiente;
        public final BigDecimal precio;
        public DetalleOpcion(String id, Presentacion presentacion, int pedida, int pendiente, BigDecimal precio) {
            this.id = id; this.presentacion = presentacion; this.pedida = pedida;
            this.pendiente = pendiente; this.precio = precio;
        }
    }

    public static final class RetiroOpcion {
        public final String id;
        public final BigDecimal pendiente;
        public RetiroOpcion(String id, BigDecimal pendiente) { this.id = id; this.pendiente = pendiente; }
    }

    public List<Opcion<Cliente>> clientes() {
        String sql = "SELECT id,nombre,telefono,direccion FROM clientes WHERE activo=TRUE ORDER BY nombre,id";
        List<Opcion<Cliente>> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int numero = 0;
            while (rs.next()) {
                Cliente c = new Cliente(); c.setId(rs.getString("id")); c.setNombre(rs.getString("nombre"));
                c.setTelefono(rs.getString("telefono")); c.setDireccion(rs.getString("direccion"));
                String descripcion = c.getNombre();
                if (c.getTelefono() != null && !c.getTelefono().isBlank()) descripcion += " — " + c.getTelefono();
                if (c.getDireccion() != null && !c.getDireccion().isBlank()) descripcion += " — " + c.getDireccion();
                lista.add(new Opcion<>(c, (++numero) + ". " + descripcion));
            }
            return lista;
        } catch (SQLException e) { throw new PersistenciaException("No se pudieron cargar los repartidores.", e); }
    }

    public List<Opcion<Envase>> envases() {
        String sql = "SELECT id,descripcion,tipo_envase,material,capacidad_lts FROM envases ORDER BY descripcion,id";
        List<Opcion<Envase>> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int numero = 0;
            while (rs.next()) {
                Envase e = new Envase(); e.setId(rs.getString("id")); e.setDescripcion(rs.getString("descripcion"));
                String descripcion = texto(e.getDescripcion(), "Envase sin descripción");
                String material = rs.getString("material");
                if (material != null && !material.isBlank()) descripcion += " — " + material;
                BigDecimal capacidad = rs.getBigDecimal("capacidad_lts");
                if (capacidad != null) descripcion += " — " + capacidad.stripTrailingZeros().toPlainString() + " L";
                lista.add(new Opcion<>(e, (++numero) + ". " + descripcion));
            }
            return lista;
        } catch (SQLException e) { throw new PersistenciaException("No se pudieron cargar los tipos de envase.", e); }
    }

    public List<Opcion<Presentacion>> presentaciones() {
        String sql = "SELECT p.id,p.nombre,p.precio_llenado,m.nombre AS marca FROM presentaciones p " +
                "LEFT JOIN marcas m ON m.id=p.marca_id WHERE p.activa=TRUE ORDER BY p.nombre,p.id";
        List<Opcion<Presentacion>> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int numero = 0;
            while (rs.next()) {
                Presentacion p = new Presentacion(); p.setId(rs.getString("id"));
                p.setNombre(rs.getString("nombre")); p.setPrecioLlenado(rs.getBigDecimal("precio_llenado"));
                String marca = rs.getString("marca");
                lista.add(new Opcion<>(p, (++numero) + ". " + texto(p.getNombre(), "Presentación") +
                        (marca == null || marca.isBlank() ? "" : " — " + marca)));
            }
            return lista;
        } catch (SQLException e) { throw new PersistenciaException("No se pudieron cargar las presentaciones.", e); }
    }

    public List<Opcion<TipoCajon>> tiposCajon() {
        String sql = "SELECT id,codigo,descripcion,capacidad FROM tipos_cajon ORDER BY descripcion,id";
        List<Opcion<TipoCajon>> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int numero = 0;
            while (rs.next()) {
                TipoCajon t = new TipoCajon(rs.getString("id"), rs.getString("codigo"),
                        rs.getString("descripcion"), rs.getInt("capacidad"));
                lista.add(new Opcion<>(t, (++numero) + ". " + texto(t.getDescripcion(), "Cajón") +
                        " — capacidad " + t.getCapacidad()));
            }
            return lista;
        } catch (SQLException e) { throw new PersistenciaException("No se pudieron cargar los tipos de cajón.", e); }
    }

    public List<Opcion<PedidoOpcion>> pedidos(String clienteId) {
        String sql = "SELECT p.id,p.fecha,p.estado, " +
                "GROUP_CONCAT(DISTINCT pr.nombre ORDER BY pr.nombre SEPARATOR ', ') AS productos " +
                "FROM pedidos p LEFT JOIN detalle_pedido d ON d.pedido_id=p.id " +
                "LEFT JOIN presentaciones pr ON pr.id=d.presentacion_id " +
                "WHERE p.cliente_id=? AND p.estado IN ('CONFIRMADO','PARCIAL') " +
                "AND EXISTS (SELECT 1 FROM detalle_pedido dp WHERE dp.pedido_id=p.id " +
                "AND dp.cantidad_unidades > COALESCE((SELECT SUM(dr.cantidad_retirada) " +
                "FROM detalle_retiro dr WHERE dr.detalle_pedido_id=dp.id),0)) " +
                "GROUP BY p.id,p.fecha,p.estado ORDER BY p.fecha DESC,p.id";
        List<Opcion<PedidoOpcion>> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                int numero = 0;
                while (rs.next()) lista.add(new Opcion<>(new PedidoOpcion(rs.getString("id")),
                        "Pedido " + (++numero) + " — " + fecha(rs.getTimestamp("fecha")) + " — " +
                                texto(rs.getString("productos"), "sin productos") + " — " + rs.getString("estado")));
            }
            return lista;
        } catch (SQLException e) { throw new PersistenciaException("No se pudieron cargar los pedidos.", e); }
    }

    public List<Opcion<DetalleOpcion>> detalles(String pedidoId) {
        String sql = "SELECT d.id,d.presentacion_id,p.nombre,d.cantidad_unidades,d.precio_unitario_pactado," +
                "COALESCE(SUM(r.cantidad_retirada),0) AS retiradas FROM detalle_pedido d " +
                "JOIN presentaciones p ON p.id=d.presentacion_id " +
                "LEFT JOIN detalle_retiro r ON r.detalle_pedido_id=d.id WHERE d.pedido_id=? " +
                "GROUP BY d.id,d.presentacion_id,p.nombre,d.cantidad_unidades,d.precio_unitario_pactado ORDER BY p.nombre,d.id";
        List<Opcion<DetalleOpcion>> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                int numero = 0;
                while (rs.next()) {
                    int pedida = rs.getInt("cantidad_unidades");
                    int pendiente = pedida - rs.getInt("retiradas");
                    if (pendiente <= 0) continue;
                    Presentacion p = new Presentacion(); p.setId(rs.getString("presentacion_id")); p.setNombre(rs.getString("nombre"));
                    DetalleOpcion d = new DetalleOpcion(rs.getString("id"), p, pedida, pendiente, rs.getBigDecimal("precio_unitario_pactado"));
                    lista.add(new Opcion<>(d, (++numero) + ". " + p.getNombre() + " — pendientes: " + pendiente));
                }
            }
            return lista;
        } catch (SQLException e) { throw new PersistenciaException("No se pudieron cargar los detalles del pedido.", e); }
    }

    public List<Opcion<RetiroOpcion>> retirosPendientes(String clienteId) {
        String sql = "SELECT r.id,r.fecha,r.total,COALESCE(SUM(CASE WHEN c.sin_cobro_autorizado=FALSE THEN c.monto ELSE 0 END),0) AS pagado " +
                "FROM retiros r LEFT JOIN cobros c ON c.retiro_id=r.id WHERE r.cliente_id=? " +
                "GROUP BY r.id,r.fecha,r.total ORDER BY r.fecha DESC,r.id";
        List<Opcion<RetiroOpcion>> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                int numero = 0;
                while (rs.next()) {
                    BigDecimal pendiente = rs.getBigDecimal("total").subtract(rs.getBigDecimal("pagado"));
                    if (pendiente.signum() <= 0) continue;
                    lista.add(new Opcion<>(new RetiroOpcion(rs.getString("id"), pendiente),
                            "Retiro " + (++numero) + " — " + fecha(rs.getTimestamp("fecha")) + " — pendiente $" + pendiente.toPlainString()));
                }
            }
            return lista;
        } catch (SQLException e) { throw new PersistenciaException("No se pudieron cargar los retiros pendientes.", e); }
    }

    private static String texto(String s, String defecto) { return s == null || s.isBlank() ? defecto : s; }
    private static String fecha(Timestamp t) { return t == null ? "sin fecha" : t.toLocalDateTime().format(FECHA); }
}
