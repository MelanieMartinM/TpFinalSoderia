package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.Marca;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.Producto;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CatalogoRepositoryJdbc
        extends RepositorioJdbc<Presentacion> {

    private static final String SQL_INSERTAR =
            "INSERT INTO presentaciones "
                    + "(id, nombre, producto_id, envase_id, marca_id, "
                    + "precio_llenado, activa) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE presentaciones SET nombre = ?, "
                    + "producto_id = ?, envase_id = ?, marca_id = ?, "
                    + "precio_llenado = ?, activa = ? "
                    + "WHERE id = ?";

    private static final String SQL_BAJA_LOGICA =
            "UPDATE presentaciones SET activa = FALSE WHERE id = ?";

    private static final String SQL_SELECT_BASE =
            "SELECT p.id, p.nombre, p.producto_id, "
                    + "p.envase_id, p.marca_id, p.precio_llenado, "
                    + "p.activa, pr.nombre AS producto_nombre "
                    + "FROM presentaciones p "
                    + "LEFT JOIN productos pr ON p.producto_id = pr.id ";

    private static final String SQL_BUSCAR_POR_ID =
            SQL_SELECT_BASE + "WHERE p.id = ?";

    private static final String SQL_LISTAR_ACTIVAS =
            SQL_SELECT_BASE + "WHERE p.activa = TRUE";

    public CatalogoRepositoryJdbc()
            throws PersistenciaException {

        super(null);
        conectar();
    }

    private void conectar() throws PersistenciaException {

        try {
            this.conexion = ConexionFactory.abrirConexion();

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo conectar a la base de datos", e
            );
        }
    }

    @Override
    protected Presentacion mapear(ResultSet resultado)
            throws SQLException {

        Presentacion presentacion = new Presentacion();

        presentacion.setId(resultado.getString("id"));
        presentacion.setNombre(resultado.getString("nombre"));

        // Producto

        String productoId =
                resultado.getString("producto_id");

        if (productoId != null && !productoId.isBlank()) {

            Producto producto = new Producto();

            producto.setId(productoId);

            producto.setNombre(
                    resultado.getString("producto_nombre")
            );

            presentacion.setProducto(producto);
        }

        // Envase

        String envaseId =
                resultado.getString("envase_id");

        if (envaseId != null && !envaseId.isBlank()) {

            Envase envase = new Envase();

            envase.setId(envaseId);

            presentacion.setEnvase(envase);
        }

        // Marca

        String marcaId =
                resultado.getString("marca_id");

        if (marcaId != null && !marcaId.isBlank()) {

            Marca marca = new Marca();

            marca.setId(marcaId);

            presentacion.setMarca(marca);
        }

        BigDecimal precioLlenado =
                resultado.getBigDecimal("precio_llenado");

        presentacion.setPrecioLlenado(precioLlenado);

        presentacion.setActiva(
                resultado.getBoolean("activa")
        );

        return presentacion;
    }

    @Override
    public void guardar(Presentacion presentacion)
            throws PersistenciaException {

        if (presentacion != null &&
                (presentacion.getId() == null
                        || presentacion.getId().isBlank())) {

            presentacion.setId(UUID.randomUUID().toString());
        }

        try (PreparedStatement ps =
                     conexion.prepareStatement(SQL_INSERTAR)) {

            ps.setString(1, presentacion.getId());
            ps.setString(2, presentacion.getNombre());

            setIdOrNull(ps, 3,
                    presentacion.getProducto() != null
                            ? presentacion.getProducto().getId()
                            : null);

            setIdOrNull(ps, 4,
                    presentacion.getEnvase() != null
                            ? presentacion.getEnvase().getId()
                            : null);

            setIdOrNull(ps, 5,
                    presentacion.getMarca() != null
                            ? presentacion.getMarca().getId()
                            : null);

            ps.setBigDecimal(6,
                    presentacion.getPrecioLlenado());

            ps.setBoolean(7, presentacion.isActiva());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al guardar la presentación id: "
                            + presentacion.getId(), e
            );
        }
    }

    @Override
    public Presentacion buscarPorId(String id)
            throws PersistenciaException {

        try (PreparedStatement ps =
                     conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapear(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al buscar la presentación id: "
                            + id, e
            );
        }
    }

    @Override
    public List<Presentacion> listar()
            throws PersistenciaException {

        List<Presentacion> presentaciones =
                new ArrayList<>();

        try (PreparedStatement ps =
                     conexion.prepareStatement(SQL_LISTAR_ACTIVAS);

             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                presentaciones.add(mapear(rs));
            }

            return presentaciones;

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al listar las presentaciones activas", e
            );
        }
    }

    @Override
    public void actualizar(Presentacion presentacion)
            throws PersistenciaException {

        try (PreparedStatement ps =
                     conexion.prepareStatement(SQL_ACTUALIZAR)) {

            ps.setString(1, presentacion.getNombre());

            setIdOrNull(ps, 2,
                    presentacion.getProducto() != null
                            ? presentacion.getProducto().getId()
                            : null);

            setIdOrNull(ps, 3,
                    presentacion.getEnvase() != null
                            ? presentacion.getEnvase().getId()
                            : null);

            setIdOrNull(ps, 4,
                    presentacion.getMarca() != null
                            ? presentacion.getMarca().getId()
                            : null);

            ps.setBigDecimal(5,
                    presentacion.getPrecioLlenado());

            ps.setBoolean(6, presentacion.isActiva());

            ps.setString(7, presentacion.getId());

            ps.executeUpdate();

        } catch (SQLException e) {


            throw new PersistenciaException(
                    "Error al actualizar la presentación id: "
                            + presentacion.getId(), e
            );
        }
    }

    @Override
    public void eliminar(String id)
            throws PersistenciaException {

        try (PreparedStatement ps =
                     conexion.prepareStatement(SQL_BAJA_LOGICA)) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al dar de baja lógica la presentación id: "
                            + id, e
            );
        }
    }

    private void setIdOrNull(
            PreparedStatement ps,
            int index,
            String valor) throws SQLException {

        if (valor == null || valor.isBlank()) {

            ps.setNull(index, Types.VARCHAR);

        } else {

            ps.setString(index, valor);
        }
    }
}