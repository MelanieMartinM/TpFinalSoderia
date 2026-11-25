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
import ar.edu.utn.tpfinalsoderia.utils.SesionUsuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ServicioPedidos {

    private final PedidoRepositoryJdbc pedidoRepository;
    private final CatalogoRepositoryJdbc catalogoRepository;

    public ServicioPedidos(
            PedidoRepositoryJdbc pedidoRepository,
            CatalogoRepositoryJdbc catalogoRepository) {

        this.pedidoRepository = Objects.requireNonNull(
                pedidoRepository,
                "El repositorio de pedidos no puede ser nulo."
        );

        this.catalogoRepository = Objects.requireNonNull(
                catalogoRepository,
                "El repositorio de catálogo no puede ser nulo."
        );
    }

    // =====================================================
    // CREAR PEDIDO
    // =====================================================

    public Pedido crearPedido(Cliente cliente)
            throws PersistenciaException, ReglaNegocioException {

        verificarSesion();

        validarCliente(cliente);

        Pedido pedido = new Pedido();

        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.BORRADOR);
        pedido.setFecha(LocalDateTime.now());

        pedidoRepository.guardar(pedido);

        return pedido;
    }

    // =====================================================
    // CREAR PEDIDO CON SU PRIMER PRODUCTO
    // =====================================================

    public Pedido crearPedidoConItem(
            Cliente cliente,
            Presentacion presentacion,
            int cantidadUnidades,
            int cantidadCajones,
            TipoCajon tipoCajon)
            throws PersistenciaException, ReglaNegocioException {

        verificarSesion();

        validarCliente(cliente);

        DetallePedido detalle = construirDetalle(
                presentacion,
                cantidadUnidades,
                cantidadCajones,
                tipoCajon,
                null
        );

        Pedido pedido = new Pedido();

        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.BORRADOR);
        pedido.setFecha(LocalDateTime.now());

        pedido.agregarDetalle(detalle);

        // Se guarda el pedido junto con su primer detalle
        // dentro de la misma transacción.

        pedidoRepository.guardar(pedido);

        return pedido;
    }

    // =====================================================
    // AGREGAR PRODUCTO AL PEDIDO
    // =====================================================

    public void agregarItem(
            Pedido pedido,
            Presentacion presentacion,
            int cantidadUnidades,
            int cantidadCajones,
            TipoCajon tipoCajon,
            BigDecimal precioUnitarioPactado)
            throws PersistenciaException, ReglaNegocioException {

        verificarSesion();

        validarPedido(pedido);

        if (pedido.getEstado() != EstadoPedido.BORRADOR) {

            throw new ReglaNegocioException(
                    "Solo se pueden agregar productos "
                            + "a pedidos en estado BORRADOR."
            );
        }

        DetallePedido detalle = construirDetalle(
                presentacion,
                cantidadUnidades,
                cantidadCajones,
                tipoCajon,
                precioUnitarioPactado
        );

        pedido.agregarDetalle(detalle);

        try {

            pedidoRepository.actualizar(pedido);

        } catch (RuntimeException e) {

            // Si falla la persistencia, se elimina el detalle
            // agregado para mantener consistente el objeto Java.

            pedido.getDetalles().remove(detalle);

            throw e;
        }
    }

    // =====================================================
    // CONSTRUIR Y VALIDAR DETALLE
    // =====================================================

    private DetallePedido construirDetalle(
            Presentacion presentacion,
            int cantidadUnidades,
            int cantidadCajones,
            TipoCajon tipoCajon,
            BigDecimal precioUnitarioPactado)
            throws ReglaNegocioException, PersistenciaException {

        if (presentacion == null
                || presentacion.getId() == null
                || presentacion.getId().isBlank()) {

            throw new ReglaNegocioException(
                    "Debe seleccionar una presentación válida."
            );
        }

        if (cantidadUnidades <= 0) {

            throw new ReglaNegocioException(
                    "La cantidad de unidades debe ser mayor a cero."
            );
        }

        if (cantidadCajones < 0) {

            throw new ReglaNegocioException(
                    "La cantidad de cajones no puede ser negativa."
            );
        }

        if (cantidadCajones > 0
                && (tipoCajon == null
                || tipoCajon.getId() == null
                || tipoCajon.getId().isBlank())) {

            throw new ReglaNegocioException(
                    "Debe seleccionar el tipo de cajón."
            );
        }

        if (cantidadCajones == 0) {

            tipoCajon = null;
        }

        Presentacion presentacionVigente =
                buscarPresentacionDisponible(
                        presentacion.getId()
                );

        BigDecimal precioCatalogo =
                presentacionVigente.getPrecioLlenado();

        if (precioCatalogo == null
                || precioCatalogo.signum() <= 0
                || precioCatalogo.stripTrailingZeros().scale() > 2) {

            throw new ReglaNegocioException(
                    "La presentación no tiene un precio válido."
            );
        }

        if (precioUnitarioPactado != null
                && precioUnitarioPactado.compareTo(precioCatalogo) != 0) {

            throw new ReglaNegocioException(
                    "El precio debe coincidir "
                            + "con el precio vigente del catálogo."
            );
        }

        return new DetallePedido(
                presentacionVigente,
                cantidadUnidades,
                precioCatalogo,
                cantidadCajones,
                tipoCajon
        );
    }

    // =====================================================
    // BUSCAR PRESENTACIÓN DISPONIBLE
    // =====================================================

    private Presentacion buscarPresentacionDisponible(String id)
            throws PersistenciaException, ReglaNegocioException {

        for (Presentacion presentacion :
                listarPresentacionesDisponibles()) {

            if (id.equals(presentacion.getId())) {

                return presentacion;
            }
        }

        throw new ReglaNegocioException(
                "La presentación no existe "
                        + "o se encuentra desactivada."
        );
    }

    // =====================================================
    // CONFIRMAR PEDIDO
    // =====================================================

    public void confirmarPedido(Pedido pedido)
            throws PersistenciaException, ReglaNegocioException {

        verificarSesion();

        validarPedido(pedido);

        Pedido pedidoExistente =
                pedidoRepository.buscarPorId(pedido.getId());

        if (pedidoExistente == null) {

            throw new ReglaNegocioException(
                    "No existe el pedido indicado."
            );
        }

        if (pedidoExistente.getEstado()
                != EstadoPedido.BORRADOR) {

            throw new ReglaNegocioException(
                    "Solo se pueden confirmar pedidos "
                            + "en estado BORRADOR."
            );
        }

        if (pedidoExistente.getDetalles().isEmpty()) {

            throw new ReglaNegocioException(
                    "No se puede confirmar un pedido sin detalles."
            );
        }

        // Utilizar la regla de negocio definida en el modelo.

        pedidoExistente.confirmar();

        // Primero se confirma el cambio en MySQL.

        pedidoRepository.actualizar(pedidoExistente);

        // Solo después de guardar correctamente
        // se modifica el objeto utilizado por el controlador.

        pedido.setEstado(EstadoPedido.CONFIRMADO);
    }

    // =====================================================
    // CANCELAR PEDIDO
    // =====================================================

    public void cancelarPedido(Pedido pedido)
            throws PersistenciaException, ReglaNegocioException {

        verificarSesion();

        if (!SesionUsuario.esAdministrador()) {

            throw new ReglaNegocioException(
                    "Solo un administrador puede cancelar pedidos."
            );
        }

        validarPedido(pedido);

        Pedido pedidoExistente =
                pedidoRepository.buscarPorId(pedido.getId());

        if (pedidoExistente == null) {

            throw new ReglaNegocioException(
                    "El pedido no existe."
            );
        }

        EstadoPedido estado = pedidoExistente.getEstado();

        if (estado != EstadoPedido.BORRADOR
                && estado != EstadoPedido.CONFIRMADO) {

            throw new ReglaNegocioException(
                    "Solo se pueden cancelar pedidos "
                            + "en BORRADOR o CONFIRMADO."
            );
        }

        // El repositorio verifica adicionalmente
        // que el pedido no tenga retiros registrados.

        pedidoRepository.desactivar(pedido.getId());

        pedido.setEstado(EstadoPedido.CANCELADO);
    }

    // =====================================================
    // LISTADOS
    // =====================================================

    public List<Pedido> listarPedidos()
            throws PersistenciaException {

        verificarSesion();

        return pedidoRepository.listar();
    }

    public List<Presentacion> listarPresentacionesDisponibles()
            throws PersistenciaException {

        verificarSesion();

        return catalogoRepository.listar();
    }

    // =====================================================
    // VALIDACIONES GENERALES
    // =====================================================

    private void validarCliente(Cliente cliente)
            throws ReglaNegocioException {

        if (cliente == null
                || cliente.getId() == null
                || cliente.getId().isBlank()) {

            throw new ReglaNegocioException(
                    "Debe seleccionar un repartidor válido."
            );
        }
    }

    private void validarPedido(Pedido pedido)
            throws ReglaNegocioException {

        if (pedido == null
                || pedido.getId() == null
                || pedido.getId().isBlank()) {

            throw new ReglaNegocioException(
                    "Debe indicar un pedido válido."
            );
        }
    }

    private void verificarSesion() {

        if (!SesionUsuario.haySesionActiva()) {

            throw new IllegalStateException(
                    "No hay una sesión activa."
            );
        }
    }
}