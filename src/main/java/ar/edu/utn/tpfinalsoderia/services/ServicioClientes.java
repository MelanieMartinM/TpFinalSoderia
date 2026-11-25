package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.ClienteRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.utils.SesionUsuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ServicioClientes {

    public void guardar(Cliente cliente) {

        verificarAdministrador();
        validarDatos(cliente);

        if (cliente.getId() == null || cliente.getId().isBlank()) {
            cliente.setId(UUID.randomUUID().toString());
        }

        try (Connection conexion = ConexionFactory.abrirConexion()) {

            ClienteRepositoryJdbc repositorio =
                    new ClienteRepositoryJdbc(conexion);

            repositorio.guardar(cliente);

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo conectar para guardar el cliente.", e
            );
        }
    }

    public Cliente buscarPorId(String id) {

        verificarSesion();
        validarId(id);

        try (Connection conexion = ConexionFactory.abrirConexion()) {

            ClienteRepositoryJdbc repositorio =
                    new ClienteRepositoryJdbc(conexion);

            return repositorio.buscarPorId(id.trim());

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo conectar para buscar el cliente.", e
            );
        }
    }

    public List<Cliente> listar() {

        verificarSesion();

        try (Connection conexion = ConexionFactory.abrirConexion()) {

            ClienteRepositoryJdbc repositorio =
                    new ClienteRepositoryJdbc(conexion);

            return repositorio.listar();

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo conectar para listar los clientes.", e
            );
        }
    }

    public void actualizar(Cliente cliente) {

        verificarAdministrador();
        validarDatos(cliente);
        validarId(cliente.getId());

        try (Connection conexion = ConexionFactory.abrirConexion()) {

            ClienteRepositoryJdbc repositorio =
                    new ClienteRepositoryJdbc(conexion);

            repositorio.actualizar(cliente);

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo conectar para actualizar el cliente.", e
            );
        }
    }

    public void desactivar(String id) {

        verificarAdministrador();
        validarId(id);

        try (Connection conexion = ConexionFactory.abrirConexion()) {

            ClienteRepositoryJdbc repositorio =
                    new ClienteRepositoryJdbc(conexion);

            repositorio.eliminar(id.trim());

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo conectar para desactivar el cliente.", e
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

    private void verificarAdministrador() {

        verificarSesion();

        if (!SesionUsuario.esAdministrador()) {
            throw new IllegalArgumentException(
                    "No tenés permisos para realizar esta operación."
            );
        }
    }

    private void validarDatos(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "Debés ingresar un cliente."
            );
        }

        if (cliente.getNombre() == null
                || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del cliente es obligatorio."
            );
        }
    }

    private void validarId(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "El ID del cliente es obligatorio."
            );
        }
    }
}