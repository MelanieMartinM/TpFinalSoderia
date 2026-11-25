package ar.edu.utn.tpfinalsoderia.models;

import java.math.BigDecimal;
import java.util.Objects;

public class DetallePedido {

    private String id;
    private Presentacion presentacion;
    private int cantidadUnidades;
    private BigDecimal precioUnitario;
    private int cantidadCajones;
    private TipoCajon tipoCajon;

    public DetallePedido() {
    }

    public DetallePedido(
            Presentacion presentacion,
            int cantUnidades,
            BigDecimal precioUnitario,
            int cantCajones,
            TipoCajon tipoCajon) {

        this.presentacion = presentacion;
        this.cantidadUnidades = cantUnidades;
        this.precioUnitario = precioUnitario;
        this.cantidadCajones = cantCajones;
        this.tipoCajon = tipoCajon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public BigDecimal calcularSubtotal() {

        if (cantidadUnidades <= 0) {
            throw new IllegalStateException(
                    "La cantidad de unidades debe ser mayor a cero."
            );
        }

        if (precioUnitario == null
                || precioUnitario.signum() <= 0
                || precioUnitario.stripTrailingZeros().scale() > 2) {

            throw new IllegalStateException(
                    "El precio unitario debe ser positivo "
                            + "y tener hasta dos decimales."
            );
        }

        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidadUnidades)
        );
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof DetallePedido detalle)) {
            return false;
        }

        return id != null && Objects.equals(id, detalle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}