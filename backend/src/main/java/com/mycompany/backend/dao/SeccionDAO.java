package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeccionDAO {

    public int crear(String nombre, int idGrado) throws SQLException {
        String sql = "INSERT INTO seccion (nombre, estado, id_grado) VALUES (?, 'Activo', ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setInt(2, idGrado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT s.id_seccion, s.nombre, s.estado, s.id_grado, g.nombre AS grado, "
                + "g.id_nivel, n.nombre AS nivel, g.id_carrera, c.nombre AS carrera "
                + "FROM seccion s JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera "
                + "WHERE s.id_seccion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapear(rs);
            }
        }
    }

    public boolean existePorNombreEnGrado(String nombre, int idGrado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seccion WHERE nombre = ? AND id_grado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idGrado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeOtroConNombre(int id, String nombre, int idGrado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seccion WHERE nombre = ? AND id_grado = ? AND id_seccion <> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idGrado);
            ps.setInt(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void modificar(int id, String nombre) throws SQLException {
        String sql = "UPDATE seccion SET nombre = ? WHERE id_seccion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE seccion SET estado = ? WHERE id_seccion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public int contar(Integer nivel, Integer carrera, Integer grado, String nombre, String estado) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT COUNT(*) FROM seccion s JOIN grado g ON g.id_grado = s.id_grado WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (nivel != null) {
            sb.append(" AND g.id_nivel = ?");
            p.add(nivel);
        }
        if (carrera != null) {
            sb.append(" AND g.id_carrera = ?");
            p.add(carrera);
        }
        if (grado != null) {
            sb.append(" AND s.id_grado = ?");
            p.add(grado);
        }
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND s.nombre LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND s.estado = ?");
            p.add(estado);
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

    public List<Map<String, Object>> listar(Integer nivel, Integer carrera, Integer grado, String nombre, String estado, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT s.id_seccion, s.nombre, s.estado, s.id_grado, g.nombre AS grado, "
                + "g.id_nivel, n.nombre AS nivel, g.id_carrera, c.nombre AS carrera "
                + "FROM seccion s JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (nivel != null) {
            sb.append(" AND g.id_nivel = ?");
            p.add(nivel);
        }
        if (carrera != null) {
            sb.append(" AND g.id_carrera = ?");
            p.add(carrera);
        }
        if (grado != null) {
            sb.append(" AND s.id_grado = ?");
            p.add(grado);
        }
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND s.nombre LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND s.estado = ?");
            p.add(estado);
        }
        sb.append(" ORDER BY n.id_nivel, g.nombre, s.nombre LIMIT ? OFFSET ?");
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
                    l.add(mapear(rs));
                }
                return l;
            }
        }
    }

    public List<Map<String, Object>> listarConAsignacionPorAnio(int anio) throws SQLException {
        String sql = "SELECT DISTINCT s.id_seccion, s.nombre, g.id_grado, g.nombre AS grado "
                + "FROM asignacion_maestro am JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "WHERE am.id_año_lectivo = ? ORDER BY g.nombre, s.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idSeccion", rs.getInt("id_seccion"));
                    m.put("nombre", rs.getString("nombre"));
                    m.put("idGrado", rs.getInt("id_grado"));
                    m.put("grado", rs.getString("grado"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    private Map<String, Object> mapear(ResultSet rs) throws SQLException {        Map<String, Object> m = new HashMap<>();
        m.put("idSeccion", rs.getInt("id_seccion"));
        m.put("nombre", rs.getString("nombre"));
        m.put("estado", rs.getString("estado"));
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
