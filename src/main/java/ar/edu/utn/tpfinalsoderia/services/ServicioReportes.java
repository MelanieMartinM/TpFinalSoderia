package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.RetiroCobroRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Cobro;
import ar.edu.utn.tpfinalsoderia.models.MovimientoCajon;
import ar.edu.utn.tpfinalsoderia.models.StockEnvase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServicioReportes {

    public static class FilaSaldoCajonCliente {
        private final String clienteId;
        private final String clienteNombre;
        private final String tipoCajonId;
        private final String tipoCajonDesc;
        private int saldoActual;

        public FilaSaldoCajonCliente(
                String clienteId,
                String clienteNombre,
                String tipoCajonId,
                String tipoCajonDesc,
                int saldoActual
        ) {
            this.clienteId = clienteId;
            this.clienteNombre = clienteNombre;
            this.tipoCajonId = tipoCajonId;
            this.tipoCajonDesc = tipoCajonDesc;
            this.saldoActual = saldoActual;
        }

        public String getClienteId() { return clienteId; }
        public String getClienteNombre() { return clienteNombre; }
        public String getTipoCajonId() { return tipoCajonId; }
        public String getTipoCajonDesc() { return tipoCajonDesc; }
        public int getSaldoActual() { return saldoActual; }

        public void sumarMovimiento(int cantidad, boolean esRetorno) {
            if (esRetorno) {
                this.saldoActual -= cantidad;
            } else {
                this.saldoActual += cantidad;
            }
        }
    }

    private final Connection conexion;
    private final InventarioRepositoryJdbc inventarioRepo;
    private final RetiroCobroRepositoryJdbc retiroCobroRepo;

    public ServicioReportes(
            Connection conexion,
            InventarioRepositoryJdbc inventarioRepo,
            RetiroCobroRepositoryJdbc retiroCobroRepo
    ) {
        this.conexion = Objects.requireNonNull(conexion, "La conexión no puede ser nula.");
        this.inventarioRepo = Objects.requireNonNull(inventarioRepo, "El repositorio de inventario no puede ser nulo.");
        this.retiroCobroRepo = Objects.requireNonNull(retiroCobroRepo, "El repositorio de retiros y cobros no puede ser nulo.");
    }

    public List<StockEnvase> obtenerReporteStockEnvases() {
        return inventarioRepo.listarStockEnvases();
    }

    public List<Cobro> obtenerReporteCobros() {
        return retiroCobroRepo.listarCobros();
    }

    public List<FilaSaldoCajonCliente> obtenerReporteSaldosCajonesPorCliente() {
        List<FilaSaldoCajonCliente> resultado = new ArrayList<>();
        for (ar.edu.utn.tpfinalsoderia.models.StockCajon stock : inventarioRepo.listarStockCajones()) {
            Cliente titular = stock.getTitular();
            if (titular == null || stock.getTipo() == null) continue;
            String nombre = titular.getNombre() == null ? titular.getId() : titular.getNombre();
            String descripcion = stock.getTipo().getDescripcion() == null
                    ? stock.getTipo().getId() : stock.getTipo().getDescripcion();
            resultado.add(new FilaSaldoCajonCliente(titular.getId(), nombre,
                    stock.getTipo().getId(), descripcion, stock.getDisponibles()));
        }
        return resultado;
    }

    private FilaSaldoCajonCliente buscarFila(List<FilaSaldoCajonCliente> filas, String clienteId, String tipoCajonId) {
        for (FilaSaldoCajonCliente f : filas) {
            if (Objects.equals(f.getClienteId(), clienteId) && Objects.equals(f.getTipoCajonId(), tipoCajonId)) {
                return f;
            }
        }
        return null;
    }

    public java.math.BigDecimal calcularTotalCobrado() {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;

        String sql = "SELECT COALESCE(SUM(monto), 0) AS total_cobrado FROM cobros WHERE sin_cobro_autorizado = FALSE";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getBigDecimal("total_cobrado");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al calcular el total cobrado", e);
        }
        return total;
    }
}