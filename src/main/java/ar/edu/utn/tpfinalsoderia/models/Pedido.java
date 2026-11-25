package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.enums.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {

    private String id;
    private Cliente cliente;
    private LocalDateTime fecha;
    private EstadoPedido estado;

    private final List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.BORRADOR;
    }

    public Pedido(String id, Cliente cliente) {
        this();
        this.id = id;
        this.cliente = cliente;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void agregarDetalle(DetallePedido detalle) {

        if (detalle == null) {
            throw new IllegalArgumentException(
                    "El detalle del pedido no puede ser nulo."
            );
        }

        detalles.add(detalle);
    }

    public void confirmar() {

        if (estado != EstadoPedido.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se pueden confirmar pedidos en estado BORRADOR."
            );
        }

        if (detalles.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede confirmar un pedido sin detalles."
            );
        }

        this.estado = EstadoPedido.CONFIRMADO;
    }

    public BigDecimal calcularTotal() {

        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedido detalle : detalles) {

            total = total.add(
                    detalle.calcularSubtotal()
            );
        }

        return total;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Pedido pedido)) {
            return false;
        }

        return id != null && Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}