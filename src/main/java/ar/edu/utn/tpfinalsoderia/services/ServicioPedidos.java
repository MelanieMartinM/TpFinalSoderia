package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.CatalogoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.PedidoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.enums.EstadoPedido;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.DetallePedido;
import ar.edu.utn.tpfinalsoderia.models.Pedido;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioPedidos {

    private final PedidoRepositoryJdbc pedidoRepository;
    private final CatalogoRepositoryJdbc catalogoRepository;

    public ServicioPedidos(PedidoRepositoryJdbc pedidoRepository,
                           CatalogoRepositoryJdbc catalogoRepository) {
        if (pedidoRepository == null) {
            throw new IllegalArgumentException("El repositorio de pedidos no puede ser nulo.");
        }
        if (catalogoRepository == null) {
            throw new IllegalArgumentException("El repositorio de catálogo no puede ser nulo.");
        }
        this.pedidoRepository = pedidoRepository;
        this.catalogoRepository = catalogoRepository;
    }

    public Pedido crearPedido(Cliente cliente) throws PersistenciaException, ReglaNegocioException {
        if (cliente == null || cliente.getId() == null || cliente.getId().isBlank()) {
            throw new ReglaNegocioException("El cliente del pedido es obligatorio.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.BORRADOR);
        pedido.setFecha(LocalDateTime.now());
        pedidoRepository.guardar(pedido);
        return pedido;
    }

    public void agregarItem(Pedido pedido,
                            Presentacion presentacion,
                            int cantidadUnidades,
                            int cantidadCajones,
                            TipoCajon tipoCajon,
                            BigDecimal precioUnitarioPactado) throws PersistenciaException, ReglaNegocioException {
        if (pedido == null) {
            throw new ReglaNegocioException("El pedido no puede ser nulo.");
        }if (pedido.getId() == null || pedido.getId().isBlank()) {
            throw new ReglaNegocioException("El ID del pedido es obligatorio para confirmar.");
        }
        if (pedido.getEstado() != EstadoPedido.BORRADOR) {
            throw new ReglaNegocioException("Solo se pueden agregar ítems a pedidos en estado BORRADOR.");
        }
        if (presentacion == null ) {
            throw new ReglaNegocioException("La presentación es obligatoria.");
        }
        if (cantidadUnidades <= 0) {
            throw new ReglaNegocioException("La cantidad de unidades debe ser mayor a cero.");
        }
        if (cantidadCajones < 0) {
            throw new ReglaNegocioException("La cantidad de cajones no puede ser negativa.");
        }

        BigDecimal precioFinal = (precioUnitarioPactado != null)
                ? precioUnitarioPactado
                : presentacion.getPrecioLlenado();

        if (precioFinal == null || precioFinal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio unitario pactado debe ser mayor a cero.");
        }

        DetallePedido detalle = new DetallePedido(
                presentacion,
                cantidadUnidades,
                precioFinal,
                cantidadCajones,
                tipoCajon
        );
        pedido.agregarDetalle(detalle);
        pedidoRepository.actualizar(pedido);
    }

    public void confirmarPedido(Pedido pedido) throws PersistenciaException, ReglaNegocioException {
        if (pedido == null) {
            throw new ReglaNegocioException("El pedido no puede ser nulo.");
        }
        if (pedido.getId() == null ) {
            throw new ReglaNegocioException("El ID del pedido es obligatorio para confirmar.");
        }

        Pedido pedidoExistente = pedidoRepository.buscarPorId(pedido.getId());
        if (pedidoExistente == null) {
            throw new ReglaNegocioException("No existe un pedido con ID: " + pedido.getId());
        }
        if (pedidoExistente.getEstado() != EstadoPedido.BORRADOR) {
            throw new ReglaNegocioException("Solo se pueden confirmar pedidos en estado BORRADOR.");
        }
        if (pedidoExistente.getDetalles() == null || pedidoExistente.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("No se puede confirmar un pedido sin detalles.");
        }

        pedidoExistente.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedidoRepository.actualizar(pedidoExistente);
    }

    public List<Pedido> listarPedidos() throws PersistenciaException {
        return pedidoRepository.listar();
    }

    public List<Presentacion> listarPresentacionesDisponibles() throws PersistenciaException {
        return catalogoRepository.listar();
    }
}