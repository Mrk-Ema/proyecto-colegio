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

public class EncargadoDAO {

    public int crear(String nombres, String apellidos, String parentesco,
            String telefono, String dpi, int idEstudiante) throws SQLException {
        String sql = "INSERT INTO encargado (nombres, apellidos, parentesco, telefono, dpi, id_estudiante) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombres);
            ps.setString(2, apellidos);
            ps.setString(3, parentesco);
            ps.setString(4, telefono);
            ps.setString(5, dpi);
            ps.setInt(6, idEstudiante);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Map<String, Object>> listarPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT id_encargado, nombres, apellidos, parentesco, telefono, dpi "
                + "FROM encargado WHERE id_estudiante = ? ORDER BY id_encargado";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idEncargado", rs.getInt("id_encargado"));
                    m.put("nombres", rs.getString("nombres"));
                    m.put("apellidos", rs.getString("apellidos"));
                    m.put("parentesco", rs.getString("parentesco"));
                    m.put("telefono", rs.getString("telefono"));
                    m.put("dpi", rs.getString("dpi"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public Map<String, Object> buscarPorId(int idEncargado) throws SQLException {
        String sql = "SELECT id_encargado, nombres, apellidos, parentesco, telefono, dpi, id_estudiante "
                + "FROM encargado WHERE id_encargado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEncargado);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idEncargado", rs.getInt("id_encargado"));
                m.put("nombres", rs.getString("nombres"));
                m.put("apellidos", rs.getString("apellidos"));
                m.put("parentesco", rs.getString("parentesco"));
                m.put("telefono", rs.getString("telefono"));
                m.put("dpi", rs.getString("dpi"));
                m.put("idEstudiante", rs.getInt("id_estudiante"));
                return m;
            }
        }
    }

    public boolean existeDpi(String dpi) throws SQLException {
        String sql = "SELECT COUNT(*) FROM encargado WHERE dpi = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dpi);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeOtroConDpi(int idEncargado, String dpi) throws SQLException {
        String sql = "SELECT COUNT(*) FROM encargado WHERE dpi = ? AND id_encargado <> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dpi);
            ps.setInt(2, idEncargado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void modificar(int idEncargado, String nombres, String apellidos,
            String parentesco, String telefono, String dpi) throws SQLException {
        String sql = "UPDATE encargado SET nombres = ?, apellidos = ?, parentesco = ?, "
                + "telefono = ?, dpi = ? WHERE id_encargado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombres);
            ps.setString(2, apellidos);
            ps.setString(3, parentesco);
            ps.setString(4, telefono);
            ps.setString(5, dpi);
            ps.setInt(6, idEncargado);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idEncargado) throws SQLException {
        String sql = "DELETE FROM encargado WHERE id_encargado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEncargado);
            ps.executeUpdate();
        }
    }
}
