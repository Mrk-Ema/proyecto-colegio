package com.mycompany.backend.service;

import com.mycompany.backend.dao.ActividadEvaluableDAO;
import com.mycompany.backend.dao.AsignacionDAO;
import com.mycompany.backend.dao.EstudianteDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.MultaDAO;
import com.mycompany.backend.dao.NotaDAO;
import com.mycompany.backend.dao.PrestamoDAO;
import com.mycompany.backend.dto.Estudiante;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas del estudiante: notas, estado de cuenta, préstamos y multas.
 * Solo muestra los datos del estudiante logueado.
 */
public class EstudianteConsultaService {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();
    private final NotaDAO notaDAO = new NotaDAO();
    private final ActividadEvaluableDAO actividadDAO = new ActividadEvaluableDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final MultaDAO multaDAO = new MultaDAO();
    private final SolvenciaService solvenciaService = new SolvenciaService();

    /** Años en los que el estudiante está inscrito (no Cerrados). */
    public List<Integer> aniosInscrito(int idUsuario) throws SQLException {
        Estudiante est = estudianteDAO.buscarPorUsuario(idUsuario);
        if (est == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        List<Integer> anios = new ArrayList<>();
        for (Map<String, Object> i : inscripcionDAO.historialPorEstudiante(est.getIdEstudiante())) {
            anios.add((Integer) i.get("anio"));
        }
        return anios;
    }

    /** Notas del estudiante por año y curso (opcional). */
    public Map<String, Object> notas(int idUsuario, Integer anio, Integer curso) throws Exception {
        Estudiante est = estudianteDAO.buscarPorUsuario(idUsuario);
        if (est == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        if (anio == null) {
            Map<String, Object> r = new HashMap<>();
            r.put("cursos", new ArrayList<>());
            r.put("anios", aniosInscrito(idUsuario));
            return r;
        }

        List<Map<String, Object>> notasRaw = notaDAO.listarPorEstudianteYAnio(est.getIdEstudiante(), anio);

        // Organizar por curso (asignación)
        Map<Integer, List<Map<String, Object>>> porCurso = new HashMap<>();
        for (Map<String, Object> n : notasRaw) {
            int idAsignacion = (Integer) n.get("idAsignacion");
            porCurso.computeIfAbsent(idAsignacion, k -> new ArrayList<>()).add(n);
        }

        // Filtrar por curso si se especifica
        List<Map<String, Object>> cursos = new ArrayList<>();
        for (Map.Entry<Integer, List<Map<String, Object>>> entry : porCurso.entrySet()) {
            int idAsignacion = entry.getKey();
            Map<String, Object> asignacion = asignacionDAO.buscarPorId(idAsignacion);
            if (asignacion == null) {
                continue;
            }
            if (curso != null && !curso.equals(asignacion.get("idCurso"))) {
                continue;
            }
            List<Map<String, Object>> actividades = actividadDAO.listarPorAsignacion(idAsignacion);
            List<Map<String, Object>> notasCurso = entry.getValue();

            // Organizar notas por actividad (idNota + valor)
            Map<String, Object> notasMap = new HashMap<>();
            BigDecimal total = BigDecimal.ZERO;
            for (Map<String, Object> n : notasCurso) {
                int idActividad = (Integer) n.get("idActividadEvaluable");
                BigDecimal valor = (BigDecimal) n.get("valor");
                int idNota = (Integer) n.get("idNota");
                Map<String, Object> notaInfo = new HashMap<>();
                notaInfo.put("idNota", idNota);
                notaInfo.put("valor", valor);
                notasMap.put(String.valueOf(idActividad), notaInfo);
                total = total.add(valor);
            }

            Map<String, Object> cursoInfo = new HashMap<>();
            cursoInfo.put("idAsignacion", idAsignacion);
            cursoInfo.put("idCurso", asignacion.get("idCurso"));
            cursoInfo.put("curso", asignacion.get("curso"));
            cursoInfo.put("grado", asignacion.get("grado"));
            cursoInfo.put("seccion", asignacion.get("seccion"));
            cursoInfo.put("anio", anio);
            cursoInfo.put("actividades", actividades);
            cursoInfo.put("notas", notasMap);
            cursoInfo.put("total", total);
            if (!actividades.isEmpty()) {
                BigDecimal totalPuntos = BigDecimal.ZERO;
                for (Map<String, Object> a : actividades) {
                    totalPuntos = totalPuntos.add((BigDecimal) a.get("puntos"));
                }
                if (totalPuntos.compareTo(BigDecimal.ZERO) > 0) {
                    cursoInfo.put("promedio", total.multiply(BigDecimal.valueOf(100)).divide(totalPuntos, 2, java.math.RoundingMode.HALF_UP));
                } else {
                    cursoInfo.put("promedio", BigDecimal.ZERO);
                }
            } else {
                cursoInfo.put("promedio", BigDecimal.ZERO);
            }
            cursos.add(cursoInfo);
        }

        Map<String, Object> r = new HashMap<>();
        r.put("cursos", cursos);
        r.put("anios", aniosInscrito(idUsuario));
        return r;
    }

    /** Estado de cuenta del estudiante en un año. */
    public Map<String, Object> estadoCuenta(int idUsuario, Integer anio) throws Exception {
        Estudiante est = estudianteDAO.buscarPorUsuario(idUsuario);
        if (est == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        if (anio == null) {
            throw new IllegalArgumentException("El año lectivo es obligatorio");
        }
        // Reutilizar la lógica de solvencia
        SolvenciaService solvencia = new SolvenciaService();
        return solvencia.calcular(est.getCarnet(), anio);
    }

    /** Préstamos del estudiante logueado. */
    public Map<String, Object> prestamos(int idUsuario, String estado, String desde, String hasta,
            int page, int size) throws Exception {
        Estudiante est = estudianteDAO.buscarPorUsuario(idUsuario);
        if (est == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = prestamoDAO.contar(null, est.getIdEstudiante(), "estudiante", estado, desde, hasta);
        List<Map<String, Object>> datos = prestamoDAO.listar(null, est.getIdEstudiante(), "estudiante", estado, desde, hasta, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    /** Multas del estudiante logueado. */
    public Map<String, Object> multas(int idUsuario, String estado, int page, int size) throws Exception {
        Estudiante est = estudianteDAO.buscarPorUsuario(idUsuario);
        if (est == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = multaDAO.contar(est.getIdEstudiante(), "estudiante", estado, null, null);
        List<Map<String, Object>> datos = multaDAO.listar(est.getIdEstudiante(), "estudiante", estado, null, null, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }
}