package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.DAO.UsuarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Usuario;

import java.sql.Connection;
import java.sql.SQLException;

public class ServicioLogin {

    /**
     * Busca el usuario y verifica si puede iniciar sesión.
     *
     * Por ahora, la verificación de contraseña está pendiente.
     * Por eso el método todavía no permite ingresar.
     */
    public Usuario iniciarSesion(String nombreUsuario, String contrasena) {

        if (nombreUsuario == null || nombreUsuario.isBlank()
                || contrasena == null || contrasena.isEmpty()) {
            return null;
        }

        try (Connection conexion = ConexionFactory.abrirConexion()) {

            UsuarioRepositoryJdbc repositorio =
                    new UsuarioRepositoryJdbc(conexion);

            Usuario usuario = repositorio.buscarPorNombreUsuario(
                    nombreUsuario.trim()
            );

            if (usuario == null || !usuario.isActivo()) {
                return null;
            }

            // PENDIENTE:
            // Verificar la contraseña ingresada contra el hash guardado.
            //
            // Hasta implementar esa comprobación, no se permite ingresar.
            return null;

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo conectar para iniciar sesión.", e
            );
        }
    }
}