package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionDAO {

    public boolean existePorAnio(int anio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM asignacion_maestro WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean cursoTieneMaestro(int anio, int idGrado, int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM asignacion_maestro am JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "WHERE am.id_año_lectivo = ? AND s.id_grado = ? AND am.id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idGrado);
            ps.setInt(3, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean cursoTienePonderacion(int anio, int idGrado, int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM actividad_evaluable ae JOIN asignacion_maestro am ON am.id_asignacion = ae.id_asignacion "
                + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "WHERE am.id_año_lectivo = ? AND s.id_grado = ? AND am.id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idGrado);
            ps.setInt(3, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean cursoTieneNotas(int anio, int idGrado, int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nota n JOIN actividad_evaluable ae ON ae.id_actividad_evaluable = n.id_actividad_evaluable "
                + "JOIN asignacion_maestro am ON am.id_asignacion = ae.id_asignacion "
                + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "WHERE am.id_año_lectivo = ? AND s.id_grado = ? AND am.id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idGrado);
            ps.setInt(3, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int crear(int anio, int idSeccion, int idCurso, int idEmpleado) throws SQLException {
        String sql = "INSERT INTO asignacion_maestro (id_año_lectivo, id_seccion, id_curso, id_empleado) VALUES (?, ?, ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, anio);
            ps.setInt(2, idSeccion);
            ps.setInt(3, idCurso);
            ps.setInt(4, idEmpleado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean existePorSeccionCursoAnio(int anio, int idSeccion, int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM asignacion_maestro WHERE id_año_lectivo = ? AND id_seccion = ? AND id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idSeccion);
            ps.setInt(3, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT am.id_asignacion, am.id_año_lectivo, s.id_seccion, s.nombre AS seccion, "
                + "g.id_grado, g.nombre AS grado, c.id_curso, c.nombre AS curso, e.id_empleado, e.nombres, e.apellidos "
                + "FROM asignacion_maestro am JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado JOIN curso c ON c.id_curso = am.id_curso "
                + "JOIN empleado e ON e.id_empleado = am.id_empleado WHERE am.id_asignacion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idAsignacion", rs.getInt(1));
                m.put("anio", rs.getInt(2));
                m.put("idSeccion", rs.getInt(3));
                m.put("seccion", rs.getString(4));
                m.put("idGrado", rs.getInt(5));
                m.put("grado", rs.getString(6));
                m.put("idCurso", rs.getInt(7));
                m.put("curso", rs.getString(8));
                m.put("idEmpleado", rs.getInt(9));
                m.put("maestro", rs.getString(10) + " " + rs.getString(11));
                return m;
            }
        }
    }

    public void reasignar(int id, int nuevoEmpleado) throws SQLException {
        String sql = "UPDATE asignacion_maestro SET id_empleado = ? WHERE id_asignacion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, nuevoEmpleado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM asignacion_maestro WHERE id_asignacion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int contar(Integer anio, Integer grado, Integer seccion, Integer curso, String maestro) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM asignacion_maestro am "
                + "JOIN seccion s ON s.id_seccion = am.id_seccion JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN empleado e ON e.id_empleado = am.id_empleado WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (anio != null) {
            sb.append(" AND am.id_año_lectivo = ?");
            p.add(anio);
        }
        if (grado != null) {
            sb.append(" AND g.id_grado = ?");
            p.add(grado);
        }
        if (seccion != null) {
            sb.append(" AND am.id_seccion = ?");
            p.add(seccion);
        }
        if (curso != null) {
            sb.append(" AND am.id_curso = ?");
            p.add(curso);
        }
        if (maestro != null && !maestro.isBlank()) {
            sb.append(" AND CONCAT(e.nombres, ' ', e.apellidos) LIKE ?");
            p.add("%" + maestro.trim() + "%");
        }
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

    public List<Map<String, Object>> listar(Integer anio, Integer grado, Integer seccion, Integer curso, String maestro, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT am.id_asignacion, am.id_año_lectivo, s.id_seccion, s.nombre AS seccion, "
                + "g.id_grado, g.nombre AS grado, c.id_curso, c.nombre AS curso, e.id_empleado, e.nombres, e.apellidos "
                + "FROM asignacion_maestro am JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado JOIN curso c ON c.id_curso = am.id_curso "
                + "JOIN empleado e ON e.id_empleado = am.id_empleado WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (anio != null) {
            sb.append(" AND am.id_año_lectivo = ?");
            p.add(anio);
        }
        if (grado != null) {
            sb.append(" AND g.id_grado = ?");
            p.add(grado);
        }
        if (seccion != null) {
            sb.append(" AND am.id_seccion = ?");
            p.add(seccion);
        }
        if (curso != null) {
            sb.append(" AND am.id_curso = ?");
            p.add(curso);
        }
        if (maestro != null && !maestro.isBlank()) {
            sb.append(" AND CONCAT(e.nombres, ' ', e.apellidos) LIKE ?");
            p.add("%" + maestro.trim() + "%");
        }
        sb.append(" ORDER BY am.id_año_lectivo DESC, g.nombre, s.nombre, c.nombre LIMIT ? OFFSET ?");
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
                    m.put("idAsignacion", rs.getInt(1));
                    m.put("anio", rs.getInt(2));
                    m.put("idSeccion", rs.getInt(3));
                    m.put("seccion", rs.getString(4));
                    m.put("idGrado", rs.getInt(5));
                    m.put("grado", rs.getString(6));
                    m.put("idCurso", rs.getInt(7));
                    m.put("curso", rs.getString(8));
                    m.put("idEmpleado", rs.getInt(9));
                    m.put("maestro", rs.getString(10) + " " + rs.getString(11));
                    l.add(m);
                }
                return l;
            }
        }
    }

    /**
     * Asignaciones de un maestro en un año no Cerrado, con nivel, grado, sección,
     * curso, año y estado de la ponderación (Sin definir/Definida).
     */
    public List<Map<String, Object>> listarPorEmpleado(int idEmpleado, Integer anio) throws SQLException {
        String sql = "SELECT am.id_asignacion, am.id_año_lectivo, s.id_seccion, s.nombre AS seccion, "
                + "g.id_grado, g.nombre AS grado, n.id_nivel, n.nombre AS nivel, "
                + "c.id_curso, c.nombre AS curso, "
                + "CASE WHEN EXISTS (SELECT 1 FROM actividad_evaluable ae WHERE ae.id_asignacion = am.id_asignacion) "
                + "THEN 'Definida' ELSE 'Sin definir' END AS estadoPonderacion "
                + "FROM asignacion_maestro am "
                + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "JOIN curso c ON c.id_curso = am.id_curso "
                + "JOIN año_lectivo a ON a.id_año_lectivo = am.id_año_lectivo "
                + "WHERE am.id_empleado = ? AND a.estado <> 'Cerrado'";
        List<Object> p = new ArrayList<>();
        p.add(idEmpleado);
        if (anio != null) {
            sql += " AND am.id_año_lectivo = ?";
            p.add(anio);
        }
        sql += " ORDER BY am.id_año_lectivo DESC, g.nombre, s.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < p.size(); i++) {
                ps.setObject(i + 1, p.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idAsignacion", rs.getInt("id_asignacion"));
                    m.put("anio", rs.getInt("id_año_lectivo"));
                    m.put("idSeccion", rs.getInt("id_seccion"));
                    m.put("seccion", rs.getString("seccion"));
                    m.put("idGrado", rs.getInt("id_grado"));
                    m.put("grado", rs.getString("grado"));
                    m.put("idNivel", rs.getInt("id_nivel"));
                    m.put("nivel", rs.getString("nivel"));
                    m.put("idCurso", rs.getInt("id_curso"));
                    m.put("curso", rs.getString("curso"));
                    m.put("estadoPonderacion", rs.getString("estadoPonderacion"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    /** Años no Cerrados en los que un maestro tiene asignaciones. */
    public List<Map<String, Object>> listarAniosPorEmpleado(int idEmpleado) throws SQLException {
        String sql = "SELECT DISTINCT am.id_año_lectivo AS anio, a.estado "
                + "FROM asignacion_maestro am "
                + "JOIN año_lectivo a ON a.id_año_lectivo = am.id_año_lectivo "
                + "WHERE am.id_empleado = ? AND a.estado <> 'Cerrado' "
                + "ORDER BY am.id_año_lectivo DESC";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("anio", rs.getInt("anio"));
                    m.put("estado", rs.getString("estado"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public List<Map<String, Object>> listarSinMaestro(int anio, int idSeccion) throws SQLException {
        String sql = "SELECT c.id_curso, c.nombre AS curso FROM estructura_año e "
                + "JOIN curriculo cu ON cu.id_estructura = e.id_estructura JOIN curso c ON c.id_curso = cu.id_curso "
                + "LEFT JOIN asignacion_maestro am ON am.id_año_lectivo = e.id_año_lectivo AND am.id_seccion = ? AND am.id_curso = c.id_curso "
                + "JOIN seccion s ON s.id_seccion = ? "
                + "WHERE e.id_año_lectivo = ? AND e.id_grado = s.id_grado AND am.id_asignacion IS NULL ORDER BY c.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idSeccion);
            ps.setInt(2, idSeccion);
            ps.setInt(3, anio);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idCurso", rs.getInt(1));
                    m.put("curso", rs.getString(2));
                    l.add(m);
                }
                return l;
            }
        }
    }
}
