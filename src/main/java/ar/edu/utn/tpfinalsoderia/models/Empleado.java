package ar.edu.utn.tpfinalsoderia.models;

public class Empleado extends Persona{

    private String legajo;

    public Empleado(String direccion, String id, String nombre, String telefono, String legajo) {
        super(direccion,id,nombre,telefono);
        this.legajo = legajo;
    }

    public String getLegajo() {
        return legajo;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }
}
