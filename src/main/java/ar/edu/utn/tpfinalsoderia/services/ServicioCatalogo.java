package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.MarcaRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.ProductoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Marca;
import ar.edu.utn.tpfinalsoderia.models.Producto;

import java.util.List;

public class ServicioCatalogo {

    private final ProductoRepositoryJdbc productoRepositoryJdbc;
    private final MarcaRepositoryJdbc marcaRepositoryJdbc;

    public ServicioCatalogo(ProductoRepositoryJdbc productoRepositoryJdbc,
                            MarcaRepositoryJdbc marcaRepositoryJdbc) {
        if (productoRepositoryJdbc == null) {
            throw new IllegalArgumentException("El repositorio de productos no puede ser nulo.");
        }
        if (marcaRepositoryJdbc == null) {
            throw new IllegalArgumentException("El repositorio de marcas no puede ser nulo.");
        }
        this.productoRepositoryJdbc = productoRepositoryJdbc;
        this.marcaRepositoryJdbc = marcaRepositoryJdbc;
    }

    // --- Productos ---

    public void registrarProducto(Producto producto) throws PersistenciaException {
        validarProducto(producto);
        productoRepositoryJdbc.guardar(producto);
    }

    public void actualizarProducto(Producto producto) throws PersistenciaException {
        validarProducto(producto);
        productoRepositoryJdbc.actualizar(producto);
    }

    public Producto buscarProductoPorId(String id) throws PersistenciaException {
        validarIdTexto(id, "producto");
        return productoRepositoryJdbc.buscarPorId(id.trim());
    }

    public List<Producto> listarProductosActivos() throws PersistenciaException {
        return productoRepositoryJdbc.listar();
    }

    public void desactivarProducto(Producto producto) throws PersistenciaException {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (producto.getId() == null || producto.getId().isBlank()) {
            throw new IllegalArgumentException("El ID del producto es obligatorio para desactivarlo.");
        }

        producto.setActivo(false);
        productoRepositoryJdbc.eliminar(producto.getId().trim());
    }

    // --- Marcas ---

    public void registrarMarca(Marca marca) throws PersistenciaException {
        validarMarca(marca);
        marcaRepositoryJdbc.guardar(marca);
    }

    public void actualizarMarca(Marca marca) throws PersistenciaException {
        validarMarca(marca);
        marcaRepositoryJdbc.actualizar(marca);
    }

    public Marca buscarMarcaPorId(String id) throws PersistenciaException {
        validarIdTexto(id, "marca");

        return marcaRepositoryJdbc.buscarPorId(id);
    }

    public List<Marca> listarMarcasActivas() throws PersistenciaException {
        return marcaRepositoryJdbc.listar();
    }

    public void desactivarMarca(String id) throws PersistenciaException {
        validarIdTexto(id, "marca");

        marcaRepositoryJdbc.eliminar(id);
    }

    // --- Validaciones ---

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (producto.getId() == null || producto.getId().isBlank()) {
            throw new IllegalArgumentException("El ID del producto es obligatorio.");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
    }

    private void validarMarca(Marca marca) {
        if (marca == null) {
            throw new IllegalArgumentException("La marca no puede ser nula.");
        }
        if (marca.getId() == null) {
            throw new IllegalArgumentException("El ID de la marca debe ser mayor a cero.");
        }
        if (marca.getNombre() == null || marca.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la marca es obligatorio.");
        }
    }

    private void validarIdTexto(String id, String entidad) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de " + entidad + " es obligatorio.");
        }
    }

}