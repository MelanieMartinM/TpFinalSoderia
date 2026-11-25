package ar.edu.utn.tpfinalsoderia.utils;

public class GenerarContrasenasPrueba {

    public static void main(String[] args) {

        System.out.println("USE tpfinal_prueba;");

        generarSQL("admin", "admin123");
        generarSQL("gerente", "gerente123");
        generarSQL("planta1", "planta123");
        generarSQL("planta2", "planta456");
        generarSQL("repartidor1", "reparto123");
        generarSQL("repartidor2", "reparto456");
        generarSQL("inactivo", "inactivo123");
        generarSQL("usuario_prueba", "demo123");

        System.out.println("COMMIT;");
    }

    private static void generarSQL(
            String usuario,
            String contrasena) {

        String hash =
                SeguridadContrasena.generarHash(contrasena);

        System.out.println(
                "UPDATE usuarios SET hash_contrasena = '"
                        + hash
                        + "' WHERE nombre_usuario = '"
                        + usuario
                        + "';"
        );
    }
}