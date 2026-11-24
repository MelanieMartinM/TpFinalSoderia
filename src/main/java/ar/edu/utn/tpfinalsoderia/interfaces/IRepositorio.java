package ar.edu.utn.tpfinalsoderia.interfaces;

import java.util.List;

public interface IRepositorio <T>{

    void guardar(T objeto);

    T buscarPorId(String id);

    List<T> listar();

    void actualizar(T objeto);

    void eliminar(String id);

    /** Baja lógica cuando la entidad admite desactivación. */
    default void desactivar(String id) {
        throw new UnsupportedOperationException("Esta entidad no admite baja lógica.");
    }

}
