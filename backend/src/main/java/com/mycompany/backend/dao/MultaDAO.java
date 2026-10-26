package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultaDAO {

    public int contar(Integer idPrestatario, String tipoPrestatario, String estado, String desde, String hasta)
            throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM multa m "
                + "JOIN prestamo p ON p.id_prestamo = m.id_prestamo WHERE 1=1");
        List<Object> params = new ArrayList<>();
        whereFiltros(sb, params, idPrestatario, tipoPrestatario, estado, desde, hasta);
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Map<String, Object>> listar(Integer idPrestatario, String tipoPrestatario, String estado,
            String desde, String hasta, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT m.id_multa, m.dias_atraso, m.monto, m.estado, "
                + "m.fecha_pago, m.numero_correlativo, m.id_prestamo, "
                + "p.fecha_prestamo, p.fecha_esperada, p.fecha_devolucion, "
                + "p.id_libro, l.titulo AS libro, "
                + "p.id_estudiante, p.id_empleado, "
                + "e.nombres AS est_nombres, e.apellidos AS est_apellidos, e.carnet AS est_carnet, "
                + "em.nombres AS emp_nombres, em.apellidos AS emp_apellidos "
                + "FROM multa m JOIN prestamo p ON p.id_prestamo = m.id_prestamo "
                + "JOIN libro l ON l.id_libro = p.id_libro "
                + "LEFT JOIN estudiante e ON e.id_estudiante = p.id_estudiante "
                + "LEFT JOIN empleado em ON em.id_empleado = p.id_empleado WHERE 1=1");
        List<Object> params = new ArrayList<>();
        whereFiltros(sb, params, idPrestatario, tipoPrestatario, estado, desde, hasta);
        sb.append(" ORDER BY m.id_multa DESC LIMIT ? OFFSET ?");
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            int i = 1;
            for (; i <= params.size(); i++) {
                ps.setObject(i, params.get(i - 1));
            }
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> lista = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idMulta", rs.getInt("id_multa"));
                    m.put("diasAtraso", rs.getInt("dias_atraso"));
                    m.put("monto", rs.getBigDecimal("monto"));
                    m.put("estado", rs.getString("estado"));
                    Timestamp fp = rs.getTimestamp("fecha_pago");
                    m.put("fechaPago", fp != null ? fp.toLocalDateTime().toString().replace('T', ' ') : null);
                    m.put("numeroCorrelativo", rs.getString("numero_correlativo"));
                    m.put("idPrestamo", rs.getInt("id_prestamo"));
                    m.put("fechaPrestamo", rs.getDate("fecha_prestamo").toLocalDate().toString());
                    m.put("fechaEsperada", rs.getDate("fecha_esperada").toLocalDate().toString());
                    Date dev = rs.getDate("fecha_devolucion");
                    m.put("fechaDevolucion", dev != null ? dev.toLocalDate().toString() : null);
                    m.put("idLibro", rs.getInt("id_libro"));
                    m.put("libro", rs.getString("libro"));
                    int idEst = rs.getInt("id_estudiante");
                    m.put("idEstudiante", rs.wasNull() ? null : idEst);
                    int idEmp = rs.getInt("id_empleado");
                    m.put("idEmpleado", rs.wasNull() ? null : idEmp);
                    if (idEst > 0) {
                        m.put("prestatario", rs.getString("est_nombres") + " " + rs.getString("est_apellidos"));
                        m.put("tipoPrestatario", "Estudiante");
                        m.put("carnet", rs.getString("est_carnet"));
                    } else {
                        m.put("prestatario", rs.getString("emp_nombres") + " " + rs.getString("emp_apellidos"));
                        m.put("tipoPrestatario", "Maestro");
                        m.put("carnet", null);
                    }
                    lista.add(m);
                }
                return lista;
            }
        }
    }

    private void whereFiltros(StringBuilder sb, List<Object> params, Integer idPrestatario,
            String tipoPrestatario, String estado, String desde, String hasta) {
        if (idPrestatario != null) {
            if ("estudiante".equalsIgnoreCase(tipoPrestatario)) {
                sb.append(" AND p.id_estudiante = ?");
            } else if ("maestro".equalsIgnoreCase(tipoPrestatario)) {
                sb.append(" AND p.id_empleado = ?");
            } else {
                sb.append(" AND (p.id_estudiante = ? OR p.id_empleado = ?)");
                params.add(idPrestatario);
            }
            params.add(idPrestatario);
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND m.estado = ?");
            params.add(estado);
        }
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND p.fecha_esperada >= ?");
            params.add(Date.valueOf(desde.trim()));
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND p.fecha_esperada <= ?");
            params.add(Date.valueOf(hasta.trim()));
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT m.id_multa, m.dias_atraso, m.monto, m.estado, m.fecha_pago, "
                + "m.numero_correlativo, m.id_prestamo, m.id_usuario_cobra, "
                + "p.id_libro, l.titulo AS libro, p.id_estudiante, p.id_empleado "
                + "FROM multa m JOIN prestamo p ON p.id_prestamo = m.id_prestamo "
                + "JOIN libro l ON l.id_libro = p.id_libro WHERE m.id_multa = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idMulta", rs.getInt("id_multa"));
                m.put("diasAtraso", rs.getInt("dias_atraso"));
                m.put("monto", rs.getBigDecimal("monto"));
                m.put("estado", rs.getString("estado"));
                Timestamp fp = rs.getTimestamp("fecha_pago");
                m.put("fechaPago", fp != null ? fp.toLocalDateTime().toString().replace('T', ' ') : null);
                m.put("numeroCorrelativo", rs.getString("numero_correlativo"));
                m.put("idPrestamo", rs.getInt("id_prestamo"));
                m.put("idLibro", rs.getInt("id_libro"));
                m.put("libro", rs.getString("libro"));
                int idEst = rs.getInt("id_estudiante");
                m.put("idEstudiante", rs.wasNull() ? null : idEst);
                int idEmp = rs.getInt("id_empleado");
                m.put("idEmpleado", rs.wasNull() ? null : idEmp);
                int idU = rs.getInt("id_usuario_cobra");
                m.put("idUsuarioCobra", rs.wasNull() ? null : idU);
                return m;
            }
        }
    }

    public int crear(int idPrestamo, int diasAtraso, BigDecimal monto) throws SQLException {
        String sql = "INSERT INTO multa (dias_atraso, monto, estado, id_prestamo) VALUES (?, ?, 'Pendiente', ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, diasAtraso);
            ps.setBigDecimal(2, monto);
            ps.setInt(3, idPrestamo);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void registrarPago(int idMulta, String correlativo, int idUsuarioCobra) throws SQLException {
        String sql = "UPDATE multa SET estado = 'Pagada', fecha_pago = CURRENT_TIMESTAMP, "
                + "numero_correlativo = ?, id_usuario_cobra = ? WHERE id_multa = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correlativo);
            ps.setInt(2, idUsuarioCobra);
            ps.setInt(3, idMulta);
            ps.executeUpdate();
        }
    }
}