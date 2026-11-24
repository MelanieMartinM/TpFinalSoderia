package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.TipoMovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.MovimientoCajon;
import ar.edu.utn.tpfinalsoderia.models.MovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.StockCajon;
import ar.edu.utn.tpfinalsoderia.models.StockEnvase;
import ar.edu.utn.tpfinalsoderia.models.StockProducto;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventarioRepositoryJdbc {

    private final Connection conexion;

    public InventarioRepositoryJdbc(Connection conexion) {
        this.conexion = Objects.requireNonNull(conexion, "La conexión no puede ser nula.");
    }

    public Connection getConexion() { return conexion; }

    private static String titularId(Cliente titular) {
        if (titular == null || titular.getId() == null || titular.getId().isBlank())
            throw new IllegalArgumentException("Debe indicar el repartidor titular.");
        return titular.getId();
    }

    public StockProducto buscarStockProducto(Cliente titular, Presentacion presentacion) {
        String clienteId = titularId(titular);
        Objects.requireNonNull(presentacion, "La presentación es obligatoria.");
        asegurarFila("stock_producto", "presentacion_id", clienteId, presentacion.getId());
        String sql = "SELECT disponibles FROM stock_producto WHERE cliente_id = ? AND presentacion_id = ? FOR UPDATE";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, clienteId); ps.setString(2, presentacion.getId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new StockProducto(titular, presentacion, rs.getInt(1)) : null;
            }
        } catch (SQLException e) { throw new PersistenciaException("Error al consultar stock producto.", e); }
    }

    public void guardarStockProducto(StockProducto stock) {
        String sql = "INSERT INTO stock_producto (cliente_id,presentacion_id,disponibles) VALUES (?,?,?) ON DUPLICATE KEY UPDATE disponibles=VALUES(disponibles)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, titularId(stock.getTitular())); ps.setString(2, stock.getPresentacion().getId());
            ps.setInt(3, stock.getDisponibles()); ps.executeUpdate();
        } catch (SQLException e) { throw new PersistenciaException("Error al guardar stock producto.", e); }
    }

    public StockEnvase buscarStockEnvase(Cliente titular, Envase envase) {
        String clienteId = titularId(titular);
        Objects.requireNonNull(envase, "El envase es obligatorio.");
        asegurarFila("stock_envase", "envase_id", clienteId, envase.getId());
        String sql = "SELECT aptos,sucios FROM stock_envase WHERE cliente_id=? AND envase_id=? FOR UPDATE";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, clienteId); ps.setString(2, envase.getId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new StockEnvase(titular, envase, rs.getInt(1), rs.getInt(2)) : null;
            }
        } catch (SQLException e) { throw new PersistenciaException("Error al consultar stock envase.", e); }
    }

    public void guardarStockEnvase(StockEnvase stock) {
        String sql = "INSERT INTO stock_envase (cliente_id,envase_id,aptos,sucios) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE aptos=VALUES(aptos),sucios=VALUES(sucios)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, titularId(stock.getTitular())); ps.setString(2, stock.getEnvase().getId());
            ps.setInt(3, stock.getAptos()); ps.setInt(4, stock.getSucios()); ps.executeUpdate();
        } catch (SQLException e) { throw new PersistenciaException("Error al guardar stock envase.", e); }
    }

    public void registrarMovimientoEnvase(MovimientoEnvase movimiento) {
        Objects.requireNonNull(movimiento, "El movimiento de envase no puede ser nulo.");
        Objects.requireNonNull(movimiento.getEnvase(), "El envase del movimiento no puede ser nulo.");
        Objects.requireNonNull(movimiento.getTipo(), "El tipo de movimiento no puede ser nulo.");

        String sql = "INSERT INTO movimiento_envase (id, cliente_id, envase_id, tipo, cantidad, fecha) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, movimiento.getId());
            ps.setString(2, titularId(movimiento.getTitular()));
            ps.setString(3, movimiento.getEnvase().getId());
            ps.setString(4, movimiento.getTipo().name());
            ps.setInt(5, movimiento.getCantidad());
            ps.setTimestamp(6, movimiento.getFecha() != null ? Timestamp.valueOf(movimiento.getFecha()) : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar movimiento de envase.", e);
        }
    }

    public void guardarStockCajon(StockCajon stock) {
        String sql = "INSERT INTO stock_cajon (cliente_id,tipo_cajon_id,disponibles) VALUES (?,?,?) ON DUPLICATE KEY UPDATE disponibles=VALUES(disponibles)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, titularId(stock.getTitular())); ps.setString(2, stock.getTipo().getId());
            ps.setInt(3, stock.getDisponibles()); ps.executeUpdate();
        } catch (SQLException e) { throw new PersistenciaException("Error al guardar stock cajón.", e); }
    }

    public void registrarMovimientoCajon(MovimientoCajon movimiento) {
        Objects.requireNonNull(movimiento, "El movimiento de cajón no puede ser nulo.");
        Objects.requireNonNull(movimiento.getTipoCajon(), "El tipo de cajón del movimiento no puede ser nulo.");

        String sql = "INSERT INTO movimiento_cajon (id, cliente_id, tipo_cajon_id, cantidad, fecha, es_retorno) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, movimiento.getId());
            ps.setString(2, titularId(movimiento.getCliente()));
            ps.setString(3, movimiento.getTipoCajon().getId());
            ps.setInt(4, movimiento.getCantidad());
            ps.setTimestamp(5, movimiento.getFecha() != null ? Timestamp.valueOf(movimiento.getFecha()) : null);
            ps.setBoolean(6, movimiento.isEsRetorno());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar movimiento de cajón.", e);
        }
    }

    public List<StockEnvase> listarStockEnvases() {
        List<StockEnvase> lista = new ArrayList<>();
        String sql = """
                SELECT se.cliente_id, c.nombre AS cliente_nombre,
                       se.envase_id, e.descripcion AS envase_desc,
                       se.aptos, se.sucios
                FROM stock_envase se
                LEFT JOIN clientes c ON se.cliente_id = c.id
                LEFT JOIN envases e ON se.envase_id = e.id
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente titular = null;
                String cliId = rs.getString("cliente_id");
                if (cliId != null && !cliId.isBlank()) {
                    titular = new Cliente();
                    titular.setId(cliId);
                    titular.setNombre(rs.getString("cliente_nombre"));
                }

                Envase envase = new Envase();
                envase.setId(rs.getString("envase_id"));
                envase.setDescripcion(rs.getString("envase_desc"));

                lista.add(new StockEnvase(
                        titular,
                        envase,
                        rs.getInt("aptos"),
                        rs.getInt("sucios")
                ));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar stock de envases.", e);
        }
    }

    public List<StockProducto> listarStockProductos() {
        List<StockProducto> lista = new ArrayList<>();
        String sql = """
                SELECT sp.cliente_id, c.nombre AS cliente_nombre,
                       sp.presentacion_id, p.nombre AS pres_nombre,
                       sp.disponibles
                FROM stock_producto sp
                LEFT JOIN clientes c ON sp.cliente_id = c.id
                LEFT JOIN presentaciones p ON sp.presentacion_id = p.id
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente titular = null;
                String cliId = rs.getString("cliente_id");
                if (cliId != null && !cliId.isBlank()) {
                    titular = new Cliente();
                    titular.setId(cliId);
                    titular.setNombre(rs.getString("cliente_nombre"));
                }

                Presentacion pres = new Presentacion();
                pres.setId(rs.getString("presentacion_id"));
                pres.setNombre(rs.getString("pres_nombre"));

                lista.add(new StockProducto(
                        titular,
                        pres,
                        rs.getInt("disponibles")
                ));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar stock de productos.", e);
        }
    }

    public List<MovimientoEnvase> listarMovimientosEnvases() {
        List<MovimientoEnvase> lista = new ArrayList<>();
        String sql = """
                SELECT me.id, me.cliente_id, c.nombre AS cliente_nombre,
                       me.envase_id, e.descripcion AS envase_desc,
                       me.tipo, me.cantidad, me.fecha
                FROM movimiento_envase me
                LEFT JOIN clientes c ON me.cliente_id = c.id
                LEFT JOIN envases e ON me.envase_id = e.id
                ORDER BY me.fecha DESC
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente titular = null;
                String cliId = rs.getString("cliente_id");
                if (cliId != null && !cliId.isBlank()) {
                    titular = new Cliente();
                    titular.setId(cliId);
                    titular.setNombre(rs.getString("cliente_nombre"));
                }

                Envase envase = new Envase();
                envase.setId(rs.getString("envase_id"));
                envase.setDescripcion(rs.getString("envase_desc"));

                String tipoStr = rs.getString("tipo");
                TipoMovimientoEnvase tipo = (tipoStr != null && !tipoStr.isBlank())
                        ? TipoMovimientoEnvase.valueOf(tipoStr)
                        : null;

                Timestamp ts = rs.getTimestamp("fecha");
                LocalDateTime fecha = (ts != null) ? ts.toLocalDateTime() : null;

                lista.add(new MovimientoEnvase(
                        rs.getString("id"),
                        titular,
                        envase,
                        tipo,
                        rs.getInt("cantidad"),
                        fecha
                ));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar movimientos de envases.", e);
        }
    }

    public List<StockCajon> listarStockCajones() {
        List<StockCajon> lista = new ArrayList<>();
        String sql = """
                SELECT sc.cliente_id, c.nombre AS cliente_nombre, sc.tipo_cajon_id, tc.descripcion AS cajon_desc, tc.capacidad,
                       sc.disponibles
                FROM stock_cajon sc
                LEFT JOIN clientes c ON c.id = sc.cliente_id
                LEFT JOIN tipos_cajon tc ON sc.tipo_cajon_id = tc.id
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TipoCajon tipo = new TipoCajon();
                tipo.setId(rs.getString("tipo_cajon_id"));
                tipo.setDescripcion(rs.getString("cajon_desc"));
                tipo.setCapacidad(rs.getInt("capacidad"));

                Cliente titular = new Cliente();
                titular.setId(rs.getString("cliente_id"));
                titular.setNombre(rs.getString("cliente_nombre"));
                lista.add(new StockCajon(titular, tipo, rs.getInt("disponibles")));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar stock de cajones.", e);
        }
    }

    public List<MovimientoCajon> listarMovimientosCajones() {
        List<MovimientoCajon> lista = new ArrayList<>();
        String sql = """
                SELECT mc.id, mc.cliente_id, c.nombre AS cliente_nombre,
                       mc.tipo_cajon_id, tc.descripcion AS cajon_desc, tc.capacidad,
                       mc.cantidad, mc.fecha, mc.es_retorno
                FROM movimiento_cajon mc
                LEFT JOIN clientes c ON mc.cliente_id = c.id
                LEFT JOIN tipos_cajon tc ON mc.tipo_cajon_id = tc.id
                ORDER BY mc.fecha DESC
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente cliente = null;
                String cliId = rs.getString("cliente_id");
                if (cliId != null && !cliId.isBlank()) {
                    cliente = new Cliente();
                    cliente.setId(cliId);
                    cliente.setNombre(rs.getString("cliente_nombre"));
                }

                TipoCajon tipo = new TipoCajon();
                tipo.setId(rs.getString("tipo_cajon_id"));
                tipo.setDescripcion(rs.getString("cajon_desc"));
                tipo.setCapacidad(rs.getInt("capacidad"));

                Timestamp ts = rs.getTimestamp("fecha");
                LocalDateTime fecha = (ts != null) ? ts.toLocalDateTime() : null;

                lista.add(new MovimientoCajon(
                        rs.getString("id"),
                        cliente,
                        tipo,
                        rs.getInt("cantidad"),
                        fecha,
                        rs.getBoolean("es_retorno")
                ));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar movimientos de cajones.", e);
        }
    }

    public int saldoEnvases(Cliente titular, Envase envase) {
        StockEnvase stock = buscarStockEnvase(titular, envase);
        return stock == null ? 0 : stock.getAptos() + stock.getSucios();
    }

    public int saldoCajones(Cliente titular, TipoCajon tipo) {
        asegurarFila("stock_cajon", "tipo_cajon_id", titularId(titular), tipo.getId());
        String sql = "SELECT disponibles FROM stock_cajon WHERE cliente_id=? AND tipo_cajon_id=? FOR UPDATE";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, titularId(titular)); ps.setString(2, tipo.getId());
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new PersistenciaException("Error al consultar saldo de cajones.", e); }
    }

    /** En una transacción crea o bloquea la fila antes de leer su saldo. */
    private void asegurarFila(String tabla, String columna, String clienteId, String tipoId) {
        if (tipoId == null || tipoId.isBlank()) throw new IllegalArgumentException("Falta identificar el tipo.");
        try {
            if (conexion.getAutoCommit()) return;
            // Los nombres son constantes internas del repositorio; los valores usan parámetros.
            String sql = "INSERT INTO " + tabla + " (cliente_id," + columna + ") VALUES (?,?) " +
                    "ON DUPLICATE KEY UPDATE cliente_id=VALUES(cliente_id)";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, clienteId);
                ps.setString(2, tipoId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al asegurar la fila de stock.", e);
        }
    }
}
