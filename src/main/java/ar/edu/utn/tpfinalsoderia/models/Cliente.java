package ar.edu.utn.tpfinalsoderia.models;

public class Cliente extends Persona{

    private boolean activo;

    public Cliente(String direccion, String id, String nombre, String telefono, boolean activo) {
        super(direccion, id, nombre, telefono);
        this.activo = activo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void desactivar() {
        this.activo = false;
    }
}

