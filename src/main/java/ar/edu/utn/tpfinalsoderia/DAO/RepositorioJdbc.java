package ar.edu.utn.tpfinalsoderia.DAO;

import ar.edu.utn.tpfinalsoderia.interfaces.IRepositorio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class RepositorioJdbc<T> implements IRepositorio<T> {

    protected Connection conexion;

    protected RepositorioJdbc(Connection conexion) {
        this.conexion = conexion;
    }

    protected abstract T mapear(ResultSet resultado) throws SQLException;
}