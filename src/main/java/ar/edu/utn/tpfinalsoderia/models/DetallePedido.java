package ar.edu.utn.tpfinalsoderia.models;

import java.math.BigDecimal;
import java.util.Objects;




public class DetallePedido {

private Presentacion presentacion;
private int cantidadUnidades;
private BigDecimal precioUnitario;
private int cantidadCajones;
private TipoCajon tipoCajon;


    public DetallePedido() {
    }

    public DetallePedido(Presentacion presentacion, int cantUnidades, BigDecimal precioUnitario, int cantCajones, TipoCajon tipoCajon) {
        this.presentacion = presentacion;
        this.cantidadUnidades = cantUnidades;
        this.precioUnitario = precioUnitario;
        this.cantidadCajones = cantCajones;
        this.tipoCajon = tipoCajon;
    }

    public Presentacion getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(Presentacion presentacion) {
        this.presentacion = presentacion;
    }

    public int getCantidadUnidades() {
        return cantidadUnidades;
    }

    public void setCantidadUnidades(int cantidadUnidades) {
        this.cantidadUnidades = cantidadUnidades;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getCantidadCajones() {
        return cantidadCajones;
    }

    public void setCantidadCajones(int cantidadCajones) {
        this.cantidadCajones = cantidadCajones;
    }

    public TipoCajon getTipoCajon() {
        return tipoCajon;
    }

    public void setTipoCajon(TipoCajon tipoCajon) {
        this.tipoCajon = tipoCajon;
    }

public BigDecimal calcularSubtotal(){
        if(this== null || cantidadUnidades <=0){
            return BigDecimal.ZERO;

        }return precioUnitario.multiply (BigDecimal.valueOf(cantidadUnidades));
}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DetallePedido that)) return false;
        return Objects.equals(presentacion, that.presentacion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(presentacion);
    }
}
