package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.ActividadEvaluableDAO;
import com.mycompany.backend.dao.AsignacionDAO;
import com.mycompany.backend.dao.EmpleadoDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.NotaDAO;
import com.mycompany.backend.dto.Empleado;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notas de los estudiantes por actividad evaluable.
 */
public class NotaService {

    private final NotaDAO dao = new NotaDAO();
    private final ActividadEvaluableDAO actividadDAO = new ActividadEvaluableDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    /**
     * Matriz de notas de un curso: actividades, estudiantes y sus notas
     * con el total acumulado por estudiante.
     */
    public Map<String, Object> listar(int idAsignacion, int idUsuario, String rol) throws Exception {
        validarAsignacionDelMaestro(idAsignacion, idUsuario, rol);
        List<Map<String, Object>> actividades = actividadDAO.listarPorAsignacion(idAsignacion);
        List<Map<String, Object>> inscritos = inscripcionDAO.listarNominaPorAsignacion(idAsignacion, null, 1000, 0);

        List<Map<String, Object>> notasRaw = dao.listarPorAsignacion(idAsignacion);

        // Organizar notas por inscripción
        Map<Integer, Map<Integer, Map<String, Object>>> notasPorInscripcion = new HashMap<>();
        for (Map<String, Object> n : notasRaw) {
            int idInscripcion = (Integer) n.get("idInscripcion");
            int idActividad = (Integer) n.get("idActividadEvaluable");
            BigDecimal valor = (BigDecimal) n.get("valor");
            int idNota = (Integer) n.get("idNota");
            Map<String, Object> notaInfo = new HashMap<>();
            notaInfo.put("idNota", idNota);
            notaInfo.put("valor", valor);
            notasPorInscripcion.computeIfAbsent(idInscripcion, k -> new HashMap<>()).put(idActividad, notaInfo);
        }

        List<Map<String, Object>> estudiantes = new ArrayList<>();
        for (Map<String, Object> ins : inscritos) {
            int idInscripcion = (Integer) ins.get("idInscripcion");
            Map<String, Object> est = new HashMap<>();
            est.put("idInscripcion", idInscripcion);
            est.put("carnet", ins.get("carnet"));
            est.put("nombres", ins.get("nombres"));
            est.put("apellidos", ins.get("apellidos"));
            est.put("estado", ins.get("estadoEstudiante"));
            Map<Integer, Map<String, Object>> notas = notasPorInscripcion.get(idInscripcion);
            Map<String, Object> notasMap = new HashMap<>();
            BigDecimal total = BigDecimal.ZERO;
            if (notas != null) {
                for (Map.Entry<Integer, Map<String, Object>> entry : notas.entrySet()) {
                    notasMap.put(String.valueOf(entry.getKey()), entry.getValue());
                    total = total.add((BigDecimal) entry.getValue().get("valor"));
                }
            }
            est.put("notas", notasMap);
            est.put("total", total);
            estudiantes.add(est);
        }

        Map<String, Object> r = new HashMap<>();
        r.put("actividades", actividades);
        r.put("estudiantes", estudiantes);
        r.put("totalActividades", actividades.stream()
                .map(a -> (BigDecimal) a.get("puntos"))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return r;
    }

    /** Ingresa notas para una actividad (batch). */
    public Map<String, Object> ingresar(int idAsignacion, int idActividadEvaluable,
            List<Map<String, Object>> notas, int idUsuario, String rol) throws Exception {
        validarAsignacionDelMaestro(idAsignacion, idUsuario, rol);
        validarCicloActivo(idAsignacion);

        Map<String, Object> actividad = buscarActividad(idActividadEvaluable, idAsignacion);
        if (actividad == null) {
            throw new NotFoundException("Actividad no encontrada");
        }
        BigDecimal puntos = (BigDecimal) actividad.get("puntos");

        if (notas == null || notas.isEmpty()) {
            throw new IllegalArgumentException("Indica al menos una nota");
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            int registradas = 0;
            for (Map<String, Object> n : notas) {
                int idInscripcion = entero(n.get("idInscripcion"));
                BigDecimal valor = decimal(n.get("valor"));
                if (valor == null) {
                    throw new IllegalArgumentException("La nota es obligatoria");
                }
                if (valor.compareTo(BigDecimal.ZERO) < 0 || valor.compareTo(puntos) > 0) {
                    throw new IllegalArgumentException("La nota debe estar entre 0 y " + puntos);
                }
                // Verificar si ya existe nota para esta actividad y estudiante
                if (dao.buscarPorActividadYInscripcion(idActividadEvaluable, idInscripcion) != null) {
                    throw new IllegalArgumentException("Ya existe una nota para ese estudiante en esta actividad");
                }
                dao.crear(idActividadEvaluable, idInscripcion, valor);
                registradas++;
            }
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("mensaje", "Notas registradas");
            r.put("registradas", registradas);
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir el ingreso de notas");
                ex.printStackTrace();
            }
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                throw new IllegalArgumentException("Ya existe una nota para ese estudiante en esta actividad");
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

    /** Modifica una nota. */
    public void modificar(int idNota, int idUsuario, String rol, BigDecimal valor) throws Exception {
        Map<String, Object> nota = dao.buscarPorId(idNota);
        if (nota == null) {
            throw new NotFoundException("Nota no encontrada");
        }
        int idAsignacion = (Integer) nota.get("idAsignacion");
        validarAsignacionDelMaestro(idAsignacion, idUsuario, rol);
        validarCicloActivo(idAsignacion);

        BigDecimal puntos = (BigDecimal) nota.get("puntos");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0 || valor.compareTo(puntos) > 0) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y " + puntos);
        }
        dao.actualizar(idNota, valor);
    }

    private void validarAsignacionDelMaestro(int idAsignacion, int idUsuario, String rol) throws Exception {
        Map<String, Object> a = asignacionDAO.buscarPorId(idAsignacion);
        if (a == null) {
            throw new NotFoundException("Asignación no encontrada");
        }
        if ("Super Admin".equals(rol)) {
            return;
        }
        Empleado e = empleadoDAO.buscarPorUsuario(idUsuario);
        if (e == null || !e.getIdEmpleado().equals(a.get("idEmpleado"))) {
            throw new SecurityException("No autorizado para esta asignación");
        }
    }

    private void validarCicloActivo(int idAsignacion) throws Exception {
        Map<String, Object> a = asignacionDAO.buscarPorId(idAsignacion);
        if (a == null) {
            throw new NotFoundException("Asignación no encontrada");
        }
        int anio = (Integer) a.get("anio");
        com.mycompany.backend.dao.AnioLectivoDAO anioDAO = new com.mycompany.backend.dao.AnioLectivoDAO();
        com.mycompany.backend.dto.AnioLectivo al = anioDAO.buscarPorAnio(anio);
        if (al == null || !"Activo".equals(al.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden ingresar notas en un ciclo Activo");
        }
    }

    private Map<String, Object> buscarActividad(int idActividadEvaluable, int idAsignacion) throws SQLException {
        List<Map<String, Object>> actividades = actividadDAO.listarPorAsignacion(idAsignacion);
        for (Map<String, Object> a : actividades) {
            if (((Integer) a.get("idActividadEvaluable")) == idActividadEvaluable) {
                return a;
            }
        }
        return null;
    }

    private String texto(Object v) {
        return v == null || "null".equals(String.valueOf(v)) ? "" : String.valueOf(v).trim();
    }

    private BigDecimal decimal(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int entero(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }
}