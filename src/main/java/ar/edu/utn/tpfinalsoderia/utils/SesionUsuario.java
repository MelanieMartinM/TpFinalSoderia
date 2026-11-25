package ar.edu.utn.tpfinalsoderia.utils;

import ar.edu.utn.tpfinalsoderia.enums.RolUsuario;
import ar.edu.utn.tpfinalsoderia.models.Usuario;

public final class SesionUsuario {

    private static Usuario usuarioActual;

    private SesionUsuario() {
    }

    public static void iniciarSesion(Usuario usuario) {
        if (usuario == null || !usuario.isActivo()) {
            throw new IllegalArgumentException(
                    "El usuario no es válido o está desactivado."
            );
        }

        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static boolean haySesionActiva() {
        return usuarioActual != null
                && usuarioActual.isActivo();
    }

    public static boolean esAdministrador() {
        return haySesionActiva()
                && usuarioActual.getRol()
                == RolUsuario.ADMINISTRADOR;
    }

    public static boolean esOperario() {
        return haySesionActiva()
                && usuarioActual.getRol()
                == RolUsuario.OPERARIO;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}