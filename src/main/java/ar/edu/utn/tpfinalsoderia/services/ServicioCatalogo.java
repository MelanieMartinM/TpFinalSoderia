package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.CatalogoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.MarcaRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.ProductoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Marca;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.Producto;

import java.math.BigDecimal;
import java.util.List;

public class ServicioCatalogo {

    private final ProductoRepositoryJdbc productoRepositoryJdbc;
    private final MarcaRepositoryJdbc marcaRepositoryJdbc;
    private final CatalogoRepositoryJdbc catalogoRepositoryJdbc;
    public ServicioCatalogo(ProductoRepositoryJdbc productoRepositoryJdbc,
                            MarcaRepositoryJdbc marcaRepositoryJdbc,CatalogoRepositoryJdbc catalogoRepositoryJdbc) {
        if (productoRepositoryJdbc == null) {
            throw new IllegalArgumentException("El repositorio de productos no puede ser nulo.");
        }
        if (marcaRepositoryJdbc == null) {
            throw new IllegalArgumentException("El repositorio de marcas no puede ser nulo.");
        }if (catalogoRepositoryJdbc == null) {
            throw new IllegalArgumentException("El repositorio de catálogo no puede ser nulo.");
        }
        this.catalogoRepositoryJdbc= catalogoRepositoryJdbc;
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
    // --- Presentaciones ---

    public List<Presentacion> listarPresentaciones() throws PersistenciaException {
        return catalogoRepositoryJdbc.listar();
    }

    public void actualizarPrecio(Presentacion presentacion, BigDecimal precio) throws PersistenciaException {
        if (presentacion == null) {
            throw new IllegalArgumentException("La presentación no puede ser nula.");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser nulo ni negativo.");
        }

        presentacion.setPrecioLlenado(precio);
        catalogoRepositoryJdbc.actualizar(presentacion);
    }

    public void desactivarPresentacion(Presentacion presentacion) throws PersistenciaException {
        if (presentacion == null) {
            throw new IllegalArgumentException("La presentación no puede ser nula.");
        }
        if (presentacion.getId() == null || presentacion.getId().isBlank()) {
            throw new IllegalArgumentException("El ID de la presentación es obligatorio para desactivarla.");
        }

        presentacion.setActiva(false);
        catalogoRepositoryJdbc.eliminar(presentacion.getId().trim());
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
        if (marca.getId() == null || marca.getId().isBlank()) {
            throw new IllegalArgumentException("El ID de la marca es obligatorio.");
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