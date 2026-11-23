package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.MaterialEnvase;
import ar.edu.utn.tpfinalsoderia.enums.TipoEnvase;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.StockEnvase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StockEnvaseDAO extends RepositorioJdbc<StockEnvase> {

    private static final String SELECT_BASE = """
            SELECT se.id, se.cliente_id, se.envase_id, se.aptos, se.sucios,
                   c.nombre AS cliente_nombre, c.telefono AS cliente_telefono, c.direccion AS cliente_direccion, c.activo AS cliente_activo,
                   e.descripcion AS envase_descripcion, e.tipo_envase, e.material, e.capacidad_lts
            FROM stock_envase se
            LEFT JOIN clientes c ON se.cliente_id = c.id
            INNER JOIN envases e ON se.envase_id = e.id
            """;

    public StockEnvaseDAO(Connection conexion) {
        super(conexion);
    }

    @Override
    public void guardar(StockEnvase stock) {
        if (stock.getId() == null || stock.getId().isBlank()) {
            stock.setId(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO stock_envase (id, cliente_id, envase_id, aptos, sucios) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, stock.getId());
            if (stock.getTitular() != null) {
                ps.setString(2, stock.getTitular().getId());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, stock.getEnvase().getId());
            ps.setInt(4, stock.getAptos());
            ps.setInt(5, stock.getSucios());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar stock de envase", e);
        }
    }

    @Override
    public StockEnvase buscarPorId(String id) {
        String sql = SELECT_BASE + " WHERE se.id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar stock de envase por id", e);
        }
        return null;
    }

    public StockEnvase buscarPorTitularYEnvase(String clienteId, String envaseId) {
        String sql = (clienteId == null)
                ? SELECT_BASE + " WHERE se.cliente_id IS NULL AND se.envase_id = ?"
                : SELECT_BASE + " WHERE se.cliente_id = ? AND se.envase_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (clienteId == null) {
                ps.setString(1, envaseId);
            } else {
                ps.setString(1, clienteId);
                ps.setString(2, envaseId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar stock de envase por titular y envase", e);
        }
        return null;
    }

    @Override
    public List<StockEnvase> listar() {
        List<StockEnvase> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar stock de envases", e);
        }
        return lista;
    }

    @Override
    public void actualizar(StockEnvase stock) {
        String sql = "UPDATE stock_envase SET aptos = ?, sucios = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, stock.getAptos());
            ps.setInt(2, stock.getSucios());
            ps.setString(3, stock.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar stock de envase", e);
        }
    }

    @Override
    public void eliminar(String id) {
        String sql = "DELETE FROM stock_envase WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar stock de envase", e);
        }
    }

    public Cliente buscarClientePorId(String clienteId) {
        String sql = "SELECT id, nombre, telefono, direccion, activo FROM clientes WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(
                            rs.getString("direccion"),
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getString("telefono"),
                            rs.getBoolean("activo")
                    );
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar existencia del cliente", e);
        }
        return null;
    }

    public Envase buscarEnvasePorId(String envaseId) {
        String sql = "SELECT id, descripcion, tipo_envase, material, capacidad_lts FROM envases WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, envaseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Envase envase = new Envase();
                    envase.setId(rs.getString("id"));
                    envase.setDescripcion(rs.getString("descripcion"));
                    String tipoStr = rs.getString("tipo_envase");
                    if (tipoStr != null) envase.setTipoEnvase(TipoEnvase.valueOf(tipoStr));
                    String matStr = rs.getString("material");
                    if (matStr != null) envase.setMaterial(MaterialEnvase.valueOf(matStr));
                    envase.setCapacidadLTS(rs.getDouble("capacidad_lts"));
                    return envase;
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar existencia del envase", e);
        }
        return null;
    }

    @Override
    protected StockEnvase mapear(ResultSet rs) throws SQLException {
        StockEnvase stock = new StockEnvase();
        stock.setId(rs.getString("id"));
        stock.setAptos(rs.getInt("aptos"));
        stock.setSucios(rs.getInt("sucios"));

        String clienteId = rs.getString("cliente_id");
        if (clienteId != null) {
            Cliente cliente = new Cliente(
                    rs.getString("cliente_direccion"),
                    clienteId,
                    rs.getString("cliente_nombre"),
                    rs.getString("cliente_telefono"),
                    rs.getBoolean("cliente_activo")
            );
            stock.setTitular(cliente);
        }

        Envase envase = new Envase();
        envase.setId(rs.getString("envase_id"));
        envase.setDescripcion(rs.getString("envase_descripcion"));
        String tipoStr = rs.getString("tipo_envase");
        if (tipoStr != null) envase.setTipoEnvase(TipoEnvase.valueOf(tipoStr));
        String matStr = rs.getString("material");
        if (matStr != null) envase.setMaterial(MaterialEnvase.valueOf(matStr));
        envase.setCapacidadLTS(rs.getDouble("capacidad_lts"));
        stock.setEnvase(envase);

        return stock;
    }
}