package ar.edu.utn.tpfinalsoderia.exceptions;

public class ReglaNegocioException extends Exception
{
    public ReglaNegocioException(String message)
    {
        super(message);
    }

    public ReglaNegocioException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
