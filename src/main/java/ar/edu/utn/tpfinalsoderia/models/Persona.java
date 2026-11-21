package ar.edu.utn.tpfinalsoderia.models;

public abstract class Persona {

    private String id;
    private String nombre;
    private String telefono;
    private String direccion;

    public Persona() {
    }

    public Persona(String direccion, String id, String nombre, String telefono) {
        this.direccion = direccion;
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
