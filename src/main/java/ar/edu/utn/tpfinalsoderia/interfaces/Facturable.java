package ar.edu.utn.tpfinalsoderia.interfaces;

import java.math.BigDecimal;

public interface Facturable
{
    BigDecimal calcularTotal();
    String generarComprobante();
}
