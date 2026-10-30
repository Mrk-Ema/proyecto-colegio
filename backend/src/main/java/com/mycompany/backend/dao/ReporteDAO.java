package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteDAO {

    // ==================== SOLVENTES Y MOROSOS ====================

    public List<Map<String, Object>> listarSolventesMorosos(int anio, Integer grado, Integer seccion)
            throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT i.id_inscripcion, i.id_año_lectivo AS anio, "
                + "e.id_estudiante, e.carnet, e.nombres, e.apellidos, e.estado AS estado_estudiante, "
                + "s.id_seccion, s.nombre AS seccion, g.id_grado, g.nombre AS grado, "
                + "n.id_nivel, n.nombre AS nivel, c.nombre AS carrera, "
                + "cu.monto_inscripcion, cu.monto_mensualidad, "
                + "(SELECT COALESCE(SUM(p.monto), 0) FROM pago p "
                + "  WHERE p.id_inscripcion = i.id_inscripcion AND p.estado = 'Registrado' "
                + "  AND p.concepto IN ('Inscripcion', 'Mensualidad')) AS total_pagado "
                + "FROM inscripcion i "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera "
                + "LEFT JOIN cuota cu ON cu.id_grado = g.id_grado AND cu.id_año_lectivo = i.id_año_lectivo "
                + "WHERE i.id_año_lectivo = ?");
        List<Object> p = new ArrayList<>();
        p.add(anio);
        if (grado != null) {
            sb.append(" AND g.id_grado = ?");
            p.add(grado);
        }
        if (seccion != null) {
            sb.append(" AND s.id_seccion = ?");
            p.add(seccion);
        }
        sb.append(" ORDER BY g.nombre, s.nombre, e.apellidos, e.nombres");
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < p.size(); i++) {
                ps.setObject(i + 1, p.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idInscripcion", rs.getInt("id_inscripcion"));
                    m.put("anio", rs.getInt("anio"));
                    m.put("idEstudiante", rs.getInt("id_estudiante"));
                    m.put("carnet", rs.getString("carnet"));
                    m.put("nombres", rs.getString("nombres"));
                    m.put("apellidos", rs.getString("apellidos"));
                    m.put("estadoEstudiante", rs.getString("estado_estudiante"));
                    m.put("idSeccion", rs.getInt("id_seccion"));
                    m.put("seccion", rs.getString("seccion"));
                    m.put("idGrado", rs.getInt("id_grado"));
                    m.put("grado", rs.getString("grado"));
                    m.put("nivel", rs.getString("nivel"));
                    m.put("carrera", rs.getString("carrera"));
                    m.put("montoInscripcion", rs.getBigDecimal("monto_inscripcion"));
                    m.put("montoMensualidad", rs.getBigDecimal("monto_mensualidad"));
                    m.put("totalPagado", rs.getBigDecimal("total_pagado"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    // ==================== RENDIMIENTO ====================

    public List<Map<String, Object>> listarCursosDeSeccion(int anio, int idSeccion) throws SQLException {
        String sql = "SELECT am.id_asignacion, c.id_curso, c.nombre AS curso "
                + "FROM asignacion_maestro am "
                + "JOIN curso c ON c.id_curso = am.id_curso "
                + "WHERE am.id_año_lectivo = ? AND am.id_seccion = ? "
                + "ORDER BY c.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idAsignacion", rs.getInt("id_asignacion"));
                    m.put("idCurso", rs.getInt("id_curso"));
                    m.put("curso", rs.getString("curso"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public List<Map<String, Object>> listarEstudiantesDeSeccion(int anio, int idSeccion) throws SQLException {
        String sql = "SELECT i.id_inscripcion, e.id_estudiante, e.carnet, e.nombres, e.apellidos "
                + "FROM inscripcion i "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "WHERE i.id_año_lectivo = ? AND i.id_seccion = ? "
                + "ORDER BY e.apellidos, e.nombres";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idInscripcion", rs.getInt("id_inscripcion"));
                    m.put("idEstudiante", rs.getInt("id_estudiante"));
                    m.put("carnet", rs.getString("carnet"));
                    m.put("nombres", rs.getString("nombres"));
                    m.put("apellidos", rs.getString("apellidos"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public List<Map<String, Object>> listarNotasDeSeccion(int anio, int idSeccion) throws SQLException {
        String sql = "SELECT n.id_nota, n.valor, ae.id_actividad_evaluable, ae.id_asignacion, ae.puntos, "
                + "ae.nombre AS actividad, i.id_inscripcion, e.id_estudiante "
                + "FROM nota n "
                + "JOIN actividad_evaluable ae ON ae.id_actividad_evaluable = n.id_actividad_evaluable "
                + "JOIN asignacion_maestro am ON am.id_asignacion = ae.id_asignacion "
                + "JOIN inscripcion i ON i.id_inscripcion = n.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "WHERE i.id_año_lectivo = ? AND i.id_seccion = ? "
                + "ORDER BY ae.id_asignacion, e.id_estudiante";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idNota", rs.getInt("id_nota"));
                    m.put("valor", rs.getBigDecimal("valor"));
                    m.put("idActividadEvaluable", rs.getInt("id_actividad_evaluable"));
                    m.put("idAsignacion", rs.getInt("id_asignacion"));
                    m.put("puntos", rs.getBigDecimal("puntos"));
                    m.put("actividad", rs.getString("actividad"));
                    m.put("idInscripcion", rs.getInt("id_inscripcion"));
                    m.put("idEstudiante", rs.getInt("id_estudiante"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    /**
     * Ingresos por concepto: total bruto y desglose por Inscripción, Mensualidad y Actividad.
     * Solo pagos Registrados, sin multas de biblioteca.
     */
    public List<Map<String, Object>> listarIngresos(String desde, String hasta) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT concepto, SUM(monto) AS monto "
                + "FROM pago WHERE estado = 'Registrado'");
        List<Object> params = new ArrayList<>();
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND DATE(fecha_pago) >= ?");
            params.add(desde.trim());
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND DATE(fecha_pago) <= ?");
            params.add(hasta.trim());
        }
        sb.append(" GROUP BY concepto ORDER BY concepto");
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("concepto", rs.getString("concepto"));
                    m.put("monto", rs.getBigDecimal("monto"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    /**
     * Préstamos activos vencidos (fecha esperada < hoy), con filtros de rango
     * de vencimiento y libro opcional. Incluye los días de atraso.
     */
    public List<Map<String, Object>> listarPrestamosEnMora(String desde, String hasta, Integer libro,
            int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT p.id_prestamo, p.fecha_prestamo, p.fecha_esperada, "
                + "p.id_libro, l.titulo AS libro, "
                + "p.id_estudiante, p.id_empleado, "
                + "e.nombres AS est_nombres, e.apellidos AS est_apellidos, e.carnet AS est_carnet, "
                + "em.nombres AS emp_nombres, em.apellidos AS emp_apellidos "
                + "FROM prestamo p JOIN libro l ON l.id_libro = p.id_libro "
                + "LEFT JOIN estudiante e ON e.id_estudiante = p.id_estudiante "
                + "LEFT JOIN empleado em ON em.id_empleado = p.id_empleado "
                + "WHERE p.estado = 'Activo' AND p.fecha_esperada < CURDATE()");
        List<Object> params = new ArrayList<>();
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND p.fecha_esperada >= ?");
            params.add(desde.trim());
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND p.fecha_esperada <= ?");
            params.add(hasta.trim());
        }
        if (libro != null) {
            sb.append(" AND p.id_libro = ?");
            params.add(libro);
        }
        sb.append(" ORDER BY p.fecha_esperada ASC, p.id_prestamo DESC LIMIT ? OFFSET ?");
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            int i = 1;
            for (; i <= params.size(); i++) {
                ps.setObject(i, params.get(i - 1));
            }
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idPrestamo", rs.getInt("id_prestamo"));
                    m.put("fechaPrestamo", rs.getDate("fecha_prestamo").toLocalDate().toString());
                    m.put("fechaEsperada", rs.getDate("fecha_esperada").toLocalDate().toString());
                    m.put("idLibro", rs.getInt("id_libro"));
                    m.put("libro", rs.getString("libro"));
                    int idEst = rs.getInt("id_estudiante");
                    m.put("idEstudiante", rs.wasNull() ? null : idEst);
                    int idEmp = rs.getInt("id_empleado");
                    m.put("idEmpleado", rs.wasNull() ? null : idEmp);
                    if (idEst > 0) {
                        m.put("prestatario", rs.getString("est_nombres") + " " + rs.getString("est_apellidos"));
                        m.put("tipoPrestatario", "Estudiante");
                        m.put("carnet", rs.getString("est_carnet"));
                    } else {
                        m.put("prestatario", rs.getString("emp_nombres") + " " + rs.getString("emp_apellidos"));
                        m.put("tipoPrestatario", "Maestro");
                        m.put("carnet", null);
                    }
                    l.add(m);
                }
                return l;
            }
        }
    }

    public int contarPrestamosEnMora(String desde, String hasta, Integer libro) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM prestamo p "
                + "WHERE p.estado = 'Activo' AND p.fecha_esperada < CURDATE()");
        List<Object> params = new ArrayList<>();
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND p.fecha_esperada >= ?");
            params.add(desde.trim());
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND p.fecha_esperada <= ?");
            params.add(hasta.trim());
        }
        if (libro != null) {
            sb.append(" AND p.id_libro = ?");
            params.add(libro);
        }
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean existeAnio(int anio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM año_lectivo WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // ==================== HISTORIAL DE PAGOS ====================

    public List<Map<String, Object>> listarHistorialPagos(int idEstudiante, String desde, String hasta,
            int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT p.id_pago, p.numero_correlativo, p.concepto, p.mes, "
                + "p.monto, p.fecha_pago, p.estado, p.motivo_anulacion, "
                + "i.id_año_lectivo AS anio, g.nombre AS grado, s.nombre AS seccion "
                + "FROM pago p "
                + "JOIN inscripcion i ON i.id_inscripcion = p.id_inscripcion "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "WHERE i.id_estudiante = ?");
        List<Object> params = new ArrayList<>();
        params.add(idEstudiante);
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND DATE(p.fecha_pago) >= ?");
            params.add(desde.trim());
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND DATE(p.fecha_pago) <= ?");
            params.add(hasta.trim());
        }
        sb.append(" ORDER BY p.fecha_pago DESC, p.id_pago DESC LIMIT ? OFFSET ?");
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            int i = 1;
            for (; i <= params.size(); i++) {
                ps.setObject(i, params.get(i - 1));
            }
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idPago", rs.getInt("id_pago"));
                    m.put("numeroCorrelativo", rs.getString("numero_correlativo"));
                    m.put("concepto", rs.getString("concepto"));
                    m.put("mes", rs.getString("mes"));
                    m.put("monto", rs.getBigDecimal("monto"));
                    java.sql.Timestamp fp = rs.getTimestamp("fecha_pago");
                    m.put("fechaPago", fp != null ? fp.toLocalDateTime().toString().replace('T', ' ') : null);
                    m.put("estado", rs.getString("estado"));
                    m.put("motivoAnulacion", rs.getString("motivo_anulacion"));
                    m.put("anio", rs.getInt("anio"));
                    m.put("grado", rs.getString("grado"));
                    m.put("seccion", rs.getString("seccion"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public int contarHistorialPagos(int idEstudiante, String desde, String hasta) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM pago p "
                + "JOIN inscripcion i ON i.id_inscripcion = p.id_inscripcion "
                + "WHERE i.id_estudiante = ?");
        List<Object> params = new ArrayList<>();
        params.add(idEstudiante);
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND DATE(p.fecha_pago) >= ?");
            params.add(desde.trim());
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND DATE(p.fecha_pago) <= ?");
            params.add(hasta.trim());
        }
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}