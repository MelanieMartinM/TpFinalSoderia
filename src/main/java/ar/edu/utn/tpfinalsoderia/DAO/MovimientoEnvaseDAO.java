package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.TipoMovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.MovimientoEnvase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoEnvaseDAO extends RepositorioJdbc<MovimientoEnvase>
{

    public MovimientoEnvaseDAO(Connection conexion)
    {
        super(conexion);
    }

    @Override
    public void guardar(MovimientoEnvase mov)
    {
        String sql = "INSERT INTO movimiento_envase (cliente_id, envase_id, tipo, cantidad, fecha) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            if (mov.getTitular() != null)
            {
                ps.setString(1, mov.getTitular().getId());
            } else
            {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setString(2, mov.getEnvase().getId());
            ps.setString(3, mov.getTipo().name());
            ps.setInt(4, mov.getCantidad());
            ps.setTimestamp(5, Timestamp.valueOf(mov.getFecha()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys())
            {
                if (rs.next())
                {
                    mov.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al registrar movimiento de envase", e);
        }
    }

    @Override
    public MovimientoEnvase buscarPorId(String id)
    {
        String sql = "SELECT * FROM movimiento_envase WHERE id = ?";

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
            throw new PersistenciaException("Error al buscar movimiento de envase por id", e);
        }
        return null;
    }

    @Override
    public List<MovimientoEnvase> listar()
    {
        List<MovimientoEnvase> lista = new ArrayList<>();

        String sql = "SELECT * FROM movimiento_envase ORDER BY fecha DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                lista.add(mapear(rs));
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al listar movimientos de envases", e);
        }
        return lista;
    }

    @Override
    public void actualizar(MovimientoEnvase mov)
    {
        throw new UnsupportedOperationException("Los movimientos históricos de inventario no se modifican.");
    }

    @Override
    public void eliminar(String id)
    {
        throw new UnsupportedOperationException("Los movimientos históricos de inventario no se eliminan.");
    }

    @Override
    protected MovimientoEnvase mapear(ResultSet rs) throws SQLException
    {
        MovimientoEnvase mov = new MovimientoEnvase();
        mov.setId(rs.getInt("id"));
        mov.setTipo(TipoMovimientoEnvase.valueOf(rs.getString("tipo")));
        mov.setCantidad(rs.getInt("cantidad"));
        mov.setFecha(rs.getTimestamp("fecha").toLocalDateTime());

        String clienteId = rs.getString("cliente_id");
        if (clienteId != null)
        {
            mov.setTitular(new Cliente("", clienteId, "", "", true));
        }

        Envase envase = new Envase();
        envase.setId(rs.getString("envase_id"));
        mov.setEnvase(envase);

        return mov;
    }
}