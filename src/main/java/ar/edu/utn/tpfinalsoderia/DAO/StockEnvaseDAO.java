package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.StockEnvase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockEnvaseDAO extends RepositorioJdbc<StockEnvase>
{
    public StockEnvaseDAO(Connection conexion)
    {
        super(conexion);
    }

    @Override
    public void guardar(StockEnvase stock)
    {
        String sql = "INSERT INTO stock_envase (cliente_id, envase_id, aptos, sucios) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            if (stock.getTitular() != null)
            {
                ps.setString(1, stock.getTitular().getId());
            } else
            {
                ps.setNull(1, Types.VARCHAR);
            }

            ps.setString(2, stock.getEnvase().getId());
            ps.setInt(3, stock.getAptos());
            ps.setInt(4, stock.getSucios());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys())
            {
                if (rs.next())
                {
                    stock.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al guardar stock de envase", e);
        }
    }

    @Override
    public StockEnvase buscarPorId(String id)
    {
        String sql = "SELECT * FROM stock_envase WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return mapear(rs);
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al buscar stock de envase por id", e);
        }

        return null;
    }

    public StockEnvase buscarPorTitularYEnvase(String clienteId, String envaseId)
    {
        String sql = (clienteId == null)
                ? "SELECT * FROM stock_envase WHERE cliente_id IS NULL AND envase_id = ?"
                : "SELECT * FROM stock_envase WHERE cliente_id = ? AND envase_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            if (clienteId == null)
            {
                ps.setString(1, envaseId);
            } else
            {
                ps.setString(1, clienteId);
                ps.setString(2, envaseId);
            }
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al buscar stock de envase por titular y envase", e);
        }

        return null;
    }

    @Override
    public List<StockEnvase> listar()
    {
        List<StockEnvase> lista = new ArrayList<>();

        String sql = "SELECT * FROM stock_envase";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                lista.add(mapear(rs));
            }
        }
        catch (SQLException e)
        {
            throw new PersistenciaException("Error al listar stock de envases", e);
        }
        return lista;
    }

    @Override
    public void actualizar(StockEnvase stock)
    {
        String sql = "UPDATE stock_envase SET aptos = ?, sucios = ? WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setInt(1, stock.getAptos());
            ps.setInt(2, stock.getSucios());
            ps.setInt(3, stock.getId());
            ps.executeUpdate();
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al actualizar stock de envase", e);
        }
    }

    @Override
    public void eliminar(String id)
    {
        String sql = "DELETE FROM stock_envase WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setInt(1, Integer.parseInt(id));
            ps.executeUpdate();
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al eliminar stock de envase", e);
        }
    }

    @Override
    protected StockEnvase mapear(ResultSet rs) throws SQLException
    {
        StockEnvase stock = new StockEnvase();

        stock.setId(rs.getInt("id"));
        stock.setAptos(rs.getInt("aptos"));
        stock.setSucios(rs.getInt("sucios"));

        String clienteId = rs.getString("cliente_id");
        if (clienteId != null)
        {
            Cliente cliente = new Cliente("", clienteId, "", "", true);
            stock.setTitular(cliente);
        }

        Envase envase = new Envase();
        envase.setId(rs.getString("envase_id"));
        stock.setEnvase(envase);

        return stock;
    }
}