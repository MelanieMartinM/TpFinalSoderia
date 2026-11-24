package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Marca;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarcaRepositoryJdbc extends RepositorioJdbc<Marca> {





    private static final String SQL_INSERTAR = "INSERT INTO marcas (id,nombre,es_propia_fabrica,titular_id ) VALUES(?,?,?,?)";
    private static final String SQL_ACTUALIZAR = "UPDATE marcas SET nombre =?,es_propia_fabrica=?,titular_id = ? where id =?";
    private static final String SQL_BAJA_LOGICA = "UPDATE  marcas SET activo = FALSE where id=?";
    private static final String SQL_BUSCAR_POR_ID = "SELECT id,nombre,es_propia_fabrica,titular_id FROM marcas where id =?";
    private static final String SQL_LISTAR_ACTIVOS = "SELECT id,nombre,es_propia_fabrica,titular_id  FROM marcas where activo  = TRUE";


    public MarcaRepositoryJdbc() throws PersistenciaException{
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
    protected Marca mapear(ResultSet resultado) throws SQLException {
        Marca marca=new Marca();
        marca.setId(resultado.getString("id"));
        marca.setNombre(resultado.getString("nombre"));
        marca.setEsPropiaFabrica(resultado.getBoolean("es_propia_fabrica"));

    String  idtitular= resultado.getString("titular_id");
    if(idtitular!=null){
        Cliente titular=new Cliente();
        titular.setId(idtitular);
        marca.setTitular(titular);
    }
return marca;
    }

    @Override
    public void guardar(Marca marca) throws PersistenciaException {
        try(PreparedStatement ps= conexion.prepareStatement(SQL_INSERTAR)){
            ps.setString(1,marca.getId());
            ps.setString(2,marca.getNombre());
            ps.setBoolean(3, marca.isEsPropiaFabrica());
            ps.setString(4,marca.getTitular().getId());
            ps.executeUpdate();
        }catch (SQLException e){
            throw new PersistenciaException("Error al guardar la marca id: "+ marca.getId(),e);
        }


    }

    @Override
    public Marca buscarPorId(String id) throws PersistenciaException{
        try(PreparedStatement ps =conexion.prepareStatement(SQL_BUSCAR_POR_ID)){
ps.setString(1,id);
try(ResultSet rs=ps.executeQuery()){
    if(rs.next()){
        return mapear(rs);
    }
    return null;}
}catch (SQLException e){
    throw new PersistenciaException("Error al buscar marca id:"+id,e);

}



        }
    @Override
    public List<Marca> listar() throws PersistenciaException {
        List<Marca> marcas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                marcas.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar las marcas", e);
        }
        return marcas;
    }

    @Override
    public void actualizar(Marca marca) throws PersistenciaException{
        try(PreparedStatement ps=conexion.prepareStatement(SQL_ACTUALIZAR)){
            ps.setString(1,marca.getTitular().getId());
            ps.setBoolean(2, marca.isEsPropiaFabrica());
            ps.setString(3, marca.getNombre());
            ps.setString(4,marca.getId());
            ps.executeUpdate();
        }catch (SQLException e){
            throw new PersistenciaException("Error el actualizar la marca id :"+marca.getId(),e);

        }


    }

    @Override
    public void eliminar(String id) throws PersistenciaException {
        try (PreparedStatement ps= conexion.prepareStatement(SQL_BAJA_LOGICA)){

                ps.setString(1, id);
                ps.executeUpdate();

            } catch (SQLException e){
            throw new PersistenciaException("Error al dar de baja logica la marca id: " + id,e);
        }
        }

    }

