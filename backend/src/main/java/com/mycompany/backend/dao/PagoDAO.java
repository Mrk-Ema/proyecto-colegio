package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PagoDAO {

    /**
     * Inserta el pago con un numero provisional y lo deja listo para que el
     * servicio le asigne el numero correlativo definitivo.
     */
    public int crear(int idInscripcion, Integer idActividad, String concepto, String mes,
            BigDecimal monto, int idUsuarioRegistra) throws SQLException {
        String sql = "INSERT INTO pago (numero_correlativo, concepto, mes, monto, fecha_pago, estado, "
                + "id_inscripcion, id_actividad, id_usuario_registra) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'Registrado', ?, ?, ?)";
        String provisional = "TMP-" + UUID.randomUUID().toString().substring(0, 18);
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, provisional);
            ps.setString(2, concepto);
            if (mes == null) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, mes);
            }
            ps.setBigDecimal(4, monto);
            ps.setInt(5, idInscripcion);
            if (idActividad == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, idActividad);
            }
            ps.setInt(7, idUsuarioRegistra);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void asignarCorrelativo(int idPago, String correlativo) throws SQLException {
        String sql = "UPDATE pago SET numero_correlativo = ? WHERE id_pago = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correlativo);
            ps.setInt(2, idPago);
            ps.executeUpdate();
        }
    }

    public Map<String, Object> buscarPorId(int idPago) throws SQLException {
        String sql = "SELECT p.id_pago, p.numero_correlativo, p.concepto, p.mes, p.monto, "
                + "p.fecha_pago, p.estado, p.motivo_anulacion, p.fecha_anulacion, "
                + "p.id_inscripcion, p.id_actividad, p.id_usuario_registra, p.id_usuario_anula, "
                + "e.id_estudiante, e.carnet, e.nombres, e.apellidos, i.id_año_lectivo AS anio, "
                + "g.nombre AS grado, s.nombre AS seccion, "
                + "ur.correo AS registra_correo, ua.correo AS anula_correo "
                + "FROM pago p "
                + "JOIN inscripcion i ON i.id_inscripcion = p.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN usuario ur ON ur.id_usuario = p.id_usuario_registra "
                + "LEFT JOIN usuario ua ON ua.id_usuario = p.id_usuario_anula "
                + "WHERE p.id_pago = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapear(rs);
            }
        }
    }

    /**
     * comprobacion de que el mes ya se haya pagado y asi no repetir pagar
     */
    public boolean existePagoDelMes(int idInscripcion, String mes) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pago WHERE id_inscripcion = ? AND mes = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idInscripcion);
            ps.setString(2, mes);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /**
     * ya se registró el pago de ese concepto para esa inscripción?
     */
    public boolean existePagoDeConcepto(int idInscripcion, String concepto) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pago WHERE id_inscripcion = ? AND concepto = ? "
                + "AND estado = 'Registrado'";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idInscripcion);
            ps.setString(2, concepto);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Suma los pagos Registrados de inscripción y mensualidad de una inscripción.
     * Las actividades y los pagos Anulados no se cuentan.
     */
    public BigDecimal sumarPagosRegistrados(int idInscripcion) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto), 0) AS total FROM pago "
                + "WHERE id_inscripcion = ? AND estado = 'Registrado' "
                + "AND concepto IN ('Inscripcion', 'Mensualidad')";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idInscripcion);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("total");
            }
        }
    }

    /**
     * Ya se pagó esa actividad para esa inscripción?
     */
    public boolean existePagoDeActividad(int idInscripcion, int idActividad) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pago WHERE id_inscripcion = ? AND id_actividad = ? "
                + "AND estado = 'Registrado'";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idInscripcion);
            ps.setInt(2, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int contar(String carnet, Integer idEstudiante, String concepto, String estado,
            String desde, String hasta) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM pago p "
                + "JOIN inscripcion i ON i.id_inscripcion = p.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante WHERE 1=1");
        List<Object> p = new ArrayList<>();
        where(sb, p, carnet, idEstudiante, concepto, estado, desde, hasta);
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

    public List<Map<String, Object>> listar(String carnet, Integer idEstudiante, String concepto,
            String estado, String desde, String hasta, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT p.id_pago, p.numero_correlativo, p.concepto, p.mes, "
                + "p.monto, p.fecha_pago, p.estado, p.motivo_anulacion, p.fecha_anulacion, "
                + "p.id_inscripcion, p.id_actividad, p.id_usuario_registra, p.id_usuario_anula, "
                + "e.id_estudiante, e.carnet, e.nombres, e.apellidos, i.id_año_lectivo AS anio, "
                + "g.nombre AS grado, s.nombre AS seccion, "
                + "ur.correo AS registra_correo, ua.correo AS anula_correo "
                + "FROM pago p "
                + "JOIN inscripcion i ON i.id_inscripcion = p.id_inscripcion "
                + "JOIN estudiante e ON e.id_estudiante = i.id_estudiante "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado "
                + "JOIN usuario ur ON ur.id_usuario = p.id_usuario_registra "
                + "LEFT JOIN usuario ua ON ua.id_usuario = p.id_usuario_anula WHERE 1=1");
        List<Object> p = new ArrayList<>();
        where(sb, p, carnet, idEstudiante, concepto, estado, desde, hasta);
        sb.append(" ORDER BY p.fecha_pago DESC, p.id_pago DESC LIMIT ? OFFSET ?");
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

    public void anular(int idPago, String motivo, int idUsuarioAnula) throws SQLException {
        String sql = "UPDATE pago SET estado = 'Anulado', motivo_anulacion = ?, "
                + "fecha_anulacion = CURRENT_TIMESTAMP, id_usuario_anula = ? WHERE id_pago = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, motivo);
            ps.setInt(2, idUsuarioAnula);
            ps.setInt(3, idPago);
            ps.executeUpdate();
        }
    }

    private void where(StringBuilder sb, List<Object> p, String carnet, Integer idEstudiante,
            String concepto, String estado, String desde, String hasta) {
        if (carnet != null && !carnet.isBlank()) {
            sb.append(" AND e.carnet = ?");
            p.add(carnet.trim());
        }
        if (idEstudiante != null) {
            sb.append(" AND e.id_estudiante = ?");
            p.add(idEstudiante);
        }
        if (concepto != null && !concepto.isBlank()) {
            sb.append(" AND p.concepto = ?");
            p.add(concepto);
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND p.estado = ?");
            p.add(estado);
        }
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND DATE(p.fecha_pago) >= ?");
            p.add(desde.trim());
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND DATE(p.fecha_pago) <= ?");
            p.add(hasta.trim());
        }
    }

    private Map<String, Object> mapear(ResultSet rs) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("idPago", rs.getInt("id_pago"));
        m.put("numeroCorrelativo", rs.getString("numero_correlativo"));
        m.put("concepto", rs.getString("concepto"));
        m.put("mes", rs.getString("mes"));
        m.put("monto", rs.getBigDecimal("monto"));
        Timestamp fp = rs.getTimestamp("fecha_pago");
        m.put("fechaPago", fp != null ? fp.toLocalDateTime().toString().replace('T', ' ') : null);
        m.put("estado", rs.getString("estado"));
        m.put("motivoAnulacion", rs.getString("motivo_anulacion"));
        Timestamp fa = rs.getTimestamp("fecha_anulacion");
        m.put("fechaAnulacion", fa != null ? fa.toLocalDateTime().toString().replace('T', ' ') : null);
        m.put("idInscripcion", rs.getInt("id_inscripcion"));
        int ia = rs.getInt("id_actividad");
        m.put("idActividad", rs.wasNull() ? null : ia);
        m.put("idUsuarioRegistra", rs.getInt("id_usuario_registra"));
        int iu = rs.getInt("id_usuario_anula");
        m.put("idUsuarioAnula", rs.wasNull() ? null : iu);
        m.put("idEstudiante", rs.getInt("id_estudiante"));
        m.put("carnet", rs.getString("carnet"));
        m.put("nombres", rs.getString("nombres"));
        m.put("apellidos", rs.getString("apellidos"));
        m.put("anio", rs.getInt("anio"));
        m.put("grado", rs.getString("grado"));
        m.put("seccion", rs.getString("seccion"));
        m.put("registraCorreo", rs.getString("registra_correo"));
        m.put("anulaCorreo", rs.getString("anula_correo"));
        return m;
    }
}
