package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteRepositoryJdbc extends RepositorioJdbc<Cliente> {

    public ClienteRepositoryJdbc() {
        super(null);
        conectar();

    }

    public ClienteRepositoryJdbc(Connection conexion) {
        super(java.util.Objects.requireNonNull(conexion, "La conexión es obligatoria."));
    }

    private void conectar() throws PersistenciaException {
        try {
            this.conexion = ConexionFactory.abrirConexion();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a la base de datos", e);
        }
    }


    @Override
    protected Cliente mapear(ResultSet resultado) throws SQLException {
        Cliente cliente = new Cliente();

        cliente.setId(resultado.getString("id"));
        cliente.setNombre(resultado.getString("nombre"));
        cliente.setTelefono(resultado.getString("telefono"));
        cliente.setDireccion(resultado.getString("direccion"));
        cliente.setActivo(resultado.getBoolean("activo"));

        return cliente;
    }

    @Override
    public void guardar(Cliente cliente) {
        if (cliente != null && (cliente.getId() == null || cliente.getId().isBlank())) {
            cliente.setId(java.util.UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO clientes "
                + "(id, nombre, telefono, direccion, activo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, cliente.getId());
            consulta.setString(2, cliente.getNombre());
            consulta.setString(3, cliente.getTelefono());
            consulta.setString(4, cliente.getDireccion());
            consulta.setBoolean(5, cliente.isActivo());

            consulta.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo guardar el cliente.", e
            );
        }
    }

    @Override
    public Cliente buscarPorId(String id) {
        String sql = "SELECT id, nombre, telefono, direccion, activo "
                + "FROM clientes WHERE id = ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, id);

            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return mapear(resultado);
                }
            }

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo buscar el cliente.", e
            );
        }

        return null;
    }

    @Override
    public List<Cliente> listar() {
        String sql = "SELECT id, nombre, telefono, direccion, activo "
                + "FROM clientes WHERE activo = TRUE";

        List<Cliente> clientes = new ArrayList<>();

        try (PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                clientes.add(mapear(resultado));
            }

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudieron listar los clientes.", e
            );
        }

        return clientes;
    }

    @Override
    public void actualizar(Cliente cliente) {
        String sql = "UPDATE clientes "
                + "SET nombre = ?, telefono = ?, direccion = ?, activo = ? "
                + "WHERE id = ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, cliente.getNombre());
            consulta.setString(2, cliente.getTelefono());
            consulta.setString(3, cliente.getDireccion());
            consulta.setBoolean(4, cliente.isActivo());
            consulta.setString(5, cliente.getId());

            consulta.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo actualizar el cliente.", e
            );
        }
    }

    @Override
    public void eliminar(String id) {
        // Baja lógica: conserva al cliente y sus relaciones.
        String sql = "UPDATE clientes SET activo = FALSE WHERE id = ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, id);
            consulta.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo desactivar el cliente.", e
            );
        }
    }
    @Override
    public void desactivar(String id) {
        eliminar(id); // Para esta entidad, eliminar ya realiza la baja lógica.
    }
}
