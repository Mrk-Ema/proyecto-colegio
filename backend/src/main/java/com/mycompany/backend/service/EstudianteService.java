package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.EncargadoDAO;
import com.mycompany.backend.dao.EstudianteDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.UsuarioDAO;
import com.mycompany.backend.util.PasswordUtil;
import jakarta.ws.rs.NotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EstudianteService {

    private static final String PASSWORD_INICIAL = "123";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final EncargadoDAO encargadoDAO = new EncargadoDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    public Map<String, Object> crear(Map<String, Object> datos) throws Exception {
        String correo = texto(datos.get("correo"));
        String nombres = texto(datos.get("nombres"));
        String apellidos = texto(datos.get("apellidos"));
        LocalDate fechaNacimiento = fecha(datos.get("fechaNacimiento"));
        String direccion = textoONulo(datos.get("direccion"));
        String telefono = textoONulo(datos.get("telefono"));
        String tipoSangre = textoONulo(datos.get("tipoSangre"));
        String alergias = textoONulo(datos.get("alergias"));
        String padecimientos = textoONulo(datos.get("padecimientos"));
        List<Map<String, Object>> encargados = encargados(datos.get("encargados"));

        validarObligatorios(nombres, apellidos, fechaNacimiento, correo);
        validarCorreo(correo);
        validarTelefono(telefono);
        if (usuarioDAO.buscarPorCorreo(correo) != null) {
            throw new IllegalArgumentException("El correo ya esta registrado");
        }
        if (encargados.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un encargado");
        }
        for (Map<String, Object> e : encargados) {
            validarEncargado(e, null);
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            int idUsuario = usuarioDAO.crear(correo, PasswordUtil.encode(PASSWORD_INICIAL), "Estudiante");
            int idEstudiante = estudianteDAO.crear(nombres, apellidos, fechaNacimiento,
                    direccion, telefono, tipoSangre, alergias, padecimientos, idUsuario);
            int anio = LocalDate.now().getYear();
            String carnet = anio + String.format("%04d", idEstudiante);
            estudianteDAO.actualizarCarnet(idEstudiante, carnet);
            for (Map<String, Object> e : encargados) {
                encargadoDAO.crear(texto(e.get("nombres")), texto(e.get("apellidos")),
                        textoONulo(e.get("parentesco")), textoONulo(e.get("telefono")),
                        texto(e.get("dpi")), idEstudiante);
            }
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("idEstudiante", idEstudiante);
            r.put("carnet", carnet);
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir crear estudiante");
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

    public Map<String, Object> listar(String nombre, String carne, String estado,
            Integer grado, Integer seccion, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        if (estado != null && !estado.isBlank()
                && !estado.equals("Activo") && !estado.equals("Inactivo") && !estado.equals("Graduado")) {
            throw new IllegalArgumentException("Estado invalido");
        }
        int total = estudianteDAO.contar(nombre, carne, estado, grado, seccion);
        List<Map<String, Object>> datos = estudianteDAO.listar(nombre, carne, estado,
                grado, seccion, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public Map<String, Object> obtener(int id) throws Exception {
        Map<String, Object> e = estudianteDAO.buscarPorId(id);
        if (e == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        e.put("encargados", encargadoDAO.listarPorEstudiante(id));
        return e;
    }

    public Map<String, Object> expediente(int id) throws Exception {
        Map<String, Object> e = obtener(id);
        e.put("inscripciones", inscripcionDAO.historialPorEstudiante(id));
        return e;
    }

    public void modificar(int id, Map<String, Object> datos) throws Exception {
        Map<String, Object> actual = estudianteDAO.buscarPorId(id);
        if (actual == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        String nombres = texto(datos.get("nombres"));
        String apellidos = texto(datos.get("apellidos"));
        LocalDate fechaNacimiento = fecha(datos.get("fechaNacimiento"));
        String direccion = textoONulo(datos.get("direccion"));
        String telefono = textoONulo(datos.get("telefono"));
        String tipoSangre = textoONulo(datos.get("tipoSangre"));
        String alergias = textoONulo(datos.get("alergias"));
        String padecimientos = textoONulo(datos.get("padecimientos"));
        String correo = texto(datos.get("correo"));
        List<Map<String, Object>> encargados = encargados(datos.get("encargados"));

        validarObligatorios(nombres, apellidos, fechaNacimiento, correo);
        validarCorreo(correo);
        validarTelefono(telefono);
        if (!correo.equalsIgnoreCase(String.valueOf(actual.get("correo")))) {
            if (usuarioDAO.buscarPorCorreo(correo) != null) {
                throw new IllegalArgumentException("El correo ya esta registrado");
            }
        }
        if (encargados.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un encargado");
        }
        Set<Integer> vistos = new HashSet<>();
        for (Map<String, Object> e : encargados) {
            Integer idEnc = numeroONulo(e.get("idEncargado"));
            if (idEnc != null) {
                if (!vistos.add(idEnc)) {
                    throw new IllegalArgumentException("Encargado repetido en la lista");
                }
                Map<String, Object> ex = encargadoDAO.buscarPorId(idEnc);
                if (ex == null || ((Integer) ex.get("idEstudiante")) != id) {
                    throw new IllegalArgumentException("Encargado invalido en la lista");
                }
            }
            validarEncargado(e, idEnc);
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            estudianteDAO.modificar(id, nombres, apellidos, fechaNacimiento,
                    direccion, telefono, tipoSangre, alergias, padecimientos);
            if (!correo.equalsIgnoreCase(String.valueOf(actual.get("correo")))) {
                usuarioDAO.actualizarCorreo((Integer) actual.get("idUsuario"), correo);
            }
            List<Map<String, Object>> actuales = encargadoDAO.listarPorEstudiante(id);
            Set<Integer> actualesIds = new HashSet<>();
            for (Map<String, Object> a : actuales) {
                actualesIds.add((Integer) a.get("idEncargado"));
            }
            Set<Integer> entrantesIds = new HashSet<>();
            for (Map<String, Object> e : encargados) {
                Integer idEnc = numeroONulo(e.get("idEncargado"));
                if (idEnc == null) {
                    encargadoDAO.crear(texto(e.get("nombres")), texto(e.get("apellidos")),
                            textoONulo(e.get("parentesco")), textoONulo(e.get("telefono")),
                            texto(e.get("dpi")), id);
                } else {
                    entrantesIds.add(idEnc);
                    encargadoDAO.modificar(idEnc, texto(e.get("nombres")), texto(e.get("apellidos")),
                            textoONulo(e.get("parentesco")), textoONulo(e.get("telefono")),
                            texto(e.get("dpi")));
                }
            }
            for (Integer idEnc : actualesIds) {
                if (!entrantesIds.contains(idEnc)) {
                    encargadoDAO.eliminar(idEnc);
                }
            }
            cn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir modificar estudiante");
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

    public void cambiarEstado(int id, String estado) throws Exception {
        Map<String, Object> actual = estudianteDAO.buscarPorId(id);
        if (actual == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        if (estado == null || (!estado.equals("Activo") && !estado.equals("Inactivo")
                && !estado.equals("Graduado"))) {
            throw new IllegalArgumentException("Estado invalido");
        }
        if (estado.equals(actual.get("estado"))) {
            throw new IllegalArgumentException("El estudiante ya esta en ese estado");
        }
        if ("Graduado".equals(actual.get("estado"))) {
            throw new IllegalArgumentException("Un estudiante Graduado ya no cambia de estado");
        }
        estudianteDAO.actualizarEstado(id, estado);
    }

    private void validarObligatorios(String nombres, String apellidos,
            LocalDate fechaNacimiento, String correo) {
        if (nombres == null || nombres.isBlank()) {
            throw new IllegalArgumentException("Nombres obligatorios");
        }
        if (apellidos == null || apellidos.isBlank()) {
            throw new IllegalArgumentException("Apellidos obligatorios");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("Fecha de nacimiento obligatoria");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Fecha de nacimiento invalida");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("Correo obligatorio");
        }
    }

    private void validarCorreo(String correo) {
        if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Correo invalido");
        }
    }

    private void validarTelefono(String telefono) {
        if (telefono != null && !telefono.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("Telefono invalido (8 digitos)");
        }
    }

    private void validarEncargado(Map<String, Object> e, Integer idEnc) throws Exception {
        if (texto(e.get("nombres")).isBlank() || texto(e.get("apellidos")).isBlank()) {
            throw new IllegalArgumentException("Nombres y apellidos del encargado obligatorios");
        }
        String dpi = texto(e.get("dpi"));
        if (dpi.isBlank()) {
            throw new IllegalArgumentException("DPI del encargado obligatorio");
        }
        if (!dpi.matches("[0-9]{13}")) {
            throw new IllegalArgumentException("DPI del encargado invalido (13 digitos)");
        }
        validarTelefono(textoONulo(e.get("telefono")));
        boolean duplicado = idEnc == null
                ? encargadoDAO.existeDpi(dpi)
                : encargadoDAO.existeOtroConDpi(idEnc, dpi);
        if (duplicado) {
            throw new IllegalArgumentException("El DPI del encargado ya esta registrado");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> encargados(Object v) {
        if (v instanceof List l) {
            return (List<Map<String, Object>>) l;
        }
        return List.of();
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

    private Integer numeroONulo(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v).trim());
    }
}
