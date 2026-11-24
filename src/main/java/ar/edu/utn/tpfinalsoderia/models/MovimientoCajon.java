package ar.edu.utn.tpfinalsoderia.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class MovimientoCajon
{
    private String id;
    private Cliente cliente;
    private TipoCajon tipoCajon;
    private int cantidad;
    private LocalDateTime fecha;
    private boolean esRetorno;

    public MovimientoCajon() {}

    public MovimientoCajon(String id, Cliente cliente, TipoCajon tipoCajon, int cantidad, LocalDateTime fecha, boolean esRetorno) {
        this.id = id;
        this.cliente = cliente;
        this.tipoCajon = tipoCajon;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.esRetorno = esRetorno;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public TipoCajon getTipoCajon() { return tipoCajon; }
    public void setTipoCajon(TipoCajon tipoCajon) { this.tipoCajon = tipoCajon; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public boolean isEsRetorno() { return esRetorno; }
    public void setEsRetorno(boolean esRetorno) { this.esRetorno = esRetorno; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimientoCajon other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
