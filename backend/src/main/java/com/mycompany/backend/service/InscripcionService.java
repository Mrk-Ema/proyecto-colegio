package com.mycompany.backend.service;

import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.CarreraDAO;
import com.mycompany.backend.dao.EstudianteDAO;
import com.mycompany.backend.dao.EstructuraDAO;
import com.mycompany.backend.dao.GradoDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.NivelDAO;
import com.mycompany.backend.dao.SeccionDAO;
import com.mycompany.backend.dto.AnioLectivo;
import jakarta.ws.rs.NotFoundException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CU051 inscribir estudiante, CU052 listar y filtrar, CU053 modificar inscripcion.
 * Actor: Secretaria (tambien Admin y Super Admin).
 */
public class InscripcionService {

    private final InscripcionDAO dao = new InscripcionDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final EstructuraDAO estructuraDAO = new EstructuraDAO();
    private final SeccionDAO seccionDAO = new SeccionDAO();
    private final GradoDAO gradoDAO = new GradoDAO();
    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final NivelDAO nivelDAO = new NivelDAO();

    /**
     * Anos que admite una inscripcion: todo menos Cerrado.
     * El que se propone por defecto es el Activo y, si no hay, el primero disponible.
     */
    public Map<String, Object> aniosDisponibles() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        Integer porDefecto = null;
        for (AnioLectivo a : anioDAO.listarTodos()) {
            if ("Cerrado".equals(a.getEstado())) {
                continue;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("anio", a.getAnio());
            m.put("estado", a.getEstado());
            m.put("fechaInicio", a.getFechaInicio() == null ? null : a.getFechaInicio().toString());
            m.put("fechaCierre", a.getFechaCierre() == null ? null : a.getFechaCierre().toString());
            lista.add(m);
        }
        for (Map<String, Object> m : lista) {
            if ("Activo".equals(m.get("estado"))) {
                porDefecto = (Integer) m.get("anio");
                break;
            }
        }
        if (porDefecto == null && !lista.isEmpty()) {
            porDefecto = (Integer) lista.get(0).get("anio");
        }
        Map<String, Object> r = new HashMap<>();
        r.put("anios", lista);
        r.put("porDefecto", porDefecto);
        return r;
    }

    /**
     * CU051: grados de la estructura del ano. Solo los Activos y, si son de
     * Diversificado, solo si su carrera sigue Activa.
     */
    public List<Map<String, Object>> gradosDisponibles(int anio) throws SQLException {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }
        if ("Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("El año lectivo está Cerrado");
        }
        List<Map<String, Object>> estructura = estructuraDAO.listarPorAnio(anio);
        if (estructura.isEmpty()) {
            throw new IllegalArgumentException("El año lectivo no tiene grados en su estructura, "
                    + "el Super Admin debe completarla");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> g : estructura) {
            if (!"Activo".equals(g.get("estadoGrado"))) {
                continue;
            }
            Integer idCarrera = (Integer) g.get("idCarrera");
            if (idCarrera != null) {
                Map<String, Object> c = carreraDAO.buscarPorId(idCarrera);
                if (c == null || !"Activo".equals(c.get("estado"))) {
                    continue;
                }
            }
            Map<String, Object> m = new HashMap<>();
            m.put("idGrado", g.get("idGrado"));
            m.put("grado", g.get("grado"));
            m.put("idNivel", g.get("idNivel"));
            m.put("nivel", g.get("nivel"));
            m.put("idCarrera", idCarrera);
            m.put("carrera", g.get("carrera"));
            out.add(m);
        }
        return out;
    }

    /** CU051: secciones Activas de un grado. */
    public List<Map<String, Object>> seccionesDeGrado(int idGrado) throws SQLException {
        if (gradoDAO.buscarPorId(idGrado) == null) {
            throw new NotFoundException("Grado no encontrado");
        }
        return seccionDAO.listarPorGrado(idGrado, "Activo");
    }

    /** CU051: inscribe a un estudiante Activo en un grado y seccion de un ano no Cerrado. */
    public Map<String, Object> crear(Map<String, Object> datos) throws Exception {
        int idEstudiante = entero(datos.get("idEstudiante"), "Estudiante obligatorio");
        Integer anio = enteroONulo(datos.get("anio"));
        int idGrado = entero(datos.get("idGrado"), "Grado obligatorio");
        int idSeccion = entero(datos.get("idSeccion"), "Sección obligatoria");
        if (anio == null) {
            throw new IllegalArgumentException("Año lectivo obligatorio");
        }

        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }
        if ("Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("El año lectivo está Cerrado");
        }

        Map<String, Object> estudiante = estudianteDAO.buscarPorId(idEstudiante);
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no existe");
        }
        if (!"Activo".equals(estudiante.get("estado"))) {
            throw new IllegalArgumentException("Solo se inscriben estudiantes Activos");
        }

        boolean gradoEnAnio = false;
        for (Map<String, Object> g : gradosDisponibles(anio)) {
            if (((Integer) g.get("idGrado")) == idGrado) {
                gradoEnAnio = true;
                break;
            }
        }
        if (!gradoEnAnio) {
            throw new IllegalArgumentException("El grado no pertenece a la estructura del año");
        }

        Map<String, Object> seccion = seccionDAO.buscarPorId(idSeccion);
        if (seccion == null) {
            throw new IllegalArgumentException("La sección no existe");
        }
        if (!"Activo".equals(seccion.get("estado"))) {
            throw new IllegalArgumentException("La sección está Inactiva");
        }
        if (((Integer) seccion.get("idGrado")) != idGrado) {
            throw new IllegalArgumentException("La sección no pertenece al grado seleccionado");
        }

        if (dao.existePorEstudianteYAnio(idEstudiante, anio)) {
            throw new IllegalArgumentException("El estudiante ya tiene una inscripción en ese año");
        }

        try {
            int id = dao.crear(idEstudiante, anio, idSeccion);
            Map<String, Object> r = new HashMap<>();
            r.put("idInscripcion", id);
            r.put("mensaje", "Inscripción registrada");
            return r;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("El estudiante ya tiene una inscripción en ese año");
        }
    }

    /** CU052: listado paginado con filtros. */
    public Map<String, Object> listar(Integer anio, Integer nivel, Integer grado, Integer seccion,
            String estudiante, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        if (nivel != null && !nivelExiste(nivel)) {
            throw new IllegalArgumentException("Nivel inválido");
        }
        int total = dao.contar(anio, nivel, grado, seccion, estudiante);
        List<Map<String, Object>> datos = dao.listar(anio, nivel, grado, seccion, estudiante,
                size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    /** Niveles para filtrar el listado (la Secretaria no puede leer el catálogo general). */
    public List<Map<String, Object>> niveles() throws SQLException {
        return nivelDAO.listar();
    }

    /** CU053: detalle de una inscripción. */
    public Map<String, Object> obtener(int idInscripcion) throws SQLException {
        Map<String, Object> f = dao.buscarPorId(idInscripcion);
        if (f == null) {
            throw new NotFoundException("Inscripción no encontrada");
        }
        return f;
    }

    /** CU053: solo cambia la sección; estudiante, año y grado quedan fijos. */
    public void modificar(int idInscripcion, Map<String, Object> datos) throws Exception {
        Map<String, Object> actual = dao.buscarPorId(idInscripcion);
        if (actual == null) {
            throw new NotFoundException("Inscripción no encontrada");
        }
        AnioLectivo a = anioDAO.buscarPorAnio((Integer) actual.get("anio"));
        if (a == null || "Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("El año lectivo está Cerrado");
        }
        int idGrado = (Integer) actual.get("idGrado");
        int idSeccion = entero(datos.get("idSeccion"), "Sección obligatoria");

        Integer otroAnio = enteroONulo(datos.get("anio"));
        if (otroAnio != null && otroAnio != (Integer) actual.get("anio")) {
            throw new IllegalArgumentException("No se puede cambiar el año lectivo");
        }
        Integer otroGrado = enteroONulo(datos.get("idGrado"));
        if (otroGrado != null && otroGrado != idGrado) {
            throw new IllegalArgumentException("No se puede cambiar el grado");
        }
        Integer otroEstudiante = enteroONulo(datos.get("idEstudiante"));
        if (otroEstudiante != null && otroEstudiante != (Integer) actual.get("idEstudiante")) {
            throw new IllegalArgumentException("No se puede cambiar el estudiante");
        }

        Map<String, Object> seccion = seccionDAO.buscarPorId(idSeccion);
        if (seccion == null) {
            throw new IllegalArgumentException("La sección no existe");
        }
        if (!"Activo".equals(seccion.get("estado"))) {
            throw new IllegalArgumentException("La sección está Inactiva");
        }
        if (((Integer) seccion.get("idGrado")) != idGrado) {
            throw new IllegalArgumentException("La sección no pertenece al grado de la inscripción");
        }
        dao.cambiarSeccion(idInscripcion, idSeccion);
    }

    private boolean nivelExiste(int idNivel) throws SQLException {
        for (Map<String, Object> n : nivelDAO.listar()) {
            if (((Integer) n.get("idNivel")) == idNivel) {
                return true;
            }
        }
        return false;
    }

    private int entero(Object v, String mensaje) {
        Integer i = enteroONulo(v);
        if (i == null) {
            throw new IllegalArgumentException(mensaje);
        }
        return i;
    }

    private Integer enteroONulo(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido");
        }
    }
}