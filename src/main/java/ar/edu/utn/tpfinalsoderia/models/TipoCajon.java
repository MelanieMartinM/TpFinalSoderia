package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.interfaces.Retornable;

import java.util.Objects;

public class TipoCajon implements Retornable
{
    private String id;
    private String codigo;
    private String descripcion;
    private int capacidad;

    public TipoCajon() {}

    public TipoCajon(String id, String codigo, String descripcion, int capacidad) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
    }

    @Override
    public String getCodigo() { return codigo; }

    @Override
    public String getDescripcion() { return descripcion; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoCajon other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
