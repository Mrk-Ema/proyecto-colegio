package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsistenciaDAO {

    public int contar(int idAsignacion, String estado, String estudiante, String desde, String hasta)
            throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM asistencia a "
                + "JOIN inscripcion i ON i.id_inscripcion = a.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "WHERE a.id_asignacion = ?");
        List<Object> p = new ArrayList<>();
        p.add(idAsignacion);
        whereFiltros(sb, p, estado, estudiante, desde, hasta);
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

    public List<Map<String, Object>> listar(int idAsignacion, String estado, String estudiante,
            String desde, String hasta, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT a.id_asistencia, a.fecha, a.estado, "
                + "i.id_inscripcion, e.id_estudiante, e.carnet, e.nombres, e.apellidos "
                + "FROM asistencia a "
                + "JOIN inscripcion i ON i.id_inscripcion = a.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "WHERE a.id_asignacion = ?");
        List<Object> p = new ArrayList<>();
        p.add(idAsignacion);
        whereFiltros(sb, p, estado, estudiante, desde, hasta);
        sb.append(" ORDER BY a.fecha DESC, e.apellidos, e.nombres LIMIT ? OFFSET ?");
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
                    m.put("idAsistencia", rs.getInt("id_asistencia"));
                    m.put("fecha", rs.getDate("fecha").toLocalDate().toString());
                    m.put("estado", rs.getString("estado"));
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

    private void whereFiltros(StringBuilder sb, List<Object> p, String estado, String estudiante,
            String desde, String hasta) {
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND a.estado = ?");
            p.add(estado);
        }
        if (estudiante != null && !estudiante.isBlank()) {
            sb.append(" AND (e.nombres LIKE ? OR e.apellidos LIKE ? OR e.carnet LIKE ?)");
            String like = "%" + estudiante.trim() + "%";
            p.add(like);
            p.add(like);
            p.add(like);
        }
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND a.fecha >= ?");
            p.add(Date.valueOf(desde.trim()));
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND a.fecha <= ?");
            p.add(Date.valueOf(hasta.trim()));
        }
    }

    public void crear(int idAsignacion, int idInscripcion, LocalDate fecha, String estado)
            throws SQLException {
        String sql = "INSERT INTO asistencia (fecha, estado, id_asignacion, id_inscripcion) "
                + "VALUES (?, ?, ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ps.setString(2, estado);
            ps.setInt(3, idAsignacion);
            ps.setInt(4, idInscripcion);
            ps.executeUpdate();
        }
    }

    public Map<String, Object> buscarPorId(int idAsistencia) throws SQLException {
        String sql = "SELECT a.id_asistencia, a.fecha, a.estado, a.id_asignacion, a.id_inscripcion, "
                + "i.id_año_lectivo, i.id_seccion, e.id_estudiante, e.carnet, e.nombres, e.apellidos "
                + "FROM asistencia a "
                + "JOIN inscripcion i ON i.id_inscripcion = a.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "WHERE a.id_asistencia = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsistencia);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idAsistencia", rs.getInt("id_asistencia"));
                m.put("fecha", rs.getDate("fecha").toLocalDate().toString());
                m.put("estado", rs.getString("estado"));
                m.put("idAsignacion", rs.getInt("id_asignacion"));
                m.put("idInscripcion", rs.getInt("id_inscripcion"));
                m.put("anio", rs.getInt("id_año_lectivo"));
                m.put("idSeccion", rs.getInt("id_seccion"));
                m.put("idEstudiante", rs.getInt("id_estudiante"));
                m.put("carnet", rs.getString("carnet"));
                m.put("nombres", rs.getString("nombres"));
                m.put("apellidos", rs.getString("apellidos"));
                return m;
            }
        }
    }

    public void actualizarEstado(int idAsistencia, String estado) throws SQLException {
        String sql = "UPDATE asistencia SET estado = ? WHERE id_asistencia = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idAsistencia);
            ps.executeUpdate();
        }
    }

    /** Verifica si ya hay asistencia para una fecha/curso/sección. */
    public boolean existeParaFecha(int idAsignacion, LocalDate fecha) throws SQLException {
        String sql = "SELECT COUNT(*) FROM asistencia WHERE id_asignacion = ? AND fecha = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsignacion);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}