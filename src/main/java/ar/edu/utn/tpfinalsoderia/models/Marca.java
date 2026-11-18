package ar.edu.utn.tpfinalsoderia.models;

import java.util.Objects;

public class Marca {
private int id;
private String nombre;
private boolean esPropiaFabrica;
private Cliente titular;


    public Marca() {
    }

    public Marca(int id, String nombre, boolean esPropiaFabrica, Cliente titular) {
        this.id = id;
        this.nombre = nombre;
        this.esPropiaFabrica = esPropiaFabrica;
        this.titular = titular;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsPropiaFabrica() {
        return esPropiaFabrica;
    }

    public void setEsPropiaFabrica(boolean esPropiaFabrica) {
        this.esPropiaFabrica = esPropiaFabrica;
    }

    public Cliente getTitular() {
        return titular;
    }

    public void setTitular(Cliente titular) {
        this.titular = titular;
    }

    @Override
    public String toString() {
        return "Marca{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", esPropiaFabrica=" + esPropiaFabrica +
                ", titular=" + titular +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Marca marca)) return false;
        return id == marca.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
