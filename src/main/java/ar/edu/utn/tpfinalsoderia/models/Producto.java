package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.enums.TipoProducto;

import java.util.Objects;

public class Producto {

    private  String id;
    private String nombre;
    private TipoProducto tipoProducto;
    private boolean activo;


    public Producto() {
        this.activo = true;
    }


    public Producto(String id, String nombre, TipoProducto tipoProducto){
        this.id = id;
        this.nombre = nombre;
        this.tipoProducto = tipoProducto;
        this.activo = true;
    }


    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public TipoProducto getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(TipoProducto tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void desactivar(){

        setActivo(false);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipoProducto=" + tipoProducto +
                ", activo=" + activo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Producto producto)) return false;
        return Objects.equals(id, producto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
