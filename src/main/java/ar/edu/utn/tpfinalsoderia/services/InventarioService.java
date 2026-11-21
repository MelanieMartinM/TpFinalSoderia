package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.MovimientoEnvaseDAO;
import ar.edu.utn.tpfinalsoderia.DAO.StockEnvaseDAO;
import ar.edu.utn.tpfinalsoderia.DAO.StockProductoDAO;
import ar.edu.utn.tpfinalsoderia.enums.TipoMovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.models.*;

import java.time.LocalDateTime;
import java.util.List;

public class InventarioService
{
    private final StockEnvaseDAO stockEnvaseDAO;
    private final StockProductoDAO stockProductoDAO;
    private final MovimientoEnvaseDAO movimientoEnvaseDAO;

    public InventarioService(StockEnvaseDAO stockEnvaseDAO, StockProductoDAO stockProductoDAO, MovimientoEnvaseDAO movimientoEnvaseDAO)
    {
        this.stockEnvaseDAO = stockEnvaseDAO;
        this.stockProductoDAO = stockProductoDAO;
        this.movimientoEnvaseDAO = movimientoEnvaseDAO;
    }

    public void registrarRecepcionVacios(Cliente titular, Envase envase, int cantidad) throws ReglaNegocioException
    {
        validarCantidadPositiva(cantidad);

        if (envase == null)
        {
            throw new ReglaNegocioException("Debe seleccionar un envase válido.");
        }

        String clienteId = (titular != null) ? titular.getId() : null;
        StockEnvase stock = stockEnvaseDAO.buscarPorTitularYEnvase(clienteId, envase.getId());

        if (stock == null)
        {
            stock = new StockEnvase(0, titular, envase, 0, cantidad);
            stockEnvaseDAO.guardar(stock);
        } else
        {
            stock.acreditarVacios(cantidad);
            stockEnvaseDAO.actualizar(stock);
        }

        registrarMovimiento(titular, envase, TipoMovimientoEnvase.INGRESO, cantidad);
    }

    public void registrarLavado(Cliente titular, Envase envase, int cantidad) throws ReglaNegocioException, StockInsuficienteException
    {
        validarCantidadPositiva(cantidad);

        if (envase == null)
        {
            throw new ReglaNegocioException("Debe seleccionar un envase válido.");
        }

        String clienteId = (titular != null) ? titular.getId() : null;
        StockEnvase stock = stockEnvaseDAO.buscarPorTitularYEnvase(clienteId, envase.getId());

        if (stock == null || stock.getSucios() < cantidad)
        {
            int disponibles = (stock == null) ? 0 : stock.getSucios();
            throw new StockInsuficienteException("No hay suficientes envases sucios para lavar. Sucios disponibles: " + disponibles);
        }

        stock.setSucios(stock.getSucios() - cantidad);
        stock.setAptos(stock.getAptos() + cantidad);
        stockEnvaseDAO.actualizar(stock);

        registrarMovimiento(titular, envase, TipoMovimientoEnvase.LAVADO, cantidad);
    }


    public void registrarProduccion(Cliente titular, Presentacion presentacion, int cantidad) throws ReglaNegocioException, StockInsuficienteException
    {
        validarCantidadPositiva(cantidad);

        if (presentacion == null || presentacion.getEnvase() == null)
        {
            throw new ReglaNegocioException("Debe seleccionar una presentación con envase asociado.");
        }

        Envase envase = presentacion.getEnvase();
        String clienteId = (titular != null) ? titular.getId() : null;
        StockEnvase stockEnvase = stockEnvaseDAO.buscarPorTitularYEnvase(clienteId, envase.getId());

        if (stockEnvase == null)
        {
            throw new StockInsuficienteException("No existe stock de envases aptos para " + envase.getDescripcion());
        }

        stockEnvase.usarAptos(cantidad);
        stockEnvaseDAO.actualizar(stockEnvase);

        StockProducto stockProd = stockProductoDAO.buscarPorTitularYPresentacion(clienteId, presentacion.getId());

        if (stockProd == null)
        {
            stockProd = new StockProducto(0, titular, presentacion, cantidad);
            stockProductoDAO.guardar(stockProd);
        } else
        {
            stockProd.sumarProduccion(cantidad);
            stockProductoDAO.actualizar(stockProd);
        }

        registrarMovimiento(titular, envase, TipoMovimientoEnvase.PRODUCCION, cantidad);
    }

    public List<StockEnvase> listarStockEnvases()
    {
        return stockEnvaseDAO.listar();
    }

    public List<StockProducto> listarStockProductos()
    {
        return stockProductoDAO.listar();
    }

    public List<MovimientoEnvase> listarMovimientosEnvases()
    {
        return movimientoEnvaseDAO.listar();
    }

    private void registrarMovimiento(Cliente titular, Envase envase, TipoMovimientoEnvase tipo, int cantidad)
    {
        MovimientoEnvase mov = new MovimientoEnvase(0, titular, envase, tipo, cantidad, LocalDateTime.now());
        movimientoEnvaseDAO.guardar(mov);
    }

    private void validarCantidadPositiva(int cantidad) throws ReglaNegocioException
    {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a cero.");
        }
    }
}