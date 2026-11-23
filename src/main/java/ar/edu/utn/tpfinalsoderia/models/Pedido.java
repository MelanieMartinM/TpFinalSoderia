package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.enums.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {

private int id;
private Cliente cliente;
        private LocalDateTime fecha;
        private EstadoPedido estado;
        private final List<DetallePedido> detalles = new ArrayList<>();

        public Pedido(){
            this.fecha=LocalDateTime.now();
            this.estado=EstadoPedido.BORRADOR;
        }


        public Pedido(int id, Cliente cliente){
            this();
            this.id=id;
            this.cliente=cliente;
        }


    public int getId() {
        return id;
    }

    public void setId(int id) {
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
    public void agregarDetalle(DetallePedido detalle){
            this.detalles.add(detalle);

    }
    public void confirmar(){


            if (detalles.isEmpty()){
                throw new IllegalStateException("Error.No se puede confirmar un pedido sin detalles.");

            }
            this.estado=EstadoPedido.CONFIRMADO;

    }
public BigDecimal calcularTotal(){
            BigDecimal aux= BigDecimal.ZERO;

            for(DetallePedido detalle : detalles){
                aux=aux.add(detalle.calcularSubtotal());
            }
            return aux ;
}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pedido pedido)) return false;
        return id == pedido.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
