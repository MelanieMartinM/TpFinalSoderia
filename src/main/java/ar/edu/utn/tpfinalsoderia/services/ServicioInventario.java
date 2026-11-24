package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.interfaces.Retornable;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.Marca;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.StockProducto;
import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ServicioInventario
{

    private final Connection conexion;
    private final InventarioRepositoryJdbc inventarioRepository;

    public ServicioInventario(Connection conexion, InventarioRepositoryJdbc inventarioRepository)
    {
        this.conexion = Objects.requireNonNull(conexion, "La conexión no puede ser nula.");
        this.inventarioRepository = Objects.requireNonNull(inventarioRepository, "El repositorio de inventario no puede ser nulo.");
    }

    public void recibirVacios(Cliente cliente, Envase envase, int cantidad) throws ReglaNegocioException, PersistenciaException
    {
        validarCantidadPositiva(cantidad);
        validarEnvase(envase);

        try
        {
            conexion.setAutoCommit(false);

            conexion.commit();

        } catch (SQLException e)
        {
            hacerRollback();
            throw new PersistenciaException("Error al registrar la recepción de envases vacíos.", e);
        } finally
        {
            restaurarAutoCommit();
        }
    }

    public void lavarEnvases(Cliente cliente, Envase envase, int cantidad) throws ReglaNegocioException, PersistenciaException
    {
        validarCantidadPositiva(cantidad);
        validarEnvase(envase);

        try {
            conexion.setAutoCommit(false);

            conexion.commit();
        } catch (SQLException e)
        {
            hacerRollback();
            throw new PersistenciaException("Error al registrar el lavado de envases.", e);
        } finally
        {
            restaurarAutoCommit();
        }
    }

    public void registrarProduccion (Cliente cliente, Presentacion presentacion, int cantidad) throws ReglaNegocioException, StockInsuficienteException, PersistenciaException
    {
        validarCantidadPositiva(cantidad);
        validarPresentacion(presentacion);
        validarTitularidadMarca(cliente, presentacion.getMarca());

        try
        {
            conexion.setAutoCommit(false);
            conexion.commit();

        } catch (SQLException e)
        {
            hacerRollback();
            throw new PersistenciaException("Error al registrar la producción.", e);
        } finally
        {
            restaurarAutoCommit();
        }
    }

    public List<StockProducto> consultarStock(Cliente cliente) throws PersistenciaException
    {
        try
        {
            return List.of();

        } catch (RuntimeException e)
        {
            throw new PersistenciaException("Error al consultar el stock de productos.", e);
        }
    }

    public String describir(Retornable elemento)
    {
        if (elemento == null)
        {
            return "";
        }

        return elemento.getCodigo() + " - " + elemento.getDescripcion();
    }

    private void validarCantidadPositiva(int cantidad) throws ReglaNegocioException
    {
        if (cantidad <= 0)
        {
            throw new ReglaNegocioException("La cantidad debe ser mayor a cero.");
        }
    }

    private void validarEnvase(Envase envase) throws ReglaNegocioException
    {
        if (envase == null)
        {
            throw new ReglaNegocioException("Debe indicar un envase válido.");
        }
    }

    private void validarPresentacion(Presentacion presentacion) throws ReglaNegocioException
    {
        if (presentacion == null)
        {
            throw new ReglaNegocioException("Debe indicar una presentación válida.");
        }
    }

    private void validarTitularidadMarca(Cliente clienteOperacion, Marca marca) throws ReglaNegocioException
    {
        if (marca == null)
        {
            return;
        }
        if (marca.isEsPropiaFabrica())
        {
            if (clienteOperacion != null)
            {
                throw new ReglaNegocioException("La marca es propia de fábrica y no corresponde a un cliente fasón.");
            }
        } else
        {
            if (clienteOperacion == null)
            {
                throw new ReglaNegocioException("La marca es de fasón y requiere un cliente titular.");
            }
            if (marca.getTitular() != null && marca.getTitular().getId() != null && clienteOperacion.getId() != null && !Objects.equals(marca.getTitular().getId(), clienteOperacion.getId()))
            {
                throw new ReglaNegocioException("La marca pertenece a otro cliente titular.");
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
