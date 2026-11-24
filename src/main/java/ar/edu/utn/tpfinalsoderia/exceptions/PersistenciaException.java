package ar.edu.utn.tpfinalsoderia.exceptions;

public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String message) {
        super(message);
    }


  public PersistenciaException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
