package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarreraDAO {

    public int crear(String nombre, String descripcion, Integer duracion) throws SQLException {
        String sql = "INSERT INTO carrera (nombre, descripcion, `duracion_años`, estado) VALUES (?, ?, ?, 'Activo')";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            if (duracion == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, duracion);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean existePorNombre(String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM carrera WHERE nombre = ?";
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
        String sql = "SELECT COUNT(*) FROM carrera WHERE nombre = ? AND id_carrera <> ?";
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
        String sql = "SELECT id_carrera, nombre, descripcion, `duracion_años` AS duracion, estado FROM carrera WHERE id_carrera = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idCarrera", rs.getInt("id_carrera"));
                m.put("nombre", rs.getString("nombre"));
                m.put("descripcion", rs.getString("descripcion"));
                int d = rs.getInt("duracion");
                m.put("duracionAnios", rs.wasNull() ? null : d);
                m.put("estado", rs.getString("estado"));
                return m;
            }
        }
    }

    public void modificar(int id, String nombre, String descripcion, Integer duracion) throws SQLException {
        String sql = "UPDATE carrera SET nombre = ?, descripcion = ?, `duracion_años` = ? WHERE id_carrera = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            if (duracion == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, duracion);
            }
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE carrera SET estado = ? WHERE id_carrera = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public int contar(String nombre, String estado) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM carrera WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND nombre LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND estado = ?");
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

    public List<Map<String, Object>> listar(String nombre, String estado, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT id_carrera, nombre, descripcion, `duracion_años` AS duracion, estado FROM carrera WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND nombre LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND estado = ?");
            p.add(estado);
        }
        sb.append(" ORDER BY nombre LIMIT ? OFFSET ?");
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
                    m.put("idCarrera", rs.getInt("id_carrera"));
                    m.put("nombre", rs.getString("nombre"));
                    m.put("descripcion", rs.getString("descripcion"));
                    int d = rs.getInt("duracion");
                    m.put("duracionAnios", rs.wasNull() ? null : d);
                    m.put("estado", rs.getString("estado"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public boolean tieneInscritosEnActivo(int idCarrera) throws SQLException {
        String sql = "SELECT COUNT(*) FROM grado g "
                + "JOIN estructura_año e ON e.id_grado = g.id_grado "
                + "JOIN año_lectivo a ON a.id_año_lectivo = e.id_año_lectivo AND a.estado = 'Activo' "
                + "JOIN seccion s ON s.id_grado = g.id_grado "
                + "JOIN inscripcion i ON i.id_seccion = s.id_seccion AND i.id_año_lectivo = a.id_año_lectivo "
                + "WHERE g.id_carrera = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCarrera);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
