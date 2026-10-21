package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InscripcionDAO {

    public boolean existePorAnio(int anio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscripcion WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existePorGradoDeAnio(int anio, int idGrado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscripcion i "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "WHERE i.id_año_lectivo = ? AND s.id_grado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idGrado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existePorSeccionEnActivo(int idSeccion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscripcion i "
                + "JOIN año_lectivo a ON a.id_año_lectivo = i.id_año_lectivo AND a.estado = 'Activo' "
                + "WHERE i.id_seccion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existePorEstudianteYAnio(int idEstudiante, int anio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscripcion WHERE id_estudiante = ? AND id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int crear(int idEstudiante, int anio, int idSeccion) throws SQLException {
        String sql = "INSERT INTO inscripcion (fecha_inscripcion, id_estudiante, id_año_lectivo, id_seccion) "
                + "VALUES (CURRENT_DATE, ?, ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, anio);
            ps.setInt(3, idSeccion);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Map<String, Object> buscarPorId(int idInscripcion) throws SQLException {
        String sql = "SELECT i.id_inscripcion, i.id_año_lectivo AS anio, i.id_seccion, "
                + "i.fecha_inscripcion, e.id_estudiante, e.carnet, e.nombres, e.apellidos, "
                + "e.estado AS estado_estudiante, s.nombre AS seccion, g.id_grado, g.nombre AS grado, "
                + "n.id_nivel, n.nombre AS nivel, c.id_carrera, c.nombre AS carrera "
                + "FROM inscripcion i "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera "
                + "WHERE i.id_inscripcion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idInscripcion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idInscripcion", rs.getInt("id_inscripcion"));
                m.put("anio", rs.getInt("anio"));
                m.put("idSeccion", rs.getInt("id_seccion"));
                Date f = rs.getDate("fecha_inscripcion");
                m.put("fechaInscripcion", f != null ? f.toLocalDate().toString() : null);
                m.put("idEstudiante", rs.getInt("id_estudiante"));
                m.put("carnet", rs.getString("carnet"));
                m.put("nombres", rs.getString("nombres"));
                m.put("apellidos", rs.getString("apellidos"));
                m.put("estadoEstudiante", rs.getString("estado_estudiante"));
                m.put("seccion", rs.getString("seccion"));
                m.put("idGrado", rs.getInt("id_grado"));
                m.put("grado", rs.getString("grado"));
                m.put("idNivel", rs.getInt("id_nivel"));
                m.put("nivel", rs.getString("nivel"));
                int ic = rs.getInt("id_carrera");
                m.put("idCarrera", rs.wasNull() ? null : ic);
                m.put("carrera", rs.getString("carrera"));
                return m;
            }
        }
    }

    public int contar(Integer anio, Integer nivel, Integer grado, Integer seccion,
            String estudiante) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM inscripcion i "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel WHERE 1=1");
        List<Object> p = new ArrayList<>();
        whereFiltros(sb, p, anio, nivel, grado, seccion, estudiante);
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < p.size(); i++) {
                ps.setObject(i + 1, p.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Map<String, Object>> listar(Integer anio, Integer nivel, Integer grado, Integer seccion,
            String estudiante, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT i.id_inscripcion, i.id_año_lectivo AS anio, "
                + "i.fecha_inscripcion, e.id_estudiante, e.carnet, e.nombres, e.apellidos, "
                + "e.estado AS estado_estudiante, s.id_seccion, s.nombre AS seccion, "
                + "g.id_grado, g.nombre AS grado, n.id_nivel, n.nombre AS nivel, "
                + "c.id_carrera, c.nombre AS carrera "
                + "FROM inscripcion i "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera WHERE 1=1");
        List<Object> p = new ArrayList<>();
        whereFiltros(sb, p, anio, nivel, grado, seccion, estudiante);
        sb.append(" ORDER BY i.id_año_lectivo DESC, e.apellidos, e.nombres LIMIT ? OFFSET ?");
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            int i = 1;
            for (; i <= p.size(); i++) {
                ps.setObject(i, p.get(i - 1));
            }
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idInscripcion", rs.getInt("id_inscripcion"));
                    m.put("anio", rs.getInt("anio"));
                    m.put("fechaInscripcion", rs.getDate("fecha_inscripcion").toLocalDate().toString());
                    m.put("idEstudiante", rs.getInt("id_estudiante"));
                    m.put("carnet", rs.getString("carnet"));
                    m.put("nombres", rs.getString("nombres"));
                    m.put("apellidos", rs.getString("apellidos"));
                    m.put("estadoEstudiante", rs.getString("estado_estudiante"));
                    m.put("idSeccion", rs.getInt("id_seccion"));
                    m.put("seccion", rs.getString("seccion"));
                    m.put("idGrado", rs.getInt("id_grado"));
                    m.put("grado", rs.getString("grado"));
                    m.put("idNivel", rs.getInt("id_nivel"));
                    m.put("nivel", rs.getString("nivel"));
                    int ic = rs.getInt("id_carrera");
                    m.put("idCarrera", rs.wasNull() ? null : ic);
                    m.put("carrera", rs.getString("carrera"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    /** Inscripción de un estudiante en un año, con su grado y su sección. */
    public Map<String, Object> buscarPorEstudianteYAnio(int idEstudiante, int anio) throws SQLException {
        String sql = "SELECT i.id_inscripcion, i.id_año_lectivo AS anio, i.id_seccion, "
                + "s.id_grado, g.nombre AS grado, s.nombre AS seccion, i.fecha_inscripcion "
                + "FROM inscripcion i "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "WHERE i.id_estudiante = ? AND i.id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idInscripcion", rs.getInt("id_inscripcion"));
                m.put("anio", rs.getInt("anio"));
                m.put("idSeccion", rs.getInt("id_seccion"));
                m.put("idGrado", rs.getInt("id_grado"));
                m.put("grado", rs.getString("grado"));
                m.put("seccion", rs.getString("seccion"));
                Date f = rs.getDate("fecha_inscripcion");
                m.put("fechaInscripcion", f != null ? f.toLocalDate().toString() : null);
                return m;
            }
        }
    }

    public void cambiarSeccion(int idInscripcion, int idSeccion) throws SQLException {
        String sql = "UPDATE inscripcion SET id_seccion = ? WHERE id_inscripcion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idSeccion);
            ps.setInt(2, idInscripcion);
            ps.executeUpdate();
        }
    }

    private void whereFiltros(StringBuilder sb, List<Object> p, Integer anio, Integer nivel,
            Integer grado, Integer seccion, String estudiante) {
        if (anio != null) {
            sb.append(" AND i.id_año_lectivo = ?");
            p.add(anio);
        }
        if (nivel != null) {
            sb.append(" AND n.id_nivel = ?");
            p.add(nivel);
        }
        if (grado != null) {
            sb.append(" AND g.id_grado = ?");
            p.add(grado);
        }
        if (seccion != null) {
            sb.append(" AND s.id_seccion = ?");
            p.add(seccion);
        }
        if (estudiante != null && !estudiante.isBlank()) {
            sb.append(" AND (e.nombres LIKE ? OR e.apellidos LIKE ? OR e.carnet LIKE ?)");
            String like = "%" + estudiante.trim() + "%";
            p.add(like);
            p.add(like);
            p.add(like);
        }
    }

    public java.util.List<java.util.Map<String, Object>> historialPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT i.id_año_lectivo AS anio, g.nombre AS grado, s.nombre AS seccion, "
                + "i.fecha_inscripcion AS fechaInscripcion "
                + "FROM inscripcion i JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "WHERE i.id_estudiante = ? ORDER BY i.id_año_lectivo DESC";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                java.util.List<java.util.Map<String, Object>> l = new java.util.ArrayList<>();
                while (rs.next()) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("anio", rs.getInt("anio"));
                    m.put("grado", rs.getString("grado"));
                    m.put("seccion", rs.getString("seccion"));
                    java.sql.Date f = rs.getDate("fechaInscripcion");
                    m.put("fechaInscripcion", f != null ? f.toLocalDate().toString() : null);
                    l.add(m);
                }
                return l;
            }
        }
    }
}
