package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import java.util.Objects;

/** Unidades llenas de una presentación pertenecientes a un repartidor. */
public class StockProducto {
    private Cliente titular;
    private Presentacion presentacion;
    private int disponibles;

    public StockProducto() { }

    public StockProducto(Cliente titular, Presentacion presentacion, int disponibles) {
        this.titular = titular;
        this.presentacion = presentacion;
        this.disponibles = disponibles;
    }

    public Cliente getTitular() { return titular; }
    public void setTitular(Cliente titular) { this.titular = titular; }
    public Presentacion getPresentacion() { return presentacion; }
    public void setPresentacion(Presentacion presentacion) { this.presentacion = presentacion; }
    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }

    public void sumarProduccion(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        disponibles = Math.addExact(disponibles, cantidad);
    }

    public void descontarRetiro(int cantidad) throws StockInsuficienteException {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        if (cantidad > disponibles) throw new StockInsuficienteException("Stock insuficiente: " + disponibles);
        disponibles -= cantidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockProducto other)) return false;
        return titular != null && other.titular != null && presentacion != null && other.presentacion != null
                && titular.getId() != null && presentacion.getId() != null
                && Objects.equals(titular.getId(), other.titular.getId())
                && Objects.equals(presentacion.getId(), other.presentacion.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(titular == null ? null : titular.getId(),
                presentacion == null ? null : presentacion.getId());
    }
}
