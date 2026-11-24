package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.CajonDAO;
import ar.edu.utn.tpfinalsoderia.DAO.RetiroCobroRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.StockEnvaseDAO;
import ar.edu.utn.tpfinalsoderia.DAO.StockProductoDAO;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.models.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ServicioRetirosCobros
{
    private final Connection conexion;
    private final RetiroCobroRepositoryJdbc retiroCobroRepo;
    private final StockProductoDAO stockProductoDAO;
    private final StockEnvaseDAO stockEnvaseDAO;
    private final CajonDAO cajonDAO;

    public ServicioRetirosCobros(Connection conexion, RetiroCobroRepositoryJdbc retiroCobroRepo, StockProductoDAO stockProductoDAO, StockEnvaseDAO stockEnvaseDAO, CajonDAO cajonDAO)
    {
        this.conexion = conexion;
        this.retiroCobroRepo = retiroCobroRepo;
        this.stockProductoDAO = stockProductoDAO;
        this.stockEnvaseDAO = stockEnvaseDAO;
        this.cajonDAO = cajonDAO;
    }

    public void registrarRetiroYCobro(Retiro retiro, Cobro cobro) throws ReglaNegocioException, StockInsuficienteException
    {
        if (retiro == null || retiro.getDetalles() == null || retiro.getDetalles().isEmpty())
        {
            throw new ReglaNegocioException("El retiro debe tener al menos un detalle.");
        }

        Cliente titular = validarTitular(retiro.getCliente());
        retiro.setCliente(titular);

        for (DetalleRetiro det : retiro.getDetalles())
        {
            if (det.getCantidadRetirada() <= 0)
            {
                throw new ReglaNegocioException("La cantidad retirada debe ser mayor a cero.");
            }
            if (det.getPrecioAplicado() < 0)
            {
                throw new ReglaNegocioException("El precio aplicado no puede ser negativo.");
            }
            if (det.getPresentacion() == null || det.getPresentacion().getId() == null || det.getPresentacion().getId().isBlank())
            {
                throw new ReglaNegocioException("Cada detalle del retiro debe indicar una presentación.");
            }

            Presentacion presCompleta = stockProductoDAO.buscarPresentacionCompleta(det.getPresentacion().getId());
            if (presCompleta == null)
            {
                throw new ReglaNegocioException("La presentación '" + det.getPresentacion().getId() + "' no existe.");
            }
            det.setPresentacion(presCompleta);

            if (det.getDetallePedidoId() != null && !det.getDetallePedidoId().isBlank())
            {
                int yaRetirado = retiroCobroRepo.obtenerCantidadYaRetirada(det.getDetallePedidoId());

                if (det.getCantidadPedida() > 0 && (yaRetirado + det.getCantidadRetirada()) > det.getCantidadPedida())
                {
                    throw new ReglaNegocioException(
                            "La cantidad retirada supera lo pedido. Pedido: " + det.getCantidadPedida()
                                    + ", ya retirado: " + yaRetirado
                    );
                }
            }
        }

        float totalRetiro = retiro.calcularTotal();
        validarCobro(cobro, totalRetiro);

        try
        {
            conexion.setAutoCommit(false);

            String clienteId = (titular != null) ? titular.getId() : null;

            for (DetalleRetiro det : retiro.getDetalles())
            {
                StockProducto stockProd = stockProductoDAO.buscarPorTitularYPresentacion(clienteId, det.getPresentacion().getId());

                int disponibles = (stockProd == null) ? 0 : stockProd.getDisponibles();

                if (disponibles < det.getCantidadRetirada())
                {
                    throw new StockInsuficienteException(
                            "Stock insuficiente para '" + det.getPresentacion().getNombre()
                                    + "'. Disponible: " + disponibles + ", solicitado: " + det.getCantidadRetirada()
                    );
                }
                stockProd.setDisponibles(disponibles - det.getCantidadRetirada());
                stockProductoDAO.actualizar(stockProd);

                if (det.getCantidadCajones() > 0)
                {
                    if (det.getTipoCajon() == null || det.getTipoCajon().getId() == null || det.getTipoCajon().getId().isBlank())
                    {
                        throw new ReglaNegocioException("Debe indicar el tipo de cajón cuando retira cajones.");
                    }

                    TipoCajon tipoCajon = cajonDAO.buscarTipoCajonPorId(det.getTipoCajon().getId());

                    if (tipoCajon == null)
                    {
                        throw new ReglaNegocioException("El tipo de cajón '" + det.getTipoCajon().getId() + "' no existe.");
                    }

                    StockCajon stockCajon = cajonDAO.buscarStockPorTipo(tipoCajon.getId());

                    int cajonesDisp = (stockCajon == null) ? 0 : stockCajon.getDisponiblesFabrica();

                    if (cajonesDisp < det.getCantidadCajones())
                    {
                        throw new StockInsuficienteException(
                                "Cajones insuficientes en fábrica para '" + tipoCajon.getDescripcion()
                                        + "'. Disponibles: " + cajonesDisp
                        );
                    }
                    cajonDAO.guardarOActualizarStock(new StockCajon(tipoCajon, cajonesDisp - det.getCantidadCajones()));

                    cajonDAO.registrarMovimiento(new MovimientoCajon
                            (
                            UUID.randomUUID().toString(),
                            titular,
                            tipoCajon,
                            det.getCantidadCajones(),
                            LocalDateTime.now(),
                            false
                    ));
                }
            }

            if (retiro.getId() == null || retiro.getId().isBlank())
            {
                retiro.setId(UUID.randomUUID().toString());
            }
            if (retiro.getFecha() == null)
            {
                retiro.setFecha(LocalDateTime.now());
            }

            retiroCobroRepo.guardarRetiro(retiro);

            if (cobro.getId() == null || cobro.getId().isBlank())
            {
                cobro.setId(UUID.randomUUID().toString());
            }
            if (cobro.getFecha() == null)
            {
                cobro.setFecha(LocalDateTime.now());
            }

            retiro.setCobro(cobro);
            retiroCobroRepo.guardarCobro(retiro.getId(), cobro);

            conexion.commit();
        }
        catch (ReglaNegocioException | StockInsuficienteException | RuntimeException e)
        {
            hacerRollback();
            throw e;
        } catch (SQLException e)
        {
            hacerRollback();
            throw new PersistenciaException("Error en la transacción de retiro y cobro", e);
        } finally
        {
            restaurarAutoCommit();
        }
    }

    public List<Cobro> listarCobros()
    {
        return retiroCobroRepo.listarCobros();
    }

    private Cliente validarTitular(Cliente cliente) throws ReglaNegocioException
    {
        if (cliente == null || cliente.getId() == null || cliente.getId().isBlank())
        {
            return null;
        }
        Cliente encontrado = stockEnvaseDAO.buscarClientePorId(cliente.getId());
        if (encontrado == null)
        {
            throw new ReglaNegocioException("El cliente con ID '" + cliente.getId() + "' no existe.");
        }
        if (!encontrado.isActivo())
        {
            throw new ReglaNegocioException("El cliente '" + encontrado.getNombre() + "' se encuentra inactivo.");
        }
        return encontrado;
    }

    private void validarCobro(Cobro cobro, float totalRetiro) throws ReglaNegocioException
    {
        if (cobro == null)
        {
            throw new ReglaNegocioException("Debe registrar un cobro o indicar autorización sin cobro.");
        }
        if (cobro.isSinCobroAutorizado())
        {
            if (cobro.getResponsableAutorizacion() == null || cobro.getResponsableAutorizacion().isBlank())
            {
                throw new ReglaNegocioException("Para autorizar sin cobro debe indicar el responsable.");
            }
            if (cobro.getMotivoAutorizacion() == null || cobro.getMotivoAutorizacion().isBlank())
            {
                throw new ReglaNegocioException("Para autorizar sin cobro debe indicar el motivo.");
            }
            cobro.setMonto(0f);
        } else
        {
            if (cobro.getMedioPago() == null)
            {
                throw new ReglaNegocioException("Debe seleccionar un medio de pago.");
            }
            if (cobro.getMonto() <= 0 || cobro.getMonto() < totalRetiro)
            {
                throw new ReglaNegocioException("El monto cobrado ($" + cobro.getMonto() + ") debe cubrir el total del retiro ($" + totalRetiro + ").");
            }
        }
    }

    private void hacerRollback()
    {
        try
        {
            conexion.rollback();

        } catch (SQLException ignored)
        {

        }
    }

    private void restaurarAutoCommit()
    {
        try
        {
            conexion.setAutoCommit(true);

        } catch (SQLException ignored)
        {

        }
    }
}