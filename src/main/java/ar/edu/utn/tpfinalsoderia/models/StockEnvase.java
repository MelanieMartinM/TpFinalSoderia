package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import java.util.Objects;

/** Cantidades de envases vacíos del repartidor, separados por tipo. */
public class StockEnvase {
    private Cliente titular;
    private Envase envase;
    private int aptos;
    private int sucios;

    public StockEnvase() { }
    public StockEnvase(Cliente titular, Envase envase, int aptos, int sucios) {
        this.titular = titular;
        this.envase = envase;
        this.aptos = aptos;
        this.sucios = sucios;
    }
    public Cliente getTitular() { return titular; }
    public void setTitular(Cliente titular) { this.titular = titular; }
    public Envase getEnvase() { return envase; }
    public void setEnvase(Envase envase) { this.envase = envase; }
    public int getAptos() { return aptos; }
    public void setAptos(int aptos) { this.aptos = aptos; }
    public int getSucios() { return sucios; }
    public void setSucios(int sucios) { this.sucios = sucios; }

    public void acreditarVacios(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        sucios = Math.addExact(sucios, cantidad);
    }
    public void usarAptos(int cantidad) throws StockInsuficienteException {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        if (cantidad > aptos) throw new StockInsuficienteException("Envases aptos insuficientes: " + aptos);
        aptos -= cantidad;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockEnvase other)) return false;
        return titular != null && other.titular != null && envase != null && other.envase != null
                && titular.getId() != null && envase.getId() != null
                && Objects.equals(titular.getId(), other.titular.getId())
                && Objects.equals(envase.getId(), other.envase.getId());
    }
    @Override
    public int hashCode() {
        return Objects.hash(titular == null ? null : titular.getId(), envase == null ? null : envase.getId());
    }
}
