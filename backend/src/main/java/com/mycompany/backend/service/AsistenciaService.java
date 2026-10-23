package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.AsignacionDAO;
import com.mycompany.backend.dao.AsistenciaDAO;
import com.mycompany.backend.dao.EmpleadoDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dto.AnioLectivo;
import com.mycompany.backend.dto.Empleado;
import jakarta.ws.rs.NotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cursos y asistencia del Maestro. El Maestro solo ve sus propias asignaciones
 * y solo toma asistencia en ciclos Activos.
 */
public class AsistenciaService {

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();

    /** Años no Cerrados en los que el maestro tiene asignaciones. */
    public List<Map<String, Object>> anios(int idUsuario) throws SQLException {
        Empleado e = empleadoDAO.buscarPorUsuario(idUsuario);
        if (e == null) {
            throw new NotFoundException("Empleado no encontrado");
        }
        return asignacionDAO.listarAniosPorEmpleado(e.getIdEmpleado());
    }

    /** Asignaciones del maestro en un año no Cerrado, con su estado de ponderación. */
    public List<Map<String, Object>> cursos(int idUsuario, Integer anio) throws SQLException {
        Empleado e = empleadoDAO.buscarPorUsuario(idUsuario);
        if (e == null) {
            throw new NotFoundException("Empleado no encontrado");
        }
        return asignacionDAO.listarPorEmpleado(e.getIdEmpleado(), anio);
    }

    /** Nómina paginada de un curso (inscritos de la sección de la asignación). */
    public Map<String, Object> nomina(int idAsignacion, int idUsuario, String nombre,
            int page, int size) throws Exception {
        validarAsignacionDelMaestro(idAsignacion, idUsuario);
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = inscripcionDAO.contarNominaPorAsignacion(idAsignacion, nombre);
        List<Map<String, Object>> datos = inscripcionDAO.listarNominaPorAsignacion(
                idAsignacion, nombre, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    /** Toma la asistencia del día para un curso. */
    public Map<String, Object> tomarAsistencia(int idAsignacion, int idUsuario,
            String fecha, List<Map<String, Object>> asistencias) throws Exception {
        validarAsignacionDelMaestro(idAsignacion, idUsuario);
        Map<String, Object> asignacion = asignacionDAO.buscarPorId(idAsignacion);
        if (asignacion == null) {
            throw new NotFoundException("Asignación no encontrada");
        }
        int anio = (Integer) asignacion.get("anio");
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null || !"Activo".equals(a.getEstado())) {
            throw new IllegalArgumentException("Solo se toma asistencia en un ciclo Activo");
        }
        if (asistencias == null || asistencias.isEmpty()) {
            throw new IllegalArgumentException("Indica la asistencia de al menos un estudiante");
        }
        LocalDate fechaAsistencia;
        try {
            fechaAsistencia = LocalDate.parse(fecha);
        } catch (Exception e) {
            throw new IllegalArgumentException("Fecha inválida");
        }
        if (fechaAsistencia.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede tomar asistencia de un día futuro");
        }
        if (asistenciaDAO.existeParaFecha(idAsignacion, fechaAsistencia)) {
            throw new IllegalArgumentException("Ya existe asistencia para esa fecha en este curso");
        }
        for (Map<String, Object> as : asistencias) {
            String estado = String.valueOf(as.get("estado"));
            if (!"Presente".equals(estado) && !"Ausente".equals(estado)
                    && !"Tardío".equals(estado) && !"Permiso".equals(estado)) {
                throw new IllegalArgumentException("Estado de asistencia inválido");
            }
        }
        // Transacción: se guardan todos o ninguno.
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            for (Map<String, Object> as : asistencias) {
                int idInscripcion = entero(as.get("idInscripcion"));
                String estado = String.valueOf(as.get("estado"));
                asistenciaDAO.crear(idAsignacion, idInscripcion, fechaAsistencia, estado);
            }
            cn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir la asistencia");
                ex.printStackTrace();
            }
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                throw new IllegalArgumentException("Ya existe asistencia para esa fecha en este curso");
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
        Map<String, Object> r = new HashMap<>();
        r.put("mensaje", "Asistencia registrada");
        r.put("registrados", asistencias.size());
        return r;
    }

    /** Historial de asistencia de un curso con filtros. */
    public Map<String, Object> listarAsistencia(int idAsignacion, int idUsuario, String estado,
            String estudiante, String desde, String hasta, int page, int size) throws Exception {
        validarAsignacionDelMaestro(idAsignacion, idUsuario);
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = asistenciaDAO.contar(idAsignacion, estado, estudiante, desde, hasta);
        List<Map<String, Object>> datos = asistenciaDAO.listar(idAsignacion, estado, estudiante,
                desde, hasta, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    /** Corrige el estado de una asistencia. Solo en ciclo Activo. */
    public void corregirAsistencia(int idAsistencia, int idUsuario, String estado) throws Exception {
        Map<String, Object> as = asistenciaDAO.buscarPorId(idAsistencia);
        if (as == null) {
            throw new NotFoundException("Asistencia no encontrada");
        }
        validarAsignacionDelMaestro((Integer) as.get("idAsignacion"), idUsuario);
        int anio = (Integer) as.get("anio");
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null || !"Activo".equals(a.getEstado())) {
            throw new IllegalArgumentException("Solo se corrige asistencia en un ciclo Activo");
        }
        if (!"Presente".equals(estado) && !"Ausente".equals(estado)
                && !"Tardío".equals(estado) && !"Permiso".equals(estado)) {
            throw new IllegalArgumentException("Estado de asistencia inválido");
        }
        asistenciaDAO.actualizarEstado(idAsistencia, estado);
    }

    private void validarAsignacionDelMaestro(int idAsignacion, int idUsuario) throws Exception {
        Map<String, Object> a = asignacionDAO.buscarPorId(idAsignacion);
        if (a == null) {
            throw new NotFoundException("Asignación no encontrada");
        }
        Empleado e = empleadoDAO.buscarPorUsuario(idUsuario);
        if (e == null || !e.getIdEmpleado().equals(a.get("idEmpleado"))) {
            throw new SecurityException("No autorizado para esta asignación");
        }
    }

    private int entero(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }
}