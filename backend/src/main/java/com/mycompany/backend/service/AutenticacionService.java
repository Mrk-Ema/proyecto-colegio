package com.mycompany.backend.service;

import com.mycompany.backend.dao.EmpleadoDAO;
import com.mycompany.backend.dao.EstudianteDAO;
import com.mycompany.backend.dao.UsuarioDAO;
import com.mycompany.backend.dto.Empleado;
import com.mycompany.backend.dto.Estudiante;
import com.mycompany.backend.dto.Usuario;
import com.mycompany.backend.util.PasswordUtil;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * login, password y perfil
 */
public class AutenticacionService {

    private static final String ERROR_LOGIN = "Credenciales invalidas";
    private static final String ERROR_RECUPERAR = "No se pudo completar la recuperación";
    private static final long SESION_MILLIS = 8L * 60 * 60 * 1000;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

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

    /**
     * cambia la contraseña verificando la actual
     */
    public void cambiarPassword(Integer idUsuario, String actual, String nueva) throws SQLException {
        if (actual == null || actual.isBlank() || nueva == null || nueva.isBlank()) {
            throw new SecurityException("La contraseña actual no es correcta");
        }
        Usuario u = usuarioDAO.buscarPorId(idUsuario);
        if (u == null || !PasswordUtil.matches(actual, u.getPassword())) {
            throw new SecurityException("La contraseña actual no es correcta");
        }
        usuarioDAO.actualizarPassword(idUsuario, PasswordUtil.encode(nueva.trim()));
    }

    /**
     * restablece la clave verificando correo y fecha de nacimiento Rechaza
     * usuarios inactivos
     */
    public void recuperarPassword(String correo, String fechaNacimiento, String nueva) throws SQLException {
        if (correo == null || correo.isBlank() || fechaNacimiento == null || fechaNacimiento.isBlank()
                || nueva == null || nueva.isBlank()) {
            throw new SecurityException(ERROR_RECUPERAR);
        }
        Usuario u = usuarioDAO.buscarPorCorreo(correo.trim());
        if (u == null || !"Activo".equals(u.getEstado())) {
            throw new SecurityException(ERROR_RECUPERAR);
        }
        LocalDate nacimiento;
        try {
            nacimiento = LocalDate.parse(fechaNacimiento.trim());
        } catch (Exception e) {
            throw new SecurityException(ERROR_RECUPERAR);
        }
        Empleado emp = empleadoDAO.buscarPorUsuario(u.getIdUsuario());
        Estudiante est = emp == null ? estudianteDAO.buscarPorUsuario(u.getIdUsuario()) : null;
        LocalDate real = emp != null ? emp.getFechaNacimiento()
                : (est != null ? est.getFechaNacimiento() : null);
        if (real == null || !real.equals(nacimiento)) {
            throw new SecurityException(ERROR_RECUPERAR);
        }
        usuarioDAO.actualizarPassword(u.getIdUsuario(), PasswordUtil.encode(nueva.trim()));
    }

    /**
     * perfil del dueño del token, nunca muestra el hash.
     */
    public Map<String, Object> obtenerPerfil(Integer idUsuario) throws SQLException {
        Usuario u = usuarioDAO.buscarPorId(idUsuario);
        if (u == null) {
            throw new SecurityException("Perfil no encontrado");
        }
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("idUsuario", u.getIdUsuario());
        perfil.put("correo", u.getCorreo());
        perfil.put("rol", u.getRol());
        perfil.put("estado", u.getEstado());
        Empleado emp = empleadoDAO.buscarPorUsuario(idUsuario);
        if (emp != null) {
            perfil.put("tipo", "empleado");
            perfil.put("nombres", emp.getNombres());
            perfil.put("apellidos", emp.getApellidos());
            perfil.put("telefono", emp.getTelefono());
            perfil.put("direccion", emp.getDireccion());
            perfil.put("puesto", emp.getPuesto());
            return perfil;
        }
        Estudiante est = estudianteDAO.buscarPorUsuario(idUsuario);
        if (est != null) {
            perfil.put("tipo", "estudiante");
            perfil.put("nombres", est.getNombres());
            perfil.put("apellidos", est.getApellidos());
            perfil.put("telefono", est.getTelefono());
            perfil.put("direccion", est.getDireccion());
            perfil.put("carnet", est.getCarnet());
            return perfil;
        }
        perfil.put("tipo", "cuenta");
        return perfil;
    }

    /**
     * modifica datos basicos del perfil
     */
    public void actualizarPerfil(Integer idUsuario, Map<String, String> datos) throws SQLException {
        if (datos == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }
        String correo = val(datos.get("correo"));
        String nombres = val(datos.get("nombres"));
        String apellidos = val(datos.get("apellidos"));
        String telefono = val(datos.get("telefono"));
        String direccion = val(datos.get("direccion"));
        if (correo != null && !correo.isBlank()) {
            try {
                usuarioDAO.actualizarCorreo(idUsuario, correo.trim());
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                throw new IllegalArgumentException("El correo ya está en uso");
            }
        }
        Empleado emp = empleadoDAO.buscarPorUsuario(idUsuario);
        if (emp != null) {
            if (nombres != null) {
                emp.setNombres(nombres);
            }
            if (apellidos != null) {
                emp.setApellidos(apellidos);
            }
            if (telefono != null && !telefono.isBlank() && !telefono.matches("[0-9]{8}")) {
                throw new IllegalArgumentException("Telefono invalido (8 dígitos)");
            }
            if (telefono != null && !telefono.isBlank()) {
                emp.setTelefono(telefono);   
            }
            if (direccion != null) {
                emp.setDireccion(direccion);
            }
            empleadoDAO.actualizarDatos(emp);
            return;
        }
        Estudiante est = estudianteDAO.buscarPorUsuario(idUsuario);
        if (est != null) {
            if (nombres != null) {
                est.setNombres(nombres);
            }
            if (apellidos != null) {
                est.setApellidos(apellidos);
            }
            if (telefono != null && !telefono.isBlank() && !telefono.matches("[0-9]{8}")) {
                throw new IllegalArgumentException("Telefono invalido (8 dígitos)");
            }
            if (telefono != null && !telefono.isBlank()) {
                emp.setTelefono(telefono);   
            }
            if (direccion != null) {
                est.setDireccion(direccion);
            }
            estudianteDAO.actualizarDatos(est);
        }
    }

    private String val(String s) {
        return s == null ? null : s.trim();
    }
}
