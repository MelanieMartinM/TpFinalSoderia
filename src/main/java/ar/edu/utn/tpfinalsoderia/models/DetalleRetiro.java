package ar.edu.utn.tpfinalsoderia.models;

import java.util.Objects;

public class DetalleRetiro
{
    private String id;
    private String pedidoId;
    private String detallePedidoId;
    private Presentacion presentacion;
    private int cantidadPedida;
    private int cantidadRetirada;
    private float precioAplicado;
    private TipoCajon tipoCajon;
    private int cantidadCajones;

    public DetalleRetiro()
    {

    }

    public DetalleRetiro(String id, String pedidoId, String detallePedidoId, Presentacion presentacion, int cantidadPedida, int cantidadRetirada, float precioAplicado, TipoCajon tipoCajon, int cantidadCajones)
    {
        this.id = id;
        this.pedidoId = pedidoId;
        this.detallePedidoId = detallePedidoId;
        this.presentacion = presentacion;
        this.cantidadPedida = cantidadPedida;
        this.cantidadRetirada = cantidadRetirada;
        this.precioAplicado = precioAplicado;
        this.tipoCajon = tipoCajon;
        this.cantidadCajones = cantidadCajones;
    }

    public float calcularSubtotal()
    {
        return cantidadRetirada * precioAplicado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
    public String getDetallePedidoId() { return detallePedidoId; }
    public void setDetallePedidoId(String detallePedidoId) { this.detallePedidoId = detallePedidoId; }
    public Presentacion getPresentacion() { return presentacion; }
    public void setPresentacion(Presentacion presentacion) { this.presentacion = presentacion; }
    public int getCantidadPedida() { return cantidadPedida; }
    public void setCantidadPedida(int cantidadPedida) { this.cantidadPedida = cantidadPedida; }
    public int getCantidadRetirada() { return cantidadRetirada; }
    public void setCantidadRetirada(int cantidadRetirada) { this.cantidadRetirada = cantidadRetirada; }
    public float getPrecioAplicado() { return precioAplicado; }
    public void setPrecioAplicado(float precioAplicado) { this.precioAplicado = precioAplicado; }
    public TipoCajon getTipoCajon() { return tipoCajon; }
    public void setTipoCajon(TipoCajon tipoCajon) { this.tipoCajon = tipoCajon; }
    public int getCantidadCajones() { return cantidadCajones; }
    public void setCantidadCajones(int cantidadCajones) { this.cantidadCajones = cantidadCajones; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleRetiro other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}