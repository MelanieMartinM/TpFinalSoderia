package ar.edu.utn.tpfinalsoderia.models;

import java.math.BigDecimal;
import java.util.Objects;

public class Presentacion {

    private String id;
    private String nombre;
    private Producto producto;
    private Envase envase;
    private Marca marca;
    private BigDecimal precioLlenado;
    private boolean activa;


    public Presentacion() {
    }


    public Presentacion(String id, String nombre, Producto producto, Envase envase, Marca marca, BigDecimal precioLlenado, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.producto = producto;
        this.envase = envase;
        this.marca = marca;
        this.precioLlenado = precioLlenado;
        this.activa = activa;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Envase getEnvase() {
        return envase;
    }

    public void setEnvase(Envase envase) {
        this.envase = envase;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public BigDecimal getPrecioLlenado() {
        return precioLlenado;
    }

    public void setPrecioLlenado(BigDecimal precioLlenado) {
        this.precioLlenado = precioLlenado;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }


    public void desactivar(){

        setActiva(false);
    }

    @Override
    public String toString() {
        return "Presentacion{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", producto=" + producto +
                ", envase=" + envase +
                ", marca=" + marca +
                ", precioLlenado=" + precioLlenado +
                ", activa=" + activa +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Presentacion that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
