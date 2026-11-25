package ar.edu.utn.tpfinalsoderia.services;

import ar.edu.utn.tpfinalsoderia.DAO.CatalogoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.MarcaRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.ProductoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Marca;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.Producto;
import ar.edu.utn.tpfinalsoderia.utils.SesionUsuario;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ServicioCatalogo {

    private final ProductoRepositoryJdbc productoRepositoryJdbc;
    private final MarcaRepositoryJdbc marcaRepositoryJdbc;
    private final CatalogoRepositoryJdbc catalogoRepositoryJdbc;

    public ServicioCatalogo(
            ProductoRepositoryJdbc productoRepositoryJdbc,
            MarcaRepositoryJdbc marcaRepositoryJdbc,
            CatalogoRepositoryJdbc catalogoRepositoryJdbc) {

        if (productoRepositoryJdbc == null) {
            throw new IllegalArgumentException(
                    "El repositorio de productos no puede ser nulo."
            );
        }

        if (marcaRepositoryJdbc == null) {
            throw new IllegalArgumentException(
                    "El repositorio de marcas no puede ser nulo."
            );
        }

        if (catalogoRepositoryJdbc == null) {
            throw new IllegalArgumentException(
                    "El repositorio de catálogo no puede ser nulo."
            );
        }

        this.productoRepositoryJdbc = productoRepositoryJdbc;
        this.marcaRepositoryJdbc = marcaRepositoryJdbc;
        this.catalogoRepositoryJdbc = catalogoRepositoryJdbc;
    }

    // --- Productos ---

    public void registrarProducto(Producto producto)
            throws PersistenciaException {

        verificarAdministrador();

        if (producto != null &&
                (producto.getId() == null
                        || producto.getId().isBlank())) {

            producto.setId(UUID.randomUUID().toString());
        }

        validarProducto(producto);

        productoRepositoryJdbc.guardar(producto);
    }

    public void actualizarProducto(Producto producto)
            throws PersistenciaException {

        verificarAdministrador();
        validarProducto(producto);

        productoRepositoryJdbc.actualizar(producto);
    }

    public Producto buscarProductoPorId(String id)
            throws PersistenciaException {

        verificarSesion();
        validarIdTexto(id, "producto");

        return productoRepositoryJdbc.buscarPorId(id.trim());
    }

    public List<Producto> listarProductosActivos()
            throws PersistenciaException {

        verificarSesion();

        return productoRepositoryJdbc.listar();
    }

    public void desactivarProducto(Producto producto)
            throws PersistenciaException {

        verificarAdministrador();

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no puede ser nulo."
            );
        }

        validarIdTexto(producto.getId(), "producto");

        productoRepositoryJdbc.eliminar(
                producto.getId().trim()
        );

        producto.setActivo(false);
    }

    // --- Marcas ---

    public void registrarMarca(Marca marca)
            throws PersistenciaException {

        verificarAdministrador();

        if (marca != null &&
                (marca.getId() == null
                        || marca.getId().isBlank())) {

            marca.setId(UUID.randomUUID().toString());
        }

        validarMarca(marca);

        marcaRepositoryJdbc.guardar(marca);
    }

    public void actualizarMarca(Marca marca)
            throws PersistenciaException {

        verificarAdministrador();
        validarMarca(marca);

        marcaRepositoryJdbc.actualizar(marca);
    }

    public Marca buscarMarcaPorId(String id)
            throws PersistenciaException {

        verificarSesion();
        validarIdTexto(id, "marca");

        return marcaRepositoryJdbc.buscarPorId(id.trim());
    }

    public List<Marca> listarMarcasActivas()
            throws PersistenciaException {

        verificarSesion();

        return marcaRepositoryJdbc.listar();
    }

    public void desactivarMarca(String id)
            throws PersistenciaException {

        verificarAdministrador();
        validarIdTexto(id, "marca");

        marcaRepositoryJdbc.eliminar(id.trim());
    }

    // --- Presentaciones ---

    public List<Presentacion> listarPresentaciones()
            throws PersistenciaException {

        verificarSesion();

        return catalogoRepositoryJdbc.listar();
    }

    public void actualizarPrecio(
            Presentacion presentacion,
            BigDecimal precio)
            throws PersistenciaException {

        verificarAdministrador();

        if (presentacion == null) {
            throw new IllegalArgumentException(
                    "La presentación no puede ser nula."
            );
        }

        validarIdTexto(presentacion.getId(), "presentación");

        if (precio == null
                || precio.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El precio no puede ser nulo ni negativo."
            );
        }

        BigDecimal precioAnterior =
                presentacion.getPrecioLlenado();

        presentacion.setPrecioLlenado(precio);

        try {

            catalogoRepositoryJdbc.actualizar(presentacion);

        } catch (RuntimeException e) {

            presentacion.setPrecioLlenado(precioAnterior);

            throw e;
        }
    }

    public void desactivarPresentacion(Presentacion presentacion)
            throws PersistenciaException {

        verificarAdministrador();

        if (presentacion == null) {
            throw new IllegalArgumentException(
                    "La presentación no puede ser nula."
            );
        }

        validarIdTexto(
                presentacion.getId(),
                "presentación"
        );

        catalogoRepositoryJdbc.eliminar(
                presentacion.getId().trim()
        );

        presentacion.setActiva(false);
    }

    // --- Validaciones de permisos ---

    private void verificarSesion() {

        if (!SesionUsuario.haySesionActiva()) {
            throw new IllegalStateException(
                    "No hay una sesión activa."
            );
        }
    }

    private void verificarAdministrador() {

        verificarSesion();

        if (!SesionUsuario.esAdministrador()) {
            throw new IllegalArgumentException(
                    "No tenés permisos para modificar el catálogo."
            );
        }
    }

    // --- Validaciones de datos ---

    private void validarProducto(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no puede ser nulo."
            );
        }

        if (producto.getId() == null
                || producto.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "El ID del producto es obligatorio."
            );
        }

        if (producto.getNombre() == null
                || producto.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio."
            );
        }
    }

    private void validarMarca(Marca marca) {

        if (marca == null) {
            throw new IllegalArgumentException(
                    "La marca no puede ser nula."
            );
        }

        if (marca.getId() == null
                || marca.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "El ID de la marca es obligatorio."
            );
        }

        if (marca.getNombre() == null
                || marca.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre de la marca es obligatorio."
            );
        }
    }

    private void validarIdTexto(String id, String entidad) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "El ID de " + entidad + " es obligatorio."
            );
        }
    }
}