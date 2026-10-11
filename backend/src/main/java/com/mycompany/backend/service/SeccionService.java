package com.mycompany.backend.service;

import com.mycompany.backend.dao.GradoDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.SeccionDAO;
import jakarta.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeccionService {

    private final SeccionDAO dao = new SeccionDAO();
    private final GradoDAO gradoDAO = new GradoDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    public void crear(String nombre, Integer idGrado) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        if (idGrado == null) {
            throw new IllegalArgumentException("Grado obligatorio");
        }
        nombre = nombre.trim();
        Map<String, Object> g = gradoDAO.buscarPorId(idGrado);
        if (g == null) {
            throw new IllegalArgumentException("Grado inválido");
        }
        if (!"Activo".equals(g.get("estado"))) {
            throw new IllegalArgumentException("El grado está inactivo, actívelo primero (CU023)");
        }
        if (dao.existePorNombreEnGrado(nombre, idGrado)) {
            throw new IllegalArgumentException("El grado ya tiene una sección con ese nombre");
        }
        dao.crear(nombre, idGrado);
    }

    public Map<String, Object> listar(Integer nivel, Integer carrera, Integer grado, String nombre, String estado, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = dao.contar(nivel, carrera, grado, nombre, estado);
        List<Map<String, Object>> datos = dao.listar(nivel, carrera, grado, nombre, estado, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public Map<String, Object> obtener(int id) throws Exception {
        Map<String, Object> s = dao.buscarPorId(id);
        if (s == null) {
            throw new NotFoundException("Sección no encontrada");
        }
        return s;
    }

    public void modificar(int id, String nombre) throws Exception {
        Map<String, Object> s = dao.buscarPorId(id);
        if (s == null) {
            throw new NotFoundException("Sección no encontrada");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        nombre = nombre.trim();
        if (dao.existeOtroConNombre(id, nombre, (Integer) s.get("idGrado"))) {
            throw new IllegalArgumentException("El grado ya tiene una sección con ese nombre");
        }
        dao.modificar(id, nombre);
    }

    public void desactivar(int id) throws Exception {
        Map<String, Object> s = dao.buscarPorId(id);
        if (s == null) {
            throw new NotFoundException("Sección no encontrada");
        }
        if (!"Activo".equals(s.get("estado"))) {
            throw new IllegalArgumentException("Solo se desactiva en estado Activo");
        }
        if (inscripcionDAO.existePorSeccionEnActivo(id)) {
            throw new IllegalArgumentException("Tiene estudiantes inscritos en el año Activo");
        }
        dao.actualizarEstado(id, "Inactivo");
    }

    public void activar(int id) throws Exception {
        Map<String, Object> s = dao.buscarPorId(id);
        if (s == null) {
            throw new NotFoundException("Sección no encontrada");
        }
        if (!"Inactivo".equals(s.get("estado"))) {
            throw new IllegalArgumentException("Solo se activa en estado Inactivo");
        }
        Map<String, Object> g = gradoDAO.buscarPorId((Integer) s.get("idGrado"));
        if (g == null || !"Activo".equals(g.get("estado"))) {
            throw new IllegalArgumentException("El grado está inactivo, actívelo primero (CU023)");
        }
        dao.actualizarEstado(id, "Activo");
    }
}
