package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.TipoMovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.MovimientoEnvase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MovimientoEnvaseDAO extends RepositorioJdbc<MovimientoEnvase> {

    private static final String SELECT_BASE = """
            SELECT m.id, m.cliente_id, m.envase_id, m.tipo, m.cantidad, m.fecha,
                   c.nombre AS cliente_nombre, c.telefono AS cliente_telefono, c.direccion AS cliente_direccion, c.activo AS cliente_activo,
                   e.descripcion AS envase_descripcion
            FROM movimiento_envase m
            LEFT JOIN clientes c ON m.cliente_id = c.id
            INNER JOIN envases e ON m.envase_id = e.id
            """;

    public MovimientoEnvaseDAO(Connection conexion) {
        super(conexion);
    }

    @Override
    public void guardar(MovimientoEnvase mov) {
        if (mov.getId() == null || mov.getId().isBlank()) {
            mov.setId(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO movimiento_envase (id, cliente_id, envase_id, tipo, cantidad, fecha) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, mov.getId());
            if (mov.getTitular() != null) {
                ps.setString(2, mov.getTitular().getId());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, mov.getEnvase().getId());
            ps.setString(4, mov.getTipo().name());
            ps.setInt(5, mov.getCantidad());
            ps.setTimestamp(6, Timestamp.valueOf(mov.getFecha()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar movimiento de envase", e);
        }
    }

    @Override
    public MovimientoEnvase buscarPorId(String id) {
        String sql = SELECT_BASE + " WHERE m.id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar movimiento de envase por id", e);
        }
        return null;
    }

    @Override
    public List<MovimientoEnvase> listar() {
        List<MovimientoEnvase> lista = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY m.fecha DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar movimientos de envases", e);
        }
        return lista;
    }

    @Override
    public void actualizar(MovimientoEnvase mov) {
        throw new UnsupportedOperationException("Los movimientos históricos de inventario no se modifican.");
    }

    @Override
    public void eliminar(String id) {
        throw new UnsupportedOperationException("Los movimientos históricos de inventario no se eliminan.");
    }

    @Override
    protected MovimientoEnvase mapear(ResultSet rs) throws SQLException {
        MovimientoEnvase mov = new MovimientoEnvase();
        mov.setId(rs.getString("id"));
        mov.setTipo(TipoMovimientoEnvase.valueOf(rs.getString("tipo")));
        mov.setCantidad(rs.getInt("cantidad"));
        mov.setFecha(rs.getTimestamp("fecha").toLocalDateTime());

        String clienteId = rs.getString("cliente_id");
        if (clienteId != null) {
            mov.setTitular(new Cliente(
                    rs.getString("cliente_direccion"),
                    clienteId,
                    rs.getString("cliente_nombre"),
                    rs.getString("cliente_telefono"),
                    rs.getBoolean("cliente_activo")
            ));
        }

        Envase envase = new Envase();
        envase.setId(rs.getString("envase_id"));
        envase.setDescripcion(rs.getString("envase_descripcion"));
        mov.setEnvase(envase);

        return mov;
    }
}