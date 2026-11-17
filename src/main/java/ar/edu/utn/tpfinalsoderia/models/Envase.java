package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.enums.MaterialEnvase;
import ar.edu.utn.tpfinalsoderia.enums.TipoEnvase;
import ar.edu.utn.tpfinalsoderia.interfaces.Retornable;

public class Envase implements Retornable {



    private String id;
    private  String descripcion;
    private TipoEnvase tipoEnvase;
    private MaterialEnvase material;
    private double capacidadLTS;


    public Envase() {
    }

    public Envase(String id, String descripcion, TipoEnvase tipoEnvase, MaterialEnvase material, double capacidadLTS) {
        this.id = id;
        this.descripcion = descripcion;
        this.tipoEnvase = tipoEnvase;
        this.material = material;
        this.capacidadLTS = capacidadLTS;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoEnvase getTipoEnvase() {
        return tipoEnvase;
    }

    public void setTipoEnvase(TipoEnvase tipoEnvase) {
        this.tipoEnvase = tipoEnvase;
    }

    public MaterialEnvase getMaterial() {
        return material;
    }

    public void setMaterial(MaterialEnvase material) {
        this.material = material;
    }

    public double getCapacidadLTS() {
        return capacidadLTS;
    }

    public void setCapacidadLTS(double capacidadLTS) {
        this.capacidadLTS = capacidadLTS;
    }

    @Override
    public String toString() {
        return "Envase{" +
                "id='" + id + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tipoEnvase=" + tipoEnvase +
                ", material=" + material +
                ", capacidadLTS=" + capacidadLTS +
                '}';
    }

    @Override
    public String getCodigo() {
        return this.id;
    }

    @Override
    public String getDescripcion() {
        return this.descripcion;
    }
}
