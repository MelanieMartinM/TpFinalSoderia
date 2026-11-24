package ar.edu.utn.tpfinalsoderia.models;

import java.util.Objects;

/** Cajones del tipo indicado, pertenecientes al repartidor y presentes en planta. */
public class StockCajon {
    private Cliente titular;
    private TipoCajon tipo;
    private int disponibles;

    public StockCajon() { }

    public StockCajon(Cliente titular, TipoCajon tipo, int disponibles) {
        this.titular = titular;
        this.tipo = tipo;
        this.disponibles = disponibles;
    }

    public Cliente getTitular() { return titular; }
    public void setTitular(Cliente titular) { this.titular = titular; }
    public TipoCajon getTipo() { return tipo; }
    public void setTipo(TipoCajon tipo) { this.tipo = tipo; }
    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockCajon other)) return false;
        return titular != null && other.titular != null && tipo != null && other.tipo != null
                && titular.getId() != null && tipo.getId() != null
                && Objects.equals(titular.getId(), other.titular.getId())
                && Objects.equals(tipo.getId(), other.tipo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(titular == null ? null : titular.getId(),
                tipo == null ? null : tipo.getId());
    }
}
