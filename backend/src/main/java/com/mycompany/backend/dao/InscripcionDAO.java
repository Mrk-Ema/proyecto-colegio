package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
