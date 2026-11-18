package ar.edu.utn.tpfinalsoderia.exceptions;

public class StockInsuficienteException extends Exception
{
    public StockInsuficienteException(String message)
    {
        super(message);
    }

    public StockInsuficienteException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
