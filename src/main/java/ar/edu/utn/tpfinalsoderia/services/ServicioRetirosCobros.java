package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.RetiroCobroRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Cobro;
import ar.edu.utn.tpfinalsoderia.models.DetalleRetiro;
import ar.edu.utn.tpfinalsoderia.models.MovimientoCajon;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.Retiro;
import ar.edu.utn.tpfinalsoderia.models.StockCajon;
import ar.edu.utn.tpfinalsoderia.models.StockProducto;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ServicioRetirosCobros {

    private final Connection conexion;
    private final RetiroCobroRepositoryJdbc retiroCobroRepo;
    private final InventarioRepositoryJdbc inventarioRepo;

    public ServicioRetirosCobros(
            Connection conexion,
            RetiroCobroRepositoryJdbc retiroCobroRepo,
            InventarioRepositoryJdbc inventarioRepo
    ) {
        this.conexion = Objects.requireNonNull(conexion, "La conexión no puede ser nula.");
        this.retiroCobroRepo = Objects.requireNonNull(retiroCobroRepo, "El repositorio de retiros y cobros no puede ser nulo.");
        this.inventarioRepo = Objects.requireNonNull(inventarioRepo, "El repositorio de inventario no puede ser nulo.");
    }

    public void registrarRetiroYCobro(Retiro retiro, Cobro cobro)
            throws ReglaNegocioException, StockInsuficienteException {
        if (retiro == null || retiro.getDetalles() == null || retiro.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("El retiro debe tener al menos un detalle.");
        }

        Cliente titular = retiro.getCliente();

        for (DetalleRetiro det : retiro.getDetalles()) {
            if (det == null) {
                throw new ReglaNegocioException("Hay un detalle nulo en el retiro.");
            }
            if (det.getCantidadRetirada() <= 0) {
                throw new ReglaNegocioException("La cantidad retirada debe ser mayor a cero.");
            }
            if (det.getPrecioAplicado() < 0) {
                throw new ReglaNegocioException("El precio aplicado no puede ser negativo.");
            }
            Presentacion pres = det.getPresentacion();
            if (pres == null || pres.getId() == null || pres.getId().isBlank()) {
                throw new ReglaNegocioException("Cada detalle del retiro debe indicar una presentación.");
            }

            if (det.getDetallePedidoId() != null && !det.getDetallePedidoId().isBlank()) {
                int yaRetirado = retiroCobroRepo.obtenerCantidadYaRetirada(det.getDetallePedidoId());
                if (det.getCantidadPedida() > 0 && (yaRetirado + det.getCantidadRetirada()) > det.getCantidadPedida()) {
                    throw new ReglaNegocioException(
                            "La cantidad retirada supera lo pedido. Pedido: " + det.getCantidadPedida()
                                    + ", ya retirado: " + yaRetirado
                    );
                }
            }
        }

        float totalRetiro = retiro.calcularTotal();
        validarCobro(cobro, totalRetiro);

        try {
            conexion.setAutoCommit(false);

            for (DetalleRetiro det : retiro.getDetalles()) {
                StockProducto stockProd = inventarioRepo.buscarStockProducto(titular, det.getPresentacion());
                int disponibles = (stockProd == null) ? 0 : stockProd.getDisponibles();

                if (disponibles < det.getCantidadRetirada()) {
                    throw new StockInsuficienteException(
                            "Stock insuficiente para '" + det.getPresentacion().getNombre()
                                    + "'. Disponible: " + disponibles + ", solicitado: " + det.getCantidadRetirada()
                    );
                }

                stockProd.setDisponibles(disponibles - det.getCantidadRetirada());
                inventarioRepo.guardarStockProducto(stockProd);

                if (det.getCantidadCajones() > 0) {
                    TipoCajon tipoCajon = det.getTipoCajon();
                    if (tipoCajon == null || tipoCajon.getId() == null || tipoCajon.getId().isBlank()) {
                        throw new ReglaNegocioException("Debe indicar el tipo de cajón cuando retira cajones.");
                    }

                    int cajonesDisp = inventarioRepo.saldoCajones(titular, tipoCajon);
                    if (cajonesDisp < det.getCantidadCajones()) {
                        throw new StockInsuficienteException(
                                "Cajones insuficientes para '" + tipoCajon.getDescripcion()
                                        + "'. Disponibles: " + cajonesDisp
                        );
                    }

                    inventarioRepo.guardarStockCajon(new StockCajon(tipoCajon, Math.max(0, cajonesDisp - det.getCantidadCajones())));
                    inventarioRepo.registrarMovimientoCajon(new MovimientoCajon(
                            UUID.randomUUID().toString(),
                            titular,
                            tipoCajon,
                            det.getCantidadCajones(),
                            LocalDateTime.now(),
                            false
                    ));
                }
            }

            if (retiro.getId() == null || retiro.getId().isBlank()) {
                retiro.setId(UUID.randomUUID().toString());
            }
            if (retiro.getFecha() == null) {
                retiro.setFecha(LocalDateTime.now());
            }

            retiroCobroRepo.guardarRetiro(retiro);

            if (cobro.getId() == null || cobro.getId().isBlank()) {
                cobro.setId(UUID.randomUUID().toString());
            }
            if (cobro.getFecha() == null) {
                cobro.setFecha(LocalDateTime.now());
            }

            retiro.setCobro(cobro);
            retiroCobroRepo.guardarCobro(retiro.getId(), cobro);

            conexion.commit();
        } catch (ReglaNegocioException | StockInsuficienteException | RuntimeException e) {
            hacerRollback();
            throw e;
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error en la transacción de retiro y cobro.", e);
        } finally {
            restaurarAutoCommit();
        }
    }

    public List<Cobro> listarCobros() {
        return retiroCobroRepo.listarCobros();
    }

    private void validarCobro(Cobro cobro, float totalRetiro) throws ReglaNegocioException {
        if (cobro == null) {
            throw new ReglaNegocioException("Debe registrar un cobro o indicar autorización sin cobro.");
        }
        if (cobro.isSinCobroAutorizado()) {
            if (cobro.getResponsableAutorizacion() == null || cobro.getResponsableAutorizacion().isBlank()) {
                throw new ReglaNegocioException("Para autorizar sin cobro debe indicar el responsable.");
            }
            if (cobro.getMotivoAutorizacion() == null || cobro.getMotivoAutorizacion().isBlank()) {
                throw new ReglaNegocioException("Para autorizar sin cobro debe indicar el motivo.");
            }
            cobro.setMonto(0f);
        } else {
            if (cobro.getMedioPago() == null) {
                throw new ReglaNegocioException("Debe seleccionar un medio de pago.");
            }
            if (cobro.getMonto() <= 0 || cobro.getMonto() < totalRetiro) {
                throw new ReglaNegocioException(
                        "El monto cobrado ($" + cobro.getMonto()
                                + ") debe cubrir el total del retiro ($" + totalRetiro + ")."
                );
            }
        }
    }

    private void hacerRollback() {
        try {
            conexion.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restaurarAutoCommit() {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}