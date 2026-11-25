package ar.edu.utn.tpfinalsoderia.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.GeneralSecurityException;
import java.util.Base64;

public final class SeguridadContrasena {

    private static final int ITERACIONES = 600000;
    private static final int LONGITUD_CLAVE = 256;
    private static final int LONGITUD_SAL = 16;

    private SeguridadContrasena() {
    }

    public static String generarHash(String contrasena) {

        byte[] sal = new byte[LONGITUD_SAL];
        SecureRandom random = new SecureRandom();
        random.nextBytes(sal);

        byte[] hash = calcularHash(contrasena, sal);

        return Base64.getEncoder().encodeToString(sal)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificarContrasena(
            String contrasena,
            String hashGuardado) {

        if (contrasena == null || hashGuardado == null) {
            return false;
        }

        String[] partes = hashGuardado.split(":");

        if (partes.length != 2) {
            return false;
        }

        try {
            byte[] sal = Base64.getDecoder().decode(partes[0]);
            byte[] hashOriginal =
                    Base64.getDecoder().decode(partes[1]);

            byte[] hashCalculado =
                    calcularHash(contrasena, sal);

            return MessageDigest.isEqual(
                    hashOriginal,
                    hashCalculado
            );

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] calcularHash(
            String contrasena,
            byte[] sal) {

        PBEKeySpec especificacion = new PBEKeySpec(
                contrasena.toCharArray(),
                sal,
                ITERACIONES,
                LONGITUD_CLAVE
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return fabrica.generateSecret(
                    especificacion
            ).getEncoded();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "No se pudo procesar la contraseña.", e
            );
        } finally {
            especificacion.clearPassword();
        }
    }
}