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

public class ActividadDAO {

    public int contar(String nombre, String estado) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM actividad_extracurricular WHERE 1=1");
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

    public List<Map<String, Object>> listar(String nombre, String estado, int limit, int offset)
            throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT id_actividad, nombre, descripcion, costo, alcance, estado "
                + "FROM actividad_extracurricular WHERE 1=1");
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
                    m.put("idActividad", rs.getInt("id_actividad"));
                    m.put("nombre", rs.getString("nombre"));
                    m.put("descripcion", rs.getString("descripcion"));
                    m.put("costo", rs.getBigDecimal("costo"));
                    m.put("alcance", rs.getString("alcance"));
                    m.put("estado", rs.getString("estado"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public Map<String, Object> buscarPorId(int idActividad) throws SQLException {
        String sql = "SELECT id_actividad, nombre, descripcion, costo, alcance, estado "
                + "FROM actividad_extracurricular WHERE id_actividad = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idActividad", rs.getInt("id_actividad"));
                m.put("nombre", rs.getString("nombre"));
                m.put("descripcion", rs.getString("descripcion"));
                m.put("costo", rs.getBigDecimal("costo"));
                m.put("alcance", rs.getString("alcance"));
                m.put("estado", rs.getString("estado"));
                return m;
            }
        }
    }

    /** La base de datos compara sin tildes ni mayúsculas, así que el nombre no se repite. */
    public boolean existeOtroConNombre(String nombre, int idActividad) throws SQLException {
        String sql = "SELECT COUNT(*) FROM actividad_extracurricular WHERE nombre = ? AND id_actividad <> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeConNombre(String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM actividad_extracurricular WHERE nombre = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int crear(String nombre, String descripcion, BigDecimal costo, String alcance)
            throws SQLException {
        String sql = "INSERT INTO actividad_extracurricular (nombre, descripcion, costo, alcance, estado) "
                + "VALUES (?, ?, ?, ?, 'Activo')";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            if (descripcion == null) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, descripcion);
            }
            ps.setBigDecimal(3, costo);
            ps.setString(4, alcance);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void modificar(int idActividad, String nombre, String descripcion, BigDecimal costo,
            String alcance) throws SQLException {
        String sql = "UPDATE actividad_extracurricular SET nombre = ?, descripcion = ?, costo = ?, "
                + "alcance = ? WHERE id_actividad = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            if (descripcion == null) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, descripcion);
            }
            ps.setBigDecimal(3, costo);
            ps.setString(4, alcance);
            ps.setInt(5, idActividad);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(int idActividad, String estado) throws SQLException {
        String sql = "UPDATE actividad_extracurricular SET estado = ? WHERE id_actividad = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idActividad);
            ps.executeUpdate();
        }
    }

    public List<Integer> listarGrados(int idActividad) throws SQLException {
        String sql = "SELECT id_grado FROM actividad_grado WHERE id_actividad = ? ORDER BY id_grado";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> l = new ArrayList<>();
                while (rs.next()) {
                    l.add(rs.getInt(1));
                }
                return l;
            }
        }
    }

    /** Grados del alcance con su nivel, para mostrarlos en el listado y en el formulario. */
    public List<Map<String, Object>> listarGradosConNombre(int idActividad) throws SQLException {
        String sql = "SELECT ag.id_grado, g.nombre AS grado, n.nombre AS nivel FROM actividad_grado ag "
                + "JOIN grado g ON g.id_grado = ag.id_grado "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "WHERE ag.id_actividad = ? ORDER BY n.id_nivel, g.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idGrado", rs.getInt("id_grado"));
                    m.put("grado", rs.getString("grado"));
                    m.put("nivel", rs.getString("nivel"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public void agregarGrados(int idActividad, List<Integer> grados) throws SQLException {
        if (grados.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO actividad_grado (id_actividad, id_grado) VALUES (?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Integer idGrado : grados) {
                ps.setInt(1, idActividad);
                ps.setInt(2, idGrado);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void quitarGrados(int idActividad, List<Integer> grados) throws SQLException {
        if (grados.isEmpty()) {
            return;
        }
        String sql = "DELETE FROM actividad_grado WHERE id_actividad = ? AND id_grado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Integer idGrado : grados) {
                ps.setInt(1, idActividad);
                ps.setInt(2, idGrado);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Un grado solo sale del alcance si ninguno de sus estudiantes tiene un pago
     * registrado de la actividad. Los pagos ya cobrados no se tocan.
     */
    public boolean tienePagosRegistradosEnGrado(int idActividad, int idGrado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pago p "
                + "JOIN inscripcion i ON i.id_inscripcion = p.id_inscripcion "
                + "JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "WHERE p.id_actividad = ? AND s.id_grado = ? AND p.estado = 'Registrado'";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            ps.setInt(2, idGrado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}