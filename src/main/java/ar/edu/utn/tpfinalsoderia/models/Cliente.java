package ar.edu.utn.tpfinalsoderia.models;

public class Cliente extends Persona{

    private boolean activo;

    public Cliente() {
        this.activo=true;
    }

    public Cliente(String direccion, String id, String nombre, String telefono) {
        super(direccion, id, nombre, telefono);
        this.activo = true ;
    }

    public boolean isActivo() {
        return activo;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}

