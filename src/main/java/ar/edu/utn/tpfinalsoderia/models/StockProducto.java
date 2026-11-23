package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;

public class StockProducto {
    private String id;
    private Cliente titular;
    private Presentacion presentacion;
    private int disponibles;

    public StockProducto() {}

    public StockProducto(String id, Cliente titular, Presentacion presentacion, int disponibles) {
        this.id = id;
        this.titular = titular;
        this.presentacion = presentacion;
        this.disponibles = disponibles;
    }

    public void sumarProduccion(int cantidad) {
        if (cantidad > 0) this.disponibles += cantidad;
    }

    public void descontarRetiro(int cantidad) throws StockInsuficienteException {
        if (cantidad > this.disponibles) {
            throw new StockInsuficienteException("Stock de producto lleno insuficiente. Disponible: " + this.disponibles);
        }
        this.disponibles -= cantidad;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Cliente getTitular() { return titular; }
    public void setTitular(Cliente titular) { this.titular = titular; }
    public Presentacion getPresentacion() { return presentacion; }
    public void setPresentacion(Presentacion presentacion) { this.presentacion = presentacion; }
    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }
}