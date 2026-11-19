package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.StockProducto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockProductoDAO extends RepositorioJdbc<StockProducto>
{

    public StockProductoDAO(Connection conexion)
    {
        super(conexion);
    }

    @Override
    public void guardar(StockProducto stock)
    {
        String sql = "INSERT INTO stock_producto (cliente_id, presentacion_id, disponibles) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            if (stock.getTitular() != null)
            {
                ps.setString(1, stock.getTitular().getId());
            } else
            {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setInt(2, stock.getPresentacion().getId());
            ps.setInt(3, stock.getDisponibles());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys())
            {
                if (rs.next()) {
                    stock.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al guardar stock de producto", e);
        }
    }

    @Override
    public StockProducto buscarPorId(String id)
    {
        String sql = "SELECT * FROM stock_producto WHERE id = ?";

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
            throw new PersistenciaException("Error al buscar stock de producto por id", e);
        }
        return null;
    }

    public StockProducto buscarPorTitularYPresentacion(String clienteId, int presentacionId)
    {
        String sql = (clienteId == null)
                ? "SELECT * FROM stock_producto WHERE cliente_id IS NULL AND presentacion_id = ?"
                : "SELECT * FROM stock_producto WHERE cliente_id = ? AND presentacion_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            if (clienteId == null)
            {
                ps.setInt(1, presentacionId);
            } else
            {
                ps.setString(1, clienteId);
                ps.setInt(2, presentacionId);
            }
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return mapear(rs);
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al buscar stock de producto por titular y presentación", e);
        }

        return null;
    }

    @Override
    public List<StockProducto> listar()
    {
        List<StockProducto> lista = new ArrayList<>();

        String sql = "SELECT * FROM stock_producto";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                lista.add(mapear(rs));
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al listar stock de productos", e);
        }

        return lista;
    }

    @Override
    public void actualizar(StockProducto stock)
    {
        String sql = "UPDATE stock_producto SET disponibles = ? WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setInt(1, stock.getDisponibles());
            ps.setInt(2, stock.getId());
            ps.executeUpdate();
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al actualizar stock de producto", e);
        }
    }

    @Override
    public void eliminar(String id)
    {
        String sql = "DELETE FROM stock_producto WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setInt(1, Integer.parseInt(id));
            ps.executeUpdate();
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al eliminar stock de producto", e);
        }
    }

    @Override
    protected StockProducto mapear(ResultSet rs) throws SQLException
    {
        StockProducto stock = new StockProducto();

        stock.setId(rs.getInt("id"));
        stock.setDisponibles(rs.getInt("disponibles"));
        String clienteId = rs.getString("cliente_id");

        if (clienteId != null)
        {
            Cliente cliente = new Cliente("", clienteId, "", "", true);
            stock.setTitular(cliente);
        }

        Presentacion presentacion = new Presentacion();
        presentacion.setId(rs.getInt("presentacion_id"));
        stock.setPresentacion(presentacion);

        return stock;
    }
}