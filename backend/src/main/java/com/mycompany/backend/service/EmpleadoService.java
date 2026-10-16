package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.EmpleadoDAO;
import com.mycompany.backend.dao.UsuarioDAO;
import com.mycompany.backend.util.PasswordUtil;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmpleadoService {

    private static final String PASSWORD_INICIAL = "123";
    private static final Set<String> OPERATIVOS = Set.of(
            "Maestro", "Secretaria", "Bibliotecario", "Mantenimiento", "Limpieza");
    private static final Set<String> SIN_CUENTA = Set.of("Mantenimiento", "Limpieza");
    private static final Set<String> ADMINIST = Set.of("Admin", "Super Admin");

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    public Map<String, Object> crear(Map<String, Object> datos, String rolAuth) throws Exception {
        String nombres = texto(datos.get("nombres"));
        String apellidos = texto(datos.get("apellidos"));
        String dpi = texto(datos.get("dpi"));
        LocalDate fechaNacimiento = fecha(datos.get("fechaNacimiento"));
        String telefono = textoONulo(datos.get("telefono"));
        String direccion = textoONulo(datos.get("direccion"));
        String puesto = texto(datos.get("puesto"));
        LocalDate fechaContratacion = fecha(datos.get("fechaContratacion"));
        BigDecimal salario = decimal(datos.get("salario"));
        String correo = texto(datos.get("correo"));

        validarCampos(nombres, apellidos, dpi, fechaNacimiento, telefono,
                puesto, fechaContratacion, salario, null);
        validarPuestoParaRol(puesto, rolAuth);
        boolean conCuenta = !SIN_CUENTA.contains(puesto);
        if (conCuenta) {
            validarCorreo(correo);
            if (usuarioDAO.buscarPorCorreo(correo) != null) {
                throw new IllegalArgumentException("El correo ya esta registrado");
            }
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            Integer idUsuario = null;
            if (conCuenta) {
                idUsuario = usuarioDAO.crear(correo, PasswordUtil.encode(PASSWORD_INICIAL), puesto);
            }
            int idEmpleado = empleadoDAO.crear(nombres, apellidos, dpi, fechaNacimiento,
                    telefono, direccion, puesto, fechaContratacion, salario, idUsuario);
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("idEmpleado", idEmpleado);
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir crear empleado");
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                cn.setAutoCommit(anterior);
            } catch (SQLException ex) {
                System.out.println("Error al restaurar autoCommit");
                ex.printStackTrace();
            }
        }
    }

    public Map<String, Object> listar(String nombre, String puesto, String estado,
            String rolAuth, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        if (estado != null && !estado.isBlank()
                && !estado.equals("Activo") && !estado.equals("Inactivo")) {
            throw new IllegalArgumentException("Estado invalido");
        }
        boolean excluirAdmin = "Admin".equals(rolAuth);
        if (excluirAdmin && puesto != null && !puesto.isBlank() && ADMINIST.contains(puesto)) {
            throw new IllegalArgumentException("No autorizado para ese puesto");
        }
        int total = empleadoDAO.contar(nombre, puesto, estado, excluirAdmin);
        List<Map<String, Object>> datos = empleadoDAO.listar(nombre, puesto, estado,
                excluirAdmin, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public Map<String, Object> obtener(int id, String rolAuth) throws Exception {
        Map<String, Object> e = empleadoDAO.buscarDetalle(id);
        if (e == null) {
            throw new NotFoundException("Empleado no encontrado");
        }
        verificarAccesoPuesto(String.valueOf(e.get("puesto")), rolAuth);
        return e;
    }

    public void modificar(int id, Map<String, Object> datos, String rolAuth) throws Exception {
        Map<String, Object> actual = empleadoDAO.buscarDetalle(id);
        if (actual == null) {
            throw new NotFoundException("Empleado no encontrado");
        }
        verificarAccesoPuesto(String.valueOf(actual.get("puesto")), rolAuth);
        String nombres = texto(datos.get("nombres"));
        String apellidos = texto(datos.get("apellidos"));
        String dpi = texto(datos.get("dpi"));
        LocalDate fechaNacimiento = fecha(datos.get("fechaNacimiento"));
        String telefono = textoONulo(datos.get("telefono"));
        String direccion = textoONulo(datos.get("direccion"));
        String puesto = texto(datos.get("puesto"));
        LocalDate fechaContratacion = fecha(datos.get("fechaContratacion"));
        BigDecimal salario = decimal(datos.get("salario"));
        String correo = texto(datos.get("correo"));

        validarCampos(nombres, apellidos, dpi, fechaNacimiento, telefono,
                puesto, fechaContratacion, salario, id);
        validarPuestoParaRol(puesto, rolAuth);
        boolean conCuentaNuevo = !SIN_CUENTA.contains(puesto);
        Integer idUsuarioActual = (Integer) actual.get("idUsuario");
        String correoActual = actual.get("correo") == null ? "" : String.valueOf(actual.get("correo"));
        if (conCuentaNuevo) {
            validarCorreo(correo);
            if (!correo.equalsIgnoreCase(correoActual) && usuarioDAO.buscarPorCorreo(correo) != null) {
                throw new IllegalArgumentException("El correo ya esta registrado");
            }
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            empleadoDAO.modificar(id, nombres, apellidos, dpi, fechaNacimiento,
                    telefono, direccion, puesto, fechaContratacion, salario);
            boolean conCuentaAntes = idUsuarioActual != null;
            if (conCuentaNuevo && conCuentaAntes) {
                if (!correo.equalsIgnoreCase(correoActual)) {
                    usuarioDAO.actualizarCorreo(idUsuarioActual, correo);
                }
                if (!puesto.equals(actual.get("puesto"))) {
                    usuarioDAO.actualizarRol(idUsuarioActual, puesto);
                }
            } else if (conCuentaNuevo && !conCuentaAntes) {
                int idUsuario = usuarioDAO.crear(correo, PasswordUtil.encode(PASSWORD_INICIAL), puesto);
                empleadoDAO.actualizarUsuario(id, idUsuario);
            } else if (!conCuentaNuevo && conCuentaAntes) {
                usuarioDAO.actualizarEstado(idUsuarioActual, "Inactivo");
                empleadoDAO.actualizarUsuario(id, null);
            }
            cn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir modificar empleado");
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                cn.setAutoCommit(anterior);
            } catch (SQLException ex) {
                System.out.println("Error al restaurar autoCommit");
                ex.printStackTrace();
            }
        }
    }

    public void desactivar(int id, String rolAuth) throws Exception {
        Map<String, Object> actual = empleadoDAO.buscarDetalle(id);
        if (actual == null) {
            throw new NotFoundException("Empleado no encontrado");
        }
        verificarAccesoPuesto(String.valueOf(actual.get("puesto")), rolAuth);
        if (!"Activo".equals(actual.get("estado"))) {
            throw new IllegalArgumentException("Solo se desactiva en estado Activo");
        }
        if ("Super Admin".equals(actual.get("puesto"))
                && empleadoDAO.contarSuperAdminActivosExcepto(id) < 1) {
            throw new IllegalArgumentException("Debe quedar al menos un Super Admin Activo");
        }
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            empleadoDAO.actualizarEstado(id, "Inactivo");
            Integer idUsuario = (Integer) actual.get("idUsuario");
            if (idUsuario != null) {
                usuarioDAO.actualizarEstado(idUsuario, "Inactivo");
            }
            cn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir desactivar empleado");
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                cn.setAutoCommit(anterior);
            } catch (SQLException ex) {
                System.out.println("Error al restaurar autoCommit");
                ex.printStackTrace();
            }
        }
    }

    public void activar(int id, String rolAuth) throws Exception {
        Map<String, Object> actual = empleadoDAO.buscarDetalle(id);
        if (actual == null) {
            throw new NotFoundException("Empleado no encontrado");
        }
        verificarAccesoPuesto(String.valueOf(actual.get("puesto")), rolAuth);
        if (!"Inactivo".equals(actual.get("estado"))) {
            throw new IllegalArgumentException("Solo se activa en estado Inactivo");
        }
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            empleadoDAO.actualizarEstado(id, "Activo");
            Integer idUsuario = (Integer) actual.get("idUsuario");
            if (idUsuario != null) {
                usuarioDAO.actualizarEstado(idUsuario, "Activo");
            }
            cn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir activar empleado");
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                cn.setAutoCommit(anterior);
            } catch (SQLException ex) {
                System.out.println("Error al restaurar autoCommit");
                ex.printStackTrace();
            }
        }
    }

    private void verificarAccesoPuesto(String puesto, String rolAuth) {
        if ("Admin".equals(rolAuth) && ADMINIST.contains(puesto)) {
            throw new SecurityException("No autorizado para ese puesto");
        }
    }

    private void validarPuestoParaRol(String puesto, String rolAuth) {
        if (!OPERATIVOS.contains(puesto) && !ADMINIST.contains(puesto)) {
            throw new IllegalArgumentException("Puesto invalido");
        }
        if ("Admin".equals(rolAuth) && ADMINIST.contains(puesto)) {
            throw new SecurityException("No autorizado para ese puesto");
        }
    }

    private void validarCampos(String nombres, String apellidos, String dpi,
            LocalDate fechaNacimiento, String telefono, String puesto,
            LocalDate fechaContratacion, BigDecimal salario, Integer idEmpleado) throws Exception {
        if (nombres == null || nombres.isBlank()) {
            throw new IllegalArgumentException("Nombres obligatorios");
        }
        if (apellidos == null || apellidos.isBlank()) {
            throw new IllegalArgumentException("Apellidos obligatorios");
        }
        if (dpi == null || dpi.isBlank()) {
            throw new IllegalArgumentException("DPI obligatorio");
        }
        if (!dpi.matches("[0-9]{13}")) {
            throw new IllegalArgumentException("DPI invalido (13 digitos)");
        }
        boolean duplicado = idEmpleado == null
                ? empleadoDAO.existeDpi(dpi)
                : empleadoDAO.existeOtroConDpi(idEmpleado, dpi);
        if (duplicado) {
            throw new IllegalArgumentException("El DPI ya esta registrado");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("Fecha de nacimiento obligatoria");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Fecha de nacimiento invalida");
        }
        if (telefono != null && !telefono.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("Telefono invalido (8 digitos)");
        }
        if (puesto == null || puesto.isBlank()) {
            throw new IllegalArgumentException("Puesto obligatorio");
        }
        if (fechaContratacion == null) {
            throw new IllegalArgumentException("Fecha de contratacion obligatoria");
        }
        if (salario == null || salario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Salario invalido");
        }
    }

    private void validarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("Correo obligatorio para este puesto");
        }
        if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Correo invalido");
        }
    }

    private String texto(Object v) {
        return v == null || "null".equals(String.valueOf(v)) ? "" : String.valueOf(v).trim();
    }

    private String textoONulo(Object v) {
        if (v == null || "null".equals(String.valueOf(v))) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private LocalDate fecha(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        return LocalDate.parse(String.valueOf(v).trim());
    }

    private BigDecimal decimal(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        if (v instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        return new BigDecimal(String.valueOf(v).trim());
    }
}
