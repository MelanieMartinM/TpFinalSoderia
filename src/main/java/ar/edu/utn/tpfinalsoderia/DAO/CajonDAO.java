package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.MovimientoCajon;
import ar.edu.utn.tpfinalsoderia.models.StockCajon;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CajonDAO
{
    private final Connection conexion;

    public CajonDAO(Connection conexion)
    {
        this.conexion = conexion;
    }


    public TipoCajon buscarTipoCajonPorId(String id)
    {
        String sql = "SELECT id, codigo, descripcion, capacidad FROM tipos_cajon WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return new TipoCajon(
                            rs.getString("id"),
                            rs.getString("codigo"),
                            rs.getString("descripcion"),
                            rs.getInt("capacidad")
                    );
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al buscar tipo de cajón", e);
        }
        return null;
    }


    public StockCajon buscarStockPorTipo (String tipoCajonId)
    {
        String sql = """
                SELECT sc.tipo_cajon_id, sc.disponibles_fabrica,
                       tc.codigo, tc.descripcion, tc.capacidad
                FROM stock_cajon sc
                INNER JOIN tipos_cajon tc ON sc.tipo_cajon_id = tc.id
                WHERE sc.tipo_cajon_id = ?
                """;

        try(PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setString(1, tipoCajonId);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    TipoCajon tipo = new TipoCajon(
                            rs.getString("tipo_cajon_id"),
                            rs.getString("codigo"),
                            rs.getString("descripcion"),
                            rs.getInt("capacidad")
                    );
                    return new StockCajon(tipo, rs.getInt("disponibles_fabrica"));
                }

            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al buscar stock de cajones", e);
        }

        return null;
    }

    public void guardarOActualizarStock(StockCajon stock)
    {
        String sql = """
                INSERT INTO stock_cajon (tipo_cajon_id, disponibles_fabrica)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE disponibles_fabrica = VALUES(disponibles_fabrica)
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setString(1, stock.getTipo().getId());
            ps.setInt(2, stock.getDisponiblesFabrica());
            ps.executeUpdate();

        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al actualizar stock de cajones", e);
        }

    }

    public void registrarMovimiento(MovimientoCajon mov)
    {
        if (mov.getId() == null || mov.getId().isBlank())
        {
            mov.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO movimiento_cajon (id, cliente_id, tipo_cajon_id, cantidad, fecha, es_retorno) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setString(1, mov.getId());

            if (mov.getCliente() != null)
            {
                ps.setString(2, mov.getCliente().getId());
            } else
            {
                ps.setNull(2, Types.VARCHAR);
            }

            ps.setString(3, mov.getTipoCajon().getId());
            ps.setInt(4, mov.getCantidad());
            ps.setTimestamp(5, Timestamp.valueOf(mov.getFecha()));
            ps.setBoolean(6, mov.isEsRetorno());
            ps.executeUpdate();

        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al registrar movimiento de cajón", e);
        }
    }

    public List<StockCajon> listarStocks()
    {
        List<StockCajon> lista = new ArrayList<>();

        String sql = "SELECT sc.tipo_cajon_id, sc.disponibles_fabrica, tc.codigo, tc.descripcion, tc.capacidad " +
                "FROM stock_cajon sc INNER JOIN tipos_cajon tc ON sc.tipo_cajon_id = tc.id";

        try (PreparedStatement ps = conexion.prepareStatement(sql);

             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                TipoCajon tipo = new TipoCajon(
                        rs.getString("tipo_cajon_id"),
                        rs.getString("codigo"),
                        rs.getString("descripcion"),
                        rs.getInt("capacidad")
                );

                lista.add(new StockCajon(tipo, rs.getInt("disponibles_fabrica")));
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al listar stock de cajones", e);
        }

        return lista;
    }

    public List<MovimientoCajon> listarMovimientos()
    {
        List<MovimientoCajon> lista = new ArrayList<>();

        String sql = "SELECT mc.id, mc.cliente_id, mc.tipo_cajon_id, mc.cantidad, mc.fecha, mc.es_retorno, " +
                "c.nombre AS cliente_nombre, c.telefono AS cliente_telefono, c.direccion AS cliente_direccion, c.activo AS cliente_activo, " +
                "tc.codigo, tc.descripcion, tc.capacidad " +
                "FROM movimiento_cajon mc " +
                "LEFT JOIN clientes c ON mc.cliente_id = c.id " +
                "INNER JOIN tipos_cajon tc ON mc.tipo_cajon_id = tc.id " +
                "ORDER BY mc.fecha DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                Cliente cliente = null;
                String clienteId = rs.getString("cliente_id");

                if (clienteId != null)
                {
                    cliente = new Cliente(
                            rs.getString("cliente_direccion"),
                            clienteId,
                            rs.getString("cliente_nombre"),
                            rs.getString("cliente_telefono"),
                            rs.getBoolean("cliente_activo")
                    );
                }

                TipoCajon tipo = new TipoCajon(
                        rs.getString("tipo_cajon_id"),
                        rs.getString("codigo"),
                        rs.getString("descripcion"),
                        rs.getInt("capacidad")
                );

                lista.add(new MovimientoCajon(
                        rs.getString("id"),
                        cliente,
                        tipo,
                        rs.getInt("cantidad"),
                        rs.getTimestamp("fecha").toLocalDateTime(),
                        rs.getBoolean("es_retorno")
                ));
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al listar movimientos de cajones", e);
        }

        return lista;
    }

    public int calcularSaldoCajonesCliente(String clienteId, String tipoCajonId)
    {
        String sql = "SELECT COALESCE(SUM(CASE WHEN es_retorno = FALSE THEN cantidad ELSE -cantidad END), 0) AS saldo " +
                "FROM movimiento_cajon WHERE cliente_id = ? AND tipo_cajon_id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql))
        {
            ps.setString(1, clienteId);
            ps.setString(2, tipoCajonId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getInt("saldo");
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenciaException("Error al calcular saldo de cajones del cliente", e);
        }

        return 0;
    }

}
