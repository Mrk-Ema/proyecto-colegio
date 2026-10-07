package com.mycompany.backend.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Codificacion Base64 de contraseñas
 * Toda la app usa esta clase y nadie codifica por su cuenta
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String encode(String plano) {
        return Base64.getEncoder().encodeToString(plano.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean matches(String plano, String guardado) {
        if (plano == null || guardado == null) {
            return false;
        }
        return encode(plano).equals(guardado);
    }
}
