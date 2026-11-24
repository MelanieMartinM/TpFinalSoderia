package ar.edu.utn.tpfinalsoderia.DAO;

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
import java.util.Objects;

public class InventarioRepositoryJdbc {

    private final Connection conexion;

    public InventarioRepositoryJdbc(Connection conexion)
    {
        this.conexion = Objects.requireNonNull(conexion, "La conexión no puede ser nula.");
    }

    public StockProducto buscarStockProducto(Cliente titular, Presentacion presentacion) {
        Objects.requireNonNull(presentacion, "La presentación no puede ser nula.");

        String sqlConTitular = "SELECT id, cliente_id, presentacion_id, disponibles FROM stock_producto WHERE cliente_id = ? AND presentacion_id = ?";
        String sqlSinTitular = "SELECT id, cliente_id, presentacion_id, disponibles FROM stock_producto WHERE cliente_id IS NULL AND presentacion_id = ?";
        boolean tieneTitular = titular != null && titular.getId() != null && !titular.getId().isBlank();
        String sql = tieneTitular ? sqlConTitular : sqlSinTitular;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (tieneTitular) {
                ps.setString(1, titular.getId());
                ps.setString(2, presentacion.getId());
            } else {
                ps.setString(1, presentacion.getId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StockProducto(
                            rs.getString("id"),
                            titular,
                            presentacion,
                            rs.getInt("disponibles")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar stock de producto.", e);
        }
    }

    public void guardarStockProducto(StockProducto stock) {
        Objects.requireNonNull(stock, "El stock de producto no puede ser nulo.");
        Objects.requireNonNull(stock.getPresentacion(), "La presentación del stock no puede ser nula.");

        String sql = """
                INSERT INTO stock_producto (id, cliente_id, presentacion_id, disponibles)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    disponibles = VALUES(disponibles)
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, stock.getId());
            ps.setString(2, stock.getTitular() != null ? stock.getTitular().getId() : null);
            ps.setString(3, stock.getPresentacion().getId());
            ps.setInt(4, stock.getDisponibles());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar stock de producto.", e);
        }
    }

    public StockEnvase buscarStockEnvase(Cliente titular, Envase envase) {
        Objects.requireNonNull(envase, "El envase no puede ser nulo.");

        String sqlConTitular = "SELECT id, cliente_id, envase_id, aptos, sucios FROM stock_envase WHERE cliente_id = ? AND envase_id = ?";
        String sqlSinTitular = "SELECT id, cliente_id, envase_id, aptos, sucios FROM stock_envase WHERE cliente_id IS NULL AND envase_id = ?";
        boolean tieneTitular = titular != null && titular.getId() != null && !titular.getId().isBlank();
        String sql = tieneTitular ? sqlConTitular : sqlSinTitular;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (tieneTitular) {
                ps.setString(1, titular.getId());
                ps.setString(2, envase.getId());
            } else {
                ps.setString(1, envase.getId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StockEnvase(
                            rs.getString("id"),
                            titular,
                            envase,
                            rs.getInt("aptos"),
                            rs.getInt("sucios")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar stock de envase.", e);
        }
    }

    public void guardarStockEnvase(StockEnvase stock) {
        Objects.requireNonNull(stock, "El stock de envase no puede ser nulo.");
        Objects.requireNonNull(stock.getEnvase(), "El envase del stock no puede ser nulo.");

        String sql = """
                INSERT INTO stock_envase (id, cliente_id, envase_id, aptos, sucios)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    aptos = VALUES(aptos),
                    sucios = VALUES(sucios)
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, stock.getId());
            ps.setString(2, stock.getTitular() != null ? stock.getTitular().getId() : null);
            ps.setString(3, stock.getEnvase().getId());
            ps.setInt(4, stock.getAptos());
            ps.setInt(5, stock.getSucios());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar stock de envase.", e);
        }
    }

    public void registrarMovimientoEnvase(MovimientoEnvase movimiento) {
        Objects.requireNonNull(movimiento, "El movimiento de envase no puede ser nulo.");
        Objects.requireNonNull(movimiento.getEnvase(), "El envase del movimiento no puede ser nulo.");
        Objects.requireNonNull(movimiento.getTipo(), "El tipo de movimiento no puede ser nulo.");

        String sql = "INSERT INTO movimiento_envase (id, cliente_id, envase_id, tipo, cantidad, fecha) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, movimiento.getId());
            ps.setString(2, movimiento.getTitular() != null ? movimiento.getTitular().getId() : null);
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
        Objects.requireNonNull(stock, "El stock de cajón no puede ser nulo.");
        Objects.requireNonNull(stock.getTipo(), "El tipo de cajón no puede ser nulo.");

        String sql = """
                INSERT INTO stock_cajon (tipo_cajon_id, disponibles_fabrica)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                    disponibles_fabrica = VALUES(disponibles_fabrica)
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, stock.getTipo().getId());
            ps.setInt(2, stock.getDisponiblesFabrica());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar stock de cajón.", e);
        }
    }

    public void registrarMovimientoCajon(MovimientoCajon movimiento) {
        Objects.requireNonNull(movimiento, "El movimiento de cajón no puede ser nulo.");
        Objects.requireNonNull(movimiento.getTipoCajon(), "El tipo de cajón del movimiento no puede ser nulo.");

        String sql = "INSERT INTO movimiento_cajon (id, cliente_id, tipo_cajon_id, cantidad, fecha, es_retorno) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, movimiento.getId());
            ps.setString(2, movimiento.getCliente() != null ? movimiento.getCliente().getId() : null);
            ps.setString(3, movimiento.getTipoCajon().getId());
            ps.setInt(4, movimiento.getCantidad());
            ps.setTimestamp(5, movimiento.getFecha() != null ? Timestamp.valueOf(movimiento.getFecha()) : null);
            ps.setBoolean(6, movimiento.isEsRetorno());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar movimiento de cajón.", e);
        }
    }

    public int saldoEnvases(Cliente cliente, Envase envase) {
        Objects.requireNonNull(envase, "El envase no puede ser nulo.");

        String sqlConTitular = "SELECT COALESCE(SUM(aptos + sucios), 0) AS saldo FROM stock_envase WHERE envase_id = ? AND cliente_id = ?";
        String sqlSinTitular = "SELECT COALESCE(SUM(aptos + sucios), 0) AS saldo FROM stock_envase WHERE envase_id = ? AND cliente_id IS NULL";
        boolean tieneTitular = cliente != null && cliente.getId() != null && !cliente.getId().isBlank();
        String sql = tieneTitular ? sqlConTitular : sqlSinTitular;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, envase.getId());
            if (tieneTitular) {
                ps.setString(2, cliente.getId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("saldo");
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar saldo de envases.", e);
        }
    }

    public int saldoCajones(Cliente cliente, TipoCajon tipo) {
        Objects.requireNonNull(tipo, "El tipo de cajón no puede ser nulo.");

        String sqlConTitular = """
                SELECT COALESCE(SUM(CASE WHEN es_retorno THEN cantidad ELSE -cantidad END), 0) AS saldo
                FROM movimiento_cajon
                WHERE tipo_cajon_id = ? AND cliente_id = ?
                """;
        String sqlSinTitular = """
                SELECT COALESCE(SUM(CASE WHEN es_retorno THEN cantidad ELSE -cantidad END), 0) AS saldo
                FROM movimiento_cajon
                WHERE tipo_cajon_id = ? AND cliente_id IS NULL
                """;
        boolean tieneTitular = cliente != null && cliente.getId() != null && !cliente.getId().isBlank();
        String sql = tieneTitular ? sqlConTitular : sqlSinTitular;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, tipo.getId());
            if (tieneTitular) {
                ps.setString(2, cliente.getId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("saldo");
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar saldo de cajones.", e);
        }
    }
}