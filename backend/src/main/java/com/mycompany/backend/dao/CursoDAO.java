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

public class CursoDAO {

    public int crear(String nombre) throws SQLException {
        String sql = "INSERT INTO curso (nombre, estado) VALUES (?, 'Activo')";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean existePorNombre(String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM curso WHERE nombre = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeOtroConNombre(int id, String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM curso WHERE nombre = ? AND id_curso <> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_curso, nombre, estado FROM curso WHERE id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idCurso", rs.getInt("id_curso"));
                m.put("nombre", rs.getString("nombre"));
                m.put("estado", rs.getString("estado"));
                return m;
            }
        }
    }

    public void modificar(int id, String nombre) throws SQLException {
        String sql = "UPDATE curso SET nombre = ? WHERE id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE curso SET estado = ? WHERE id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public int contar(String nombre, String estado, Integer grado) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT COUNT(*) FROM curso c "
                + "LEFT JOIN curriculo cu ON cu.id_curso = c.id_curso "
                + "AND EXISTS (SELECT 1 FROM estructura_año e2 WHERE e2.id_estructura = cu.id_estructura AND e2.id_año_lectivo IS NULL) "
                + "LEFT JOIN estructura_año e ON e.id_estructura = cu.id_estructura WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND c.nombre LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND c.estado = ?");
            p.add(estado);
        }
        if (grado != null) {
            sb.append(" AND e.id_grado = ?");
            p.add(grado);
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

    public List<Map<String, Object>> listar(String nombre, String estado, Integer grado, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT c.id_curso, c.nombre, c.estado, e.id_grado AS idGrado, g.nombre AS grado "
                + "FROM curso c LEFT JOIN curriculo cu ON cu.id_curso = c.id_curso "
                + "AND EXISTS (SELECT 1 FROM estructura_año e2 WHERE e2.id_estructura = cu.id_estructura AND e2.id_año_lectivo IS NULL) "
                + "LEFT JOIN estructura_año e ON e.id_estructura = cu.id_estructura "
                + "LEFT JOIN grado g ON g.id_grado = e.id_grado WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND c.nombre LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND c.estado = ?");
            p.add(estado);
        }
        if (grado != null) {
            sb.append(" AND e.id_grado = ?");
            p.add(grado);
        }
        sb.append(" ORDER BY c.nombre, g.nombre LIMIT ? OFFSET ?");
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
                    m.put("idCurso", rs.getInt("id_curso"));
                    m.put("nombre", rs.getString("nombre"));
                    m.put("estado", rs.getString("estado"));
                    int idG = rs.getInt("idGrado");
                    m.put("idGrado", rs.wasNull() ? null : idG);
                    m.put("grado", rs.getString("grado"));
                    l.add(m);
                }
                return l;
            }
        }
    }
}
