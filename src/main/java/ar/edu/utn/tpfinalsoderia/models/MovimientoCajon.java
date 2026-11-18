package ar.edu.utn.tpfinalsoderia.models;

import java.time.LocalDateTime;

public class MovimientoCajon
{
    private int id;
    private Cliente cliente;
    private TipoCajon tipoCajon;
    private int cantidad;
    private LocalDateTime fecha;
    private boolean esRetorno;

    public MovimientoCajon() {}

    public MovimientoCajon(int id, Cliente cliente, TipoCajon tipoCajon, int cantidad, LocalDateTime fecha, boolean esRetorno) {
        this.id = id;
        this.cliente = cliente;
        this.tipoCajon = tipoCajon;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.esRetorno = esRetorno;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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
}
