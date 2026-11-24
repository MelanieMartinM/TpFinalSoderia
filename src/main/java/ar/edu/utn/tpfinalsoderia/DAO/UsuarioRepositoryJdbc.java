package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.RolUsuario;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepositoryJdbc extends RepositorioJdbc<Usuario> {

    public UsuarioRepositoryJdbc(Connection conexion) {
        super(conexion);
    }

    // Convierte una fila de la tabla en un objeto Usuario.
    @Override
    protected Usuario mapear(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getString("id"),
                resultado.getString("nombre_usuario"),
                resultado.getString("hash_contrasena"),
                RolUsuario.valueOf(resultado.getString("rol")),
                resultado.getBoolean("activo")
        );
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios "
                + "(id, nombre_usuario, hash_contrasena, rol, activo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, usuario.getId());
            consulta.setString(2, usuario.getNombreUsuario());
            consulta.setString(3, usuario.getHashContrasena());
            consulta.setString(4, usuario.getRol().name());
            consulta.setBoolean(5, usuario.isActivo());

            consulta.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar el usuario.", e);
        }
    }

    @Override
    public Usuario buscarPorId(String id) {
        String sql = "SELECT id, nombre_usuario, hash_contrasena, rol, activo "
                + "FROM usuarios WHERE id = ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, id);

            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return mapear(resultado);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar el usuario.", e);
        }

        return null;
    }

    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT id, nombre_usuario, hash_contrasena, rol, activo "
                + "FROM usuarios WHERE nombre_usuario = ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nombreUsuario);

            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return mapear(resultado);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo buscar el usuario por nombre.", e
            );
        }

        return null;
    }

    @Override
    public List<Usuario> listar() {
        String sql = "SELECT id, nombre_usuario, hash_contrasena, rol, activo "
                + "FROM usuarios";

        List<Usuario> usuarios = new ArrayList<>();

        try (PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                usuarios.add(mapear(resultado));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron listar los usuarios.", e);
        }

        return usuarios;
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre_usuario = ?, "
                + "hash_contrasena = ?, rol = ?, activo = ? WHERE id = ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, usuario.getNombreUsuario());
            consulta.setString(2, usuario.getHashContrasena());
            consulta.setString(3, usuario.getRol().name());
            consulta.setBoolean(4, usuario.isActivo());
            consulta.setString(5, usuario.getId());

            consulta.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el usuario.", e);
        }
    }

    @Override
    public void eliminar(String id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, id);
            consulta.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo eliminar el usuario.", e);
        }
    }
    @Override
    public void desactivar(String id) {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE id = ?";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException("No se pudo desactivar el usuario.", e);
        }
    }
}