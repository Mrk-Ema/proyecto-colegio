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

public class CuotaDAO {

    /**
     * Grados de la estructura del año con los montos ya configurados.
     * Los grados que todavía no tienen cuota llegan con los montos en null.
     */
    public List<Map<String, Object>> listarGradosConCuota(int anio) throws SQLException {
        String sql = "SELECT g.id_grado, g.nombre AS grado, n.nombre AS nivel, c.nombre AS carrera, "
                + "q.id_cuota, q.monto_inscripcion, q.monto_mensualidad "
                + "FROM estructura_año e "
                + "JOIN grado g ON g.id_grado = e.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera "
                + "LEFT JOIN cuota q ON q.id_grado = g.id_grado AND q.id_año_lectivo = e.id_año_lectivo "
                + "WHERE e.id_año_lectivo = ? ORDER BY n.id_nivel, g.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> lista = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idGrado", rs.getInt("id_grado"));
                    m.put("grado", rs.getString("grado"));
                    m.put("nivel", rs.getString("nivel"));
                    m.put("carrera", rs.getString("carrera"));
                    int id = rs.getInt("id_cuota");
                    m.put("idCuota", rs.wasNull() ? null : id);
                    m.put("montoInscripcion", rs.getBigDecimal("monto_inscripcion"));
                    m.put("montoMensualidad", rs.getBigDecimal("monto_mensualidad"));
                    lista.add(m);
                }
                return lista;
            }
        }
    }

    public Integer buscarIdCuota(int idGrado, int anio) throws SQLException {
        String sql = "SELECT id_cuota FROM cuota WHERE id_grado = ? AND id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGrado);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rs.getInt(1);
            }
        }
    }

    public int crear(int anio, int idGrado, BigDecimal inscripcion, BigDecimal mensualidad)
            throws SQLException {
        String sql = "INSERT INTO cuota (monto_inscripcion, monto_mensualidad, id_año_lectivo, id_grado) "
                + "VALUES (?, ?, ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, inscripcion);
            ps.setBigDecimal(2, mensualidad);
            ps.setInt(3, anio);
            ps.setInt(4, idGrado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(int idCuota, BigDecimal inscripcion, BigDecimal mensualidad)
            throws SQLException {
        String sql = "UPDATE cuota SET monto_inscripcion = ?, monto_mensualidad = ? WHERE id_cuota = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, inscripcion);
            ps.setBigDecimal(2, mensualidad);
            ps.setInt(3, idCuota);
            ps.executeUpdate();
        }
    }
}