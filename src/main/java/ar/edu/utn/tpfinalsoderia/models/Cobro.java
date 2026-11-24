package ar.edu.utn.tpfinalsoderia.models;

import ar.edu.utn.tpfinalsoderia.enums.MedioDePago;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Objects;

public class Cobro
{
    private String id;
    private BigDecimal monto;
    private MedioDePago medioPago;
    private LocalDateTime fecha;
    private boolean sinCobroAutorizado;
    private String responsableAutorizacion;
    private String motivoAutorizacion;

    public Cobro()
    {

    }

    public Cobro(String id, BigDecimal monto, MedioDePago medioPago, LocalDateTime fecha, boolean sinCobroAutorizado, String responsableAutorizacion, String motivoAutorizacion)
    {
        this.id = id;
        this.monto = monto;
        this.medioPago = medioPago;
        this.fecha = fecha;
        this.sinCobroAutorizado = sinCobroAutorizado;
        this.responsableAutorizacion = responsableAutorizacion;
        this.motivoAutorizacion = motivoAutorizacion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public MedioDePago getMedioPago() { return medioPago; }
    public void setMedioPago(MedioDePago medioPago) { this.medioPago = medioPago; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public boolean isSinCobroAutorizado() { return sinCobroAutorizado; }
    public void setSinCobroAutorizado(boolean sinCobroAutorizado) { this.sinCobroAutorizado = sinCobroAutorizado; }
    public String getResponsableAutorizacion() { return responsableAutorizacion; }
    public void setResponsableAutorizacion(String responsableAutorizacion) { this.responsableAutorizacion = responsableAutorizacion; }
    public String getMotivoAutorizacion() { return motivoAutorizacion; }
    public void setMotivoAutorizacion(String motivoAutorizacion) { this.motivoAutorizacion = motivoAutorizacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cobro other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}