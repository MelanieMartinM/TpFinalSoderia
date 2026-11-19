package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.enums.RolUsuario;

public class Usuario {

    private String id;
    private String nombreUsuario;
    private String hashContrasena; //representa un hash ya generado; no le pasen una contraseña en texto como si ya fuera un hash. La implementación de verificarContrasena() queda pendiente para la tarea de acceso, junto con la decisión de cómo almacenar y verificar contraseñas.
    private RolUsuario rol;
    private boolean activo;

    public Usuario(
            String id,
            String nombreUsuario,
            String hashContrasena,
            RolUsuario rol
    ) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.hashContrasena = hashContrasena;
        this.rol = rol;
        this.activo = true;
    }


    //permite reconstruir un usuario que viene de la base, respetando si está activo o no.
    public Usuario(
            String id,
            String nombreUsuario,
            String hashContrasena,
            RolUsuario rol,
            boolean activo
    ) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.hashContrasena = hashContrasena;
        this.rol = rol;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void desactivar() {
        this.activo = false;
    }
}

