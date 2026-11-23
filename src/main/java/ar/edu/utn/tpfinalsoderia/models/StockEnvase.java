package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;

public class StockEnvase {
    private String id;
    private Cliente titular;
    private Envase envase;
    private int aptos;
    private int sucios;

    public StockEnvase() {}

    public StockEnvase(String id, Cliente titular, Envase envase, int aptos, int sucios) {
        this.id = id;
        this.titular = titular;
        this.envase = envase;
        this.aptos = aptos;
        this.sucios = sucios;
    }

    public void acreditarVacios(int cantidad) {
        if (cantidad > 0) this.sucios += cantidad;
    }

    public void usarAptos(int cantidad) throws StockInsuficienteException {
        if (cantidad > this.aptos) {
            throw new StockInsuficienteException("Stock de envases aptos insuficiente. Disponible: " + this.aptos);
        }
        this.aptos -= cantidad;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Cliente getTitular() { return titular; }
    public void setTitular(Cliente titular) { this.titular = titular; }
    public Envase getEnvase() { return envase; }
    public void setEnvase(Envase envase) { this.envase = envase; }
    public int getAptos() { return aptos; }
    public void setAptos(int aptos) { this.aptos = aptos; }
    public int getSucios() { return sucios; }
    public void setSucios(int sucios) { this.sucios = sucios; }
}