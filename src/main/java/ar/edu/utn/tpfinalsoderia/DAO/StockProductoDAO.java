package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.MaterialEnvase;
import ar.edu.utn.tpfinalsoderia.enums.TipoEnvase;
import ar.edu.utn.tpfinalsoderia.enums.TipoProducto;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StockProductoDAO extends RepositorioJdbc<StockProducto> {

    private static final String SELECT_BASE = """
            SELECT sp.id, sp.cliente_id, sp.presentacion_id, sp.disponibles,
                   c.nombre AS cliente_nombre, c.telefono AS cliente_telefono, c.direccion AS cliente_direccion, c.activo AS cliente_activo,
                   p.nombre AS pres_nombre, p.precio_llenado, p.activa AS pres_activa,
                   e.id AS envase_id, e.descripcion AS envase_descripcion, e.tipo_envase, e.material, e.capacidad_lts,
                   m.id AS marca_id, m.nombre AS marca_nombre, m.es_propia_fabrica, m.titular_id AS marca_titular_id
            FROM stock_producto sp
            LEFT JOIN clientes c ON sp.cliente_id = c.id
            INNER JOIN presentaciones p ON sp.presentacion_id = p.id
            INNER JOIN envases e ON p.envase_id = e.id
            LEFT JOIN marcas m ON p.marca_id = m.id
            """;

    public StockProductoDAO(Connection conexion) {
        super(conexion);
    }

    @Override
    public void guardar(StockProducto stock) {
        if (stock.getId() == null || stock.getId().isBlank()) {
            stock.setId(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO stock_producto (id, cliente_id, presentacion_id, disponibles) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, stock.getId());
            if (stock.getTitular() != null) {
                ps.setString(2, stock.getTitular().getId());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, stock.getPresentacion().getId());
            ps.setInt(4, stock.getDisponibles());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar stock de producto", e);
        }
    }

    @Override
    public StockProducto buscarPorId(String id) {
        String sql = SELECT_BASE + " WHERE sp.id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar stock de producto por id", e);
        }
        return null;
    }

    public StockProducto buscarPorTitularYPresentacion(String clienteId, String presentacionId) {
        String sql = (clienteId == null)
                ? SELECT_BASE + " WHERE sp.cliente_id IS NULL AND sp.presentacion_id = ?"
                : SELECT_BASE + " WHERE sp.cliente_id = ? AND sp.presentacion_id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (clienteId == null) {
                ps.setString(1, presentacionId);
            } else {
                ps.setString(1, clienteId);
                ps.setString(2, presentacionId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar stock de producto por titular y presentación", e);
        }
        return null;
    }

    public Presentacion buscarPresentacionCompleta(String presentacionId) {
        String sql = """
                SELECT p.id, p.nombre, p.precio_llenado, p.activa,
                       pr.id AS prod_id, pr.nombre AS prod_nombre, pr.tipo_producto, pr.activo AS prod_activo,
                       e.id AS envase_id, e.descripcion AS envase_descripcion, e.tipo_envase, e.material, e.capacidad_lts,
                       m.id AS marca_id, m.nombre AS marca_nombre, m.es_propia_fabrica, m.titular_id AS marca_titular_id
                FROM presentaciones p
                LEFT JOIN productos pr ON p.producto_id = pr.id
                INNER JOIN envases e ON p.envase_id = e.id
                LEFT JOIN marcas m ON p.marca_id = m.id
                WHERE p.id = ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, presentacionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Presentacion pres = new Presentacion();
                    pres.setId(rs.getString("id"));
                    pres.setNombre(rs.getString("nombre"));
                    pres.setPrecioLlenado(rs.getBigDecimal("precio_llenado"));
                    pres.setActiva(rs.getBoolean("activa"));

                    String prodId = rs.getString("prod_id");
                    if (prodId != null) {
                        Producto prod = new Producto();
                        prod.setId(prodId);
                        prod.setNombre(rs.getString("prod_nombre"));
                        String tipoProdStr = rs.getString("tipo_producto");
                        if (tipoProdStr != null) prod.setTipoProducto(TipoProducto.valueOf(tipoProdStr));
                        prod.setActivo(rs.getBoolean("prod_activo"));
                        pres.setProducto(prod);
                    }

                    Envase envase = new Envase();
                    envase.setId(rs.getString("envase_id"));
                    envase.setDescripcion(rs.getString("envase_descripcion"));
                    String tipoEnvStr = rs.getString("tipo_envase");
                    if (tipoEnvStr != null) envase.setTipoEnvase(TipoEnvase.valueOf(tipoEnvStr));
                    String matStr = rs.getString("material");
                    if (matStr != null) envase.setMaterial(MaterialEnvase.valueOf(matStr));
                    envase.setCapacidadLTS(rs.getDouble("capacidad_lts"));
                    pres.setEnvase(envase);

                    int marcaId = rs.getInt("marca_id");
                    if (!rs.wasNull()) {
                        Marca marca = new Marca();
                        marca.setId(marcaId);
                        marca.setNombre(rs.getString("marca_nombre"));
                        marca.setEsPropiaFabrica(rs.getBoolean("es_propia_fabrica"));
                        String marcaTitularId = rs.getString("marca_titular_id");
                        if (marcaTitularId != null) {
                            marca.setTitular(new Cliente("", marcaTitularId, "", "", true));
                        }
                        pres.setMarca(marca);
                    }

                    return pres;
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar presentación completa", e);
        }
        return null;
    }

    @Override
    public List<StockProducto> listar() {
        List<StockProducto> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar stock de productos", e);
        }
        return lista;
    }

    @Override
    public void actualizar(StockProducto stock) {
        String sql = "UPDATE stock_producto SET disponibles = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, stock.getDisponibles());
            ps.setString(2, stock.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar stock de producto", e);
        }
    }

    @Override
    public void eliminar(String id) {
        String sql = "DELETE FROM stock_producto WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar stock de producto", e);
        }
    }

    @Override
    protected StockProducto mapear(ResultSet rs) throws SQLException {
        StockProducto stock = new StockProducto();
        stock.setId(rs.getString("id"));
        stock.setDisponibles(rs.getInt("disponibles"));

        String clienteId = rs.getString("cliente_id");
        if (clienteId != null) {
            Cliente cliente = new Cliente(
                    rs.getString("cliente_direccion"),
                    clienteId,
                    rs.getString("cliente_nombre"),
                    rs.getString("cliente_telefono"),
                    rs.getBoolean("cliente_activo")
            );
            stock.setTitular(cliente);
        }

        Presentacion presentacion = new Presentacion();
        presentacion.setId(rs.getString("presentacion_id"));
        presentacion.setNombre(rs.getString("pres_nombre"));
        presentacion.setPrecioLlenado(rs.getBigDecimal("precio_llenado"));
        presentacion.setActiva(rs.getBoolean("pres_activa"));

        Envase envase = new Envase();
        envase.setId(rs.getString("envase_id"));
        envase.setDescripcion(rs.getString("envase_descripcion"));
        presentacion.setEnvase(envase);

        int marcaId = rs.getInt("marca_id");
        if (!rs.wasNull()) {
            Marca marca = new Marca();
            marca.setId(marcaId);
            marca.setNombre(rs.getString("marca_nombre"));
            marca.setEsPropiaFabrica(rs.getBoolean("es_propia_fabrica"));
            String marcaTitularId = rs.getString("marca_titular_id");
            if (marcaTitularId != null) {
                marca.setTitular(new Cliente("", marcaTitularId, "", "", true));
            }
            presentacion.setMarca(marca);
        }

        stock.setPresentacion(presentacion);
        return stock;
    }
}