package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.enums.TipoProducto;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoRepositoryJdbc extends RepositorioJdbc<Producto>{




    private static final String SQL_INSERTAR = "INSERT INTO productos (id,nombre,tipo_producto,activo ) VALUES(?,?,?,?)";
    private static final String SQL_ACTUALIZAR = "UPDATE productos SET nombre =?,tipo_producto=?,activo = ? where id =?";
    private static final String SQL_BAJA_LOGICA = "UPDATE  productos SET activo = FALSE where id=?";
    private static final String SQL_BUSCAR_POR_ID = "SELECT id,nombre,tipo_producto,activo FROM productos where id =?";
    private static final String SQL_LISTAR_ACTIVOS = "SELECT id,nombre,tipo_producto,activo FROM productos where activo  = TRUE";


    public ProductoRepositoryJdbc() throws PersistenciaException{
        super(null);
        conectar();

    }

    private void conectar()throws PersistenciaException{

        try{
            this.conexion=ConexionFactory.abrirConexion();


        } catch (SQLException e) {
            throw new PersistenciaException("Error no se pudo conectar a la base de datos",e);
        }

    }




    @Override
    protected Producto mapear(ResultSet resultado) throws SQLException {
        Producto producto= new Producto();
        producto.setId(resultado.getString("id"));
        producto.setNombre(resultado.getString("nombre"));

        String tipo= resultado.getString("tipo_producto");
        if(tipo != null){
            producto.setTipoProducto(TipoProducto.valueOf(tipo));
        }
        producto.setActivo(resultado.getBoolean("activo"));
        return producto;
    }


    public void guardar(Producto producto) throws PersistenciaException {
        if (producto != null && (producto.getId() == null || producto.getId().isBlank()))
            producto.setId(java.util.UUID.randomUUID().toString());
        try(PreparedStatement ps =conexion.prepareStatement(SQL_INSERTAR)){
            ps.setString(1,producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getTipoProducto().name());
            ps.setBoolean(4, producto.isActivo());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el producto id: "+producto.getId(),e);
        }
    }


    @Override
    public Producto buscarPorId(String id) throws PersistenciaException {
        try(PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)){
            ps.setString(1,id);
            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return mapear(rs);
                }
                return null;
            }
        }catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el producto id: "+ id ,e);


        }
    }


    @Override
    public List<Producto> listar() throws PersistenciaException {
        List<Producto>productos = new ArrayList<>();
        try(PreparedStatement ps=conexion.prepareStatement(SQL_LISTAR_ACTIVOS);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                productos.add(mapear(rs));
            } return productos;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar los productos activos",e);
        }

    }

    @Override
    public void actualizar(Producto producto) throws PersistenciaException{
        try(PreparedStatement ps= conexion.prepareStatement(SQL_ACTUALIZAR)){
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getTipoProducto().name());
            ps.setBoolean(3, producto.isActivo());
            ps.setString(4, producto.getId());
            ps.executeUpdate();
        }catch(SQLException e){
            throw  new PersistenciaException("Error al actualizar el producto id: "+producto.getId(),e);
        }

    }

    @Override
    public void eliminar(String id) throws PersistenciaException {
        try(PreparedStatement ps=conexion.prepareStatement(SQL_BAJA_LOGICA)){
            ps.setString(1,id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al dar de baja logica el producto id: "+id,e);
        }

    }
}
