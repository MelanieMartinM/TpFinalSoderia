package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.enums.TipoMovimientoEnvase;

import java.time.LocalDateTime;
import java.util.Objects;

public class MovimientoEnvase {
    private String id;
    private Cliente titular;
    private Envase envase;
    private TipoMovimientoEnvase tipo;
    private int cantidad;
    private LocalDateTime fecha;

    public MovimientoEnvase() {}

    public MovimientoEnvase(String id, Cliente titular, Envase envase, TipoMovimientoEnvase tipo, int cantidad, LocalDateTime fecha) {
        this.id = id;
        this.titular = titular;
        this.envase = envase;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Cliente getTitular() { return titular; }
    public void setTitular(Cliente titular) { this.titular = titular; }
    public Envase getEnvase() { return envase; }
    public void setEnvase(Envase envase) { this.envase = envase; }
    public TipoMovimientoEnvase getTipo() { return tipo; }
    public void setTipo(TipoMovimientoEnvase tipo) { this.tipo = tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimientoEnvase other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}