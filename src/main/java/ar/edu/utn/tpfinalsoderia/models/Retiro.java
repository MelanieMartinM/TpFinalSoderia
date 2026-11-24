package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.interfaces.Facturable;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Retiro implements Facturable
{
    private String id;
    private String pedidoId;
    private Cliente cliente;
    private LocalDateTime fecha;
    private List<DetalleRetiro> detalles;
    private Cobro cobro;

    public Retiro()
    {
        this.detalles = new ArrayList<>();
    }

    public Retiro(String id, String pedidoId, Cliente cliente, LocalDateTime fecha, List<DetalleRetiro> detalles, Cobro cobro)
    {
        this.id = id;
        this.pedidoId = pedidoId;
        this.cliente = cliente;
        this.fecha = fecha;
        this.detalles = (detalles != null) ? detalles : new ArrayList<>();
        this.cobro = cobro;
    }

    @Override
    public BigDecimal calcularTotal()
    {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleRetiro d : detalles)
        {
            total = total.add(d.calcularSubtotal());
        }
        return total;
    }

    @Override
    public String generarComprobante()
    {
        String nombreCliente = (cliente != null && cliente.getNombre() != null) ? cliente.getNombre() : "Sin titular";

        return "Retiro #" + id + " | Cliente: " + nombreCliente + " | Total: $" + calcularTotal();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public List<DetalleRetiro> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleRetiro> detalles) { this.detalles = detalles; }
    public Cobro getCobro() { return cobro; }
    public void setCobro(Cobro cobro) { this.cobro = cobro; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Retiro other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}