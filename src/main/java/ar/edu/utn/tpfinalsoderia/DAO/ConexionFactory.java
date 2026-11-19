package ar.edu.utn.tpfinalsoderia.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Para que nadie tenga que subir su contraseña a GitHub,
// vamos a leer los datos de conexión desde variables locales de cada computadora.

public final class ConexionFactory {

    private ConexionFactory() {
        // Evita crear objetos de esta clase.
    }

    public static Connection abrirConexion() throws SQLException {

        String url = System.getenv("SODERIA_DB_URL");
        String usuario = System.getenv("SODERIA_DB_USUARIO");
        String clave = System.getenv("SODERIA_DB_CLAVE");

        if (url == null || url.isBlank()
                || usuario == null || usuario.isBlank()
                || clave == null) {
            throw new IllegalStateException(
                    "Falta configurar SODERIA_DB_URL, "
                            + "SODERIA_DB_USUARIO o SODERIA_DB_CLAVE."
            );
        }

        return DriverManager.getConnection(url, usuario, clave);
    }
}