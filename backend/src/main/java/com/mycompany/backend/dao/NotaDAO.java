package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotaDAO {

    public List<Map<String, Object>> listarPorAsignacion(int idAsignacion) throws SQLException {
        String sql = "SELECT n.id_nota, n.id_actividad_evaluable, n.id_inscripcion, n.valor, n.fecha_registro, "
                + "e.id_estudiante, e.carnet, e.nombres, e.apellidos "
                + "FROM nota n "
                + "JOIN actividad_evaluable ae ON ae.id_actividad_evaluable = n.id_actividad_evaluable "
                + "JOIN inscripcion i ON i.id_inscripcion = n.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "WHERE ae.id_asignacion = ? "
                + "ORDER BY e.apellidos, e.nombres, ae.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idNota", rs.getInt("id_nota"));
                    m.put("idActividadEvaluable", rs.getInt("id_actividad_evaluable"));
                    m.put("idInscripcion", rs.getInt("id_inscripcion"));
                    m.put("valor", rs.getBigDecimal("valor"));
                    Timestamp fr = rs.getTimestamp("fecha_registro");
                    m.put("fechaRegistro", fr != null ? fr.toLocalDateTime().toString().replace('T', ' ') : null);
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

    public List<Map<String, Object>> listarPorActividad(int idActividadEvaluable) throws SQLException {
        String sql = "SELECT n.id_nota, n.id_actividad_evaluable, n.id_inscripcion, n.valor, "
                + "e.id_estudiante, e.carnet, e.nombres, e.apellidos "
                + "FROM nota n "
                + "JOIN inscripcion i ON i.id_inscripcion = n.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "WHERE n.id_actividad_evaluable = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividadEvaluable);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idNota", rs.getInt("id_nota"));
                    m.put("idActividadEvaluable", rs.getInt("id_actividad_evaluable"));
                    m.put("idInscripcion", rs.getInt("id_inscripcion"));
                    m.put("valor", rs.getBigDecimal("valor"));
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

    public int crear(int idActividadEvaluable, int idInscripcion, BigDecimal valor) throws SQLException {
        String sql = "INSERT INTO nota (valor, fecha_registro, id_actividad_evaluable, id_inscripcion) "
                + "VALUES (?, CURRENT_TIMESTAMP, ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, valor);
            ps.setInt(2, idActividadEvaluable);
            ps.setInt(3, idInscripcion);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(int id, BigDecimal valor) throws SQLException {
        String sql = "UPDATE nota SET valor = ? WHERE id_nota = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, valor);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT n.id_nota, n.id_actividad_evaluable, n.id_inscripcion, n.valor, "
                + "ae.id_asignacion, ae.puntos, e.id_estudiante, e.carnet, e.nombres, e.apellidos "
                + "FROM nota n "
                + "JOIN actividad_evaluable ae ON ae.id_actividad_evaluable = n.id_actividad_evaluable "
                + "JOIN inscripcion i ON i.id_inscripcion = n.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "WHERE n.id_nota = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idNota", rs.getInt("id_nota"));
                m.put("idActividadEvaluable", rs.getInt("id_actividad_evaluable"));
                m.put("idInscripcion", rs.getInt("id_inscripcion"));
                m.put("valor", rs.getBigDecimal("valor"));
                m.put("puntos", rs.getBigDecimal("puntos"));
                m.put("idAsignacion", rs.getInt("id_asignacion"));
                m.put("idEstudiante", rs.getInt("id_estudiante"));
                m.put("carnet", rs.getString("carnet"));
                m.put("nombres", rs.getString("nombres"));
                m.put("apellidos", rs.getString("apellidos"));
                return m;
            }
        }
    }

    /** Busca una nota por actividad y estudiante (para evitar duplicados). */
    public Map<String, Object> buscarPorActividadYInscripcion(int idActividadEvaluable, int idInscripcion)
            throws SQLException {
        String sql = "SELECT id_nota, valor FROM nota WHERE id_actividad_evaluable = ? AND id_inscripcion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividadEvaluable);
            ps.setInt(2, idInscripcion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idNota", rs.getInt("id_nota"));
                m.put("valor", rs.getBigDecimal("valor"));
                return m;
            }
        }
    }

    /** Cuenta las notas de una actividad (para saber cuántas se recalcularán). */
    public int contarPorActividad(int idActividadEvaluable) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nota WHERE id_actividad_evaluable = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividadEvaluable);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void eliminarPorActividad(int idActividadEvaluable) throws SQLException {
        String sql = "DELETE FROM nota WHERE id_actividad_evaluable = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividadEvaluable);
            ps.executeUpdate();
        }
    }

    public BigDecimal sumarPorInscripcion(int idInscripcion) throws SQLException {
        String sql = "SELECT COALESCE(SUM(valor), 0) AS total FROM nota WHERE id_inscripcion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idInscripcion);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("total");
            }
        }
    }

    /**
     * Notas de un estudiante en un año, con actividad, curso, grado y sección.
     * Para las consultas del estudiante (solo lectura de sus propios datos).
     */
    public List<Map<String, Object>> listarPorEstudianteYAnio(int idEstudiante, int anio) throws SQLException {
        String sql = "SELECT n.id_nota, n.valor, n.fecha_registro, "
                + "ae.id_actividad_evaluable, ae.nombre AS actividad, ae.puntos, "
                + "am.id_asignacion, c.nombre AS curso, g.nombre AS grado, s.nombre AS seccion, "
                + "am.id_año_lectivo AS anio "
                + "FROM nota n "
                + "JOIN actividad_evaluable ae ON ae.id_actividad_evaluable = n.id_actividad_evaluable "
                + "JOIN asignacion_maestro am ON am.id_asignacion = ae.id_asignacion "
                + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN curso c ON c.id_curso = am.id_curso "
                + "JOIN inscripcion i ON i.id_inscripcion = n.id_inscripcion "
                + "WHERE i.id_estudiante = ? AND i.id_año_lectivo = ? "
                + "ORDER BY c.nombre, ae.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idNota", rs.getInt("id_nota"));
                    m.put("valor", rs.getBigDecimal("valor"));
                    m.put("idActividadEvaluable", rs.getInt("id_actividad_evaluable"));
                    m.put("actividad", rs.getString("actividad"));
                    m.put("puntos", rs.getBigDecimal("puntos"));
                    m.put("idAsignacion", rs.getInt("id_asignacion"));
                    m.put("curso", rs.getString("curso"));
                    m.put("grado", rs.getString("grado"));
                    m.put("seccion", rs.getString("seccion"));
                    m.put("anio", rs.getInt("anio"));
                    l.add(m);
                }
                return l;
            }
        }
    }
}