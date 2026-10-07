package com.mycompany.backend.service;

import com.mycompany.backend.dao.UsuarioDAO;
import com.mycompany.backend.dto.Usuario;
import com.mycompany.backend.util.PasswordUtil;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Autenticacion (CU001): login y reglas de acceso
 */
public class AutenticacionService {

    private static final String ERROR_LOGIN = "Credenciales inválidas";
    private static final long SESION_MILLIS = 8L * 60 * 60 * 1000; 

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Map<String, String> login(String correo, String password) throws SQLException {
        if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
            throw new SecurityException(ERROR_LOGIN);
        }
        Usuario u = usuarioDAO.buscarPorCorreo(correo.trim());
        if (u == null || !"Activo".equals(u.getEstado())
                || !PasswordUtil.matches(password, u.getPassword())) {
            throw new SecurityException(ERROR_LOGIN);
        }
        Map<String, String> sesion = new HashMap<>();
        sesion.put("token", generarToken(u));
        sesion.put("rol", u.getRol());
        return sesion;
    }

    private String generarToken(Usuario u) {
        long expira = System.currentTimeMillis() + SESION_MILLIS;
        String datos = u.getIdUsuario() + ":" + u.getRol() + ":" + expira;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(datos.getBytes(StandardCharsets.UTF_8));
    }
}
