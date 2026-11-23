package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.CajonDAO;
import ar.edu.utn.tpfinalsoderia.DAO.RetiroCobroRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.StockEnvaseDAO;
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

public class ServicioReportes
{
    public static class FilaSaldoCajonCliente
    {
        private final String clienteId;
        private final String clienteNombre;
        private final String tipoCajonId;
        private final String tipoCajonDesc;
        private final int saldoActual;

        public FilaSaldoCajonCliente(String clienteId, String clienteNombre, String tipoCajonId, String tipoCajonDesc, int saldoActual)
        {
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
    }

    private final Connection conexion;
    private final StockEnvaseDAO stockEnvaseDAO;
    private final CajonDAO cajonDAO;
    private final RetiroCobroRepositoryJdbc retiroCobroRepo;

    public ServicioReportes(Connection conexion, StockEnvaseDAO stockEnvaseDAO, CajonDAO cajonDAO, RetiroCobroRepositoryJdbc retiroCobroRepo)
    {
        this.conexion = conexion;
        this.stockEnvaseDAO = stockEnvaseDAO;
        this.cajonDAO = cajonDAO;
        this.retiroCobroRepo = retiroCobroRepo;
    }

    public List<StockEnvase> obtenerReporteStockEnvases()
    {
        return stockEnvaseDAO.listar();
    }

    public List<Cobro> obtenerReporteCobros()
    {
        return retiroCobroRepo.listarCobros();
    }

    public List<FilaSaldoCajonCliente> obtenerReporteSaldosCajonesPorCliente()
    {
        List<FilaSaldoCajonCliente> resultado = new ArrayList<>();

        List<MovimientoCajon> movimientos = cajonDAO.listarMovimientos();

        List<String> paresVistos = new ArrayList<>();

        for (MovimientoCajon mov : movimientos)
        {
            Cliente cli = mov.getCliente();

            if (cli == null || cli.getId() == null || cli.getId().isBlank())
            {
                continue;
            }
            if (mov.getTipoCajon() == null || mov.getTipoCajon().getId() == null)
            {
                continue;
            }

            String clave = cli.getId() + "::" + mov.getTipoCajon().getId();
            if (paresVistos.contains(clave))
            {
                continue;
            }

            paresVistos.add(clave);

            int saldo = cajonDAO.calcularSaldoCajonesCliente(cli.getId(), mov.getTipoCajon().getId());
            String nombreCliente = (cli.getNombre() != null && !cli.getNombre().isBlank())
                    ? cli.getNombre()
                    : cli.getId();

            resultado.add(new FilaSaldoCajonCliente
                    (
                    cli.getId(),
                    nombreCliente,
                    mov.getTipoCajon().getId(),
                    mov.getTipoCajon().getDescripcion(),
                    saldo
            ));
        }

        return resultado;
    }

    public float calcularTotalCobrado()
    {
        float total = 0f;

        String sql = "SELECT COALESCE(SUM(monto), 0) AS total_cobrado FROM cobros WHERE sin_cobro_autorizado = FALSE";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getFloat("total_cobrado");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al calcular el total cobrado", e);
        }
        return total;
    }
}