package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.DAO.UsuarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Usuario;
import ar.edu.utn.tpfinalsoderia.utils.SeguridadContrasena;

import java.sql.Connection;
import java.sql.SQLException;

public class ServicioLogin {

    public Usuario iniciarSesion(
            String nombreUsuario,
            String contrasena) {

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

            boolean contrasenaCorrecta =
                    SeguridadContrasena.verificarContrasena(
                            contrasena,
                            usuario.getHashContrasena()
                    );

            if (!contrasenaCorrecta) {
                return null;
            }

            return usuario;

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo conectar para iniciar sesión.", e
            );
        }
    }
}