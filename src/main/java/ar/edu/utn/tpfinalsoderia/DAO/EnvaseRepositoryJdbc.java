package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.MaterialEnvase;
import ar.edu.utn.tpfinalsoderia.enums.TipoEnvase;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Envase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnvaseRepositoryJdbc extends RepositorioJdbc<Envase> {

    private static final String SQL_INSERTAR =
            "INSERT INTO envases (id, descripcion, tipo_envase, material, capacidad_lts) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE envases SET descripcion = ?, tipo_envase = ?, material = ?, capacidad_lts = ? WHERE id = ?";
    private static final String SQL_ELIMINAR =
            "DELETE FROM envases WHERE id = ?";
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, descripcion, tipo_envase, material, capacidad_lts FROM envases WHERE id = ?";
    private static final String SQL_LISTAR =
            "SELECT id, descripcion, tipo_envase, material, capacidad_lts FROM envases";

    public EnvaseRepositoryJdbc() throws PersistenciaException {
        super(null);
        conectar();
    }

    private void conectar() throws PersistenciaException {
        try {
            this.conexion = ConexionFactory.abrirConexion();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a la base de datos", e);
        }
    }

    @Override
    protected Envase mapear(ResultSet resultado) throws SQLException {
        Envase envase = new Envase();
        envase.setId(resultado.getString("id"));
        envase.setDescripcion(resultado.getString("descripcion"));

        String tipoEnvaseStr = resultado.getString("tipo_envase");
        if (tipoEnvaseStr != null && !tipoEnvaseStr.isBlank()) {
            envase.setTipoEnvase(TipoEnvase.valueOf(tipoEnvaseStr));
        }

        String materialStr = resultado.getString("material");
        if (materialStr != null && !materialStr.isBlank()) {
            envase.setMaterial(MaterialEnvase.valueOf(materialStr));
        }

        envase.setCapacidadLTS(resultado.getDouble("capacidad_lts"));
        return envase;
    }

    @Override
    public void guardar(Envase envase) throws PersistenciaException {
        if (envase != null && (envase.getId() == null || envase.getId().isBlank()))
            envase.setId(java.util.UUID.randomUUID().toString());
        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR)) {
            ps.setString(1, envase.getId());
            ps.setString(2, envase.getDescripcion());
            ps.setString(3, envase.getTipoEnvase() != null ? envase.getTipoEnvase().name() : null);
            ps.setString(4, envase.getMaterial() != null ? envase.getMaterial().name() : null);
            ps.setDouble(5, envase.getCapacidadLTS());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el envase id: " + envase.getId(), e);
        }
    }

    @Override
    public Envase buscarPorId(String id) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el envase id: " + id, e);
        }
    }

    @Override
    public List<Envase> listar() throws PersistenciaException {
        List<Envase> envases = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                envases.add(mapear(rs));
            }
            return envases;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar los envases", e);
        }
    }

    @Override
    public void actualizar(Envase envase) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, envase.getDescripcion());
            ps.setString(2, envase.getTipoEnvase() != null ? envase.getTipoEnvase().name() : null);
            ps.setString(3, envase.getMaterial() != null ? envase.getMaterial().name() : null);
            ps.setDouble(4, envase.getCapacidadLTS());
            ps.setString(5, envase.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar el envase id: " + envase.getId(), e);
        }
    }

    @Override
    public void eliminar(String id) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el envase id: " + id, e);
        }
    }
}