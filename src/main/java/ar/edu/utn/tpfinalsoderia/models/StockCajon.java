package ar.edu.utn.tpfinalsoderia.models;

public class StockCajon
{
    private TipoCajon tipo;
    private int disponiblesFabrica;

    public StockCajon() {}

    public StockCajon(TipoCajon tipo, int disponiblesFabrica) {
        this.tipo = tipo;
        this.disponiblesFabrica = disponiblesFabrica;
    }

    public TipoCajon getTipo() { return tipo; }
    public void setTipo(TipoCajon tipo) { this.tipo = tipo; }
    public int getDisponiblesFabrica() { return disponiblesFabrica; }
    public void setDisponiblesFabrica(int disponiblesFabrica) { this.disponiblesFabrica = disponiblesFabrica; }
}
