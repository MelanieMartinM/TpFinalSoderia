package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.MedioDePago;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cobro;
import ar.edu.utn.tpfinalsoderia.models.DetalleRetiro;
import ar.edu.utn.tpfinalsoderia.models.Retiro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RetiroCobroRepositoryJdbc {

    private final Connection conexion;

    public RetiroCobroRepositoryJdbc(Connection conexion) {
        this.conexion = Objects.requireNonNull(conexion, "La conexión no puede ser nula.");
    }

    public void guardarRetiro(Retiro retiro) {
        Objects.requireNonNull(retiro, "El retiro no puede ser nulo.");

        if (retiro.getId() == null || retiro.getId().isBlank()) {
            retiro.setId(UUID.randomUUID().toString());
        }
        if (retiro.getFecha() == null) {
            retiro.setFecha(LocalDateTime.now());
        }

        String sql = "INSERT INTO retiros (id, pedido_id, cliente_id, fecha, total) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, retiro.getId());

            if (retiro.getPedidoId() != null && !retiro.getPedidoId().isBlank()) {
                ps.setString(2, retiro.getPedidoId());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }

            if (retiro.getCliente() != null && retiro.getCliente().getId() != null && !retiro.getCliente().getId().isBlank()) {
                ps.setString(3, retiro.getCliente().getId());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }

            ps.setTimestamp(4, Timestamp.valueOf(retiro.getFecha()));
            ps.setFloat(5, retiro.calcularTotal());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el retiro", e);
        }

        if (retiro.getDetalles() != null) {
            for (DetalleRetiro det : retiro.getDetalles()) {
                if (det != null) {
                    guardarDetalleRetiro(retiro.getId(), det);
                }
            }
        }
    }

    public void guardarDetalleRetiro(String retiroId, DetalleRetiro det) {
        Objects.requireNonNull(det, "El detalle de retiro no puede ser nulo.");

        if (det.getId() == null || det.getId().isBlank()) {
            det.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO detalle_retiro " +
                "(id, retiro_id, pedido_id, detalle_pedido_id, presentacion_id, cantidad_pedida, cantidad_retirada, precio_aplicado, tipo_cajon_id, cantidad_cajones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, det.getId());
            ps.setString(2, retiroId);

            if (det.getPedidoId() != null && !det.getPedidoId().isBlank()) {
                ps.setString(3, det.getPedidoId());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }

            if (det.getDetallePedidoId() != null && !det.getDetallePedidoId().isBlank()) {
                ps.setString(4, det.getDetallePedidoId());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            if (det.getPresentacion() != null && det.getPresentacion().getId() != null && !det.getPresentacion().getId().isBlank()) {
                ps.setString(5, det.getPresentacion().getId());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            ps.setInt(6, det.getCantidadPedida());
            ps.setInt(7, det.getCantidadRetirada());
            ps.setFloat(8, det.getPrecioAplicado());

            if (det.getTipoCajon() != null && det.getTipoCajon().getId() != null && !det.getTipoCajon().getId().isBlank()) {
                ps.setString(9, det.getTipoCajon().getId());
            } else {
                ps.setNull(9, Types.VARCHAR);
            }

            ps.setInt(10, det.getCantidadCajones());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar detalle de retiro", e);
        }
    }

    public void guardarCobro(String retiroId, Cobro cobro) {
        Objects.requireNonNull(cobro, "El cobro no puede ser nulo.");

        if (cobro.getId() == null || cobro.getId().isBlank()) {
            cobro.setId(UUID.randomUUID().toString());
        }
        if (cobro.getFecha() == null) {
            cobro.setFecha(LocalDateTime.now());
        }

        String sql = "INSERT INTO cobros " +
                "(id, retiro_id, monto, medio_pago, fecha, sin_cobro_autorizado, responsable_autorizacion, motivo_autorizacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, cobro.getId());
            ps.setString(2, retiroId);
            ps.setFloat(3, cobro.getMonto());
            ps.setString(4, cobro.getMedioPago() != null ? cobro.getMedioPago().name() : null);
            ps.setTimestamp(5, Timestamp.valueOf(cobro.getFecha()));
            ps.setBoolean(6, cobro.isSinCobroAutorizado());
            ps.setString(7, cobro.getResponsableAutorizacion());
            ps.setString(8, cobro.getMotivoAutorizacion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el cobro", e);
        }
    }

    public int obtenerCantidadYaRetirada(String detallePedidoId) {
        if (detallePedidoId == null || detallePedidoId.isBlank()) {
            return 0;
        }

        String sql = "SELECT COALESCE(SUM(cantidad_retirada), 0) AS total_retirado " +
                "FROM detalle_retiro WHERE detalle_pedido_id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, detallePedidoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_retirado");
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar cantidades ya retiradas", e);
        }
        return 0;
    }

    public List<Cobro> listarCobros() {
        List<Cobro> lista = new ArrayList<>();

        String sql = "SELECT id, monto, medio_pago, fecha, sin_cobro_autorizado, responsable_autorizacion, motivo_autorizacion " +
                "FROM cobros ORDER BY fecha DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String medioStr = rs.getString("medio_pago");
                MedioDePago medio = (medioStr != null && !medioStr.isBlank()) ? MedioDePago.valueOf(medioStr) : null;
                Timestamp ts = rs.getTimestamp("fecha");
                LocalDateTime fecha = (ts != null) ? ts.toLocalDateTime() : null;

                Cobro c = new Cobro(
                        rs.getString("id"),
                        rs.getFloat("monto"),
                        medio,
                        fecha,
                        rs.getBoolean("sin_cobro_autorizado"),
                        rs.getString("responsable_autorizacion"),
                        rs.getString("motivo_autorizacion")
                );

                lista.add(c);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar cobros", e);
        }

        return lista;
    }
}