package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActividadEvaluableDAO {

    public List<Map<String, Object>> listarPorAsignacion(int idAsignacion) throws SQLException {
        String sql = "SELECT ae.id_actividad_evaluable, ae.nombre, ae.puntos, "
                + "(SELECT COUNT(*) FROM nota n WHERE n.id_actividad_evaluable = ae.id_actividad_evaluable) AS tiene_notas "
                + "FROM actividad_evaluable ae WHERE ae.id_asignacion = ? ORDER BY ae.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idActividadEvaluable", rs.getInt("id_actividad_evaluable"));
                    m.put("nombre", rs.getString("nombre"));
                    m.put("puntos", rs.getBigDecimal("puntos"));
                    m.put("tieneNotas", rs.getInt("tiene_notas") > 0);
                    l.add(m);
                }
                return l;
            }
        }
    }

    public int crear(int idAsignacion, String nombre, BigDecimal puntos) throws SQLException {
        String sql = "INSERT INTO actividad_evaluable (nombre, puntos, id_asignacion) VALUES (?, ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setBigDecimal(2, puntos);
            ps.setInt(3, idAsignacion);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(int id, String nombre, BigDecimal puntos) throws SQLException {
        String sql = "UPDATE actividad_evaluable SET nombre = ?, puntos = ? WHERE id_actividad_evaluable = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setBigDecimal(2, puntos);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM actividad_evaluable WHERE id_actividad_evaluable = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void eliminarPorAsignacion(int idAsignacion) throws SQLException {
        String sql = "DELETE FROM actividad_evaluable WHERE id_asignacion = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsignacion);
            ps.executeUpdate();
        }
    }

    public boolean tieneNotas(int idActividadEvaluable) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nota WHERE id_actividad_evaluable = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividadEvaluable);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int contarNotas(int idActividadEvaluable) throws SQLException {
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

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_actividad_evaluable, nombre, puntos, id_asignacion FROM actividad_evaluable WHERE id_actividad_evaluable = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idActividadEvaluable", rs.getInt("id_actividad_evaluable"));
                m.put("nombre", rs.getString("nombre"));
                m.put("puntos", rs.getBigDecimal("puntos"));
                m.put("idAsignacion", rs.getInt("id_asignacion"));
                return m;
            }
        }
    }
}