package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dto.Estudiante;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstudianteDAO {

    public Estudiante buscarPorUsuario(Integer idUsuario) throws SQLException {
        String sql = "SELECT id_estudiante, carnet, nombres, apellidos, fecha_nacimiento, "
                + "direccion, telefono, tipo_sangre, alergias, padecimientos, estado, id_usuario "
                + "FROM estudiante WHERE id_usuario = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        }
    }

    public void actualizarDatos(Estudiante e) throws SQLException {
        String sql = "UPDATE estudiante SET nombres = ?, apellidos = ?, telefono = ?, "
                + "direccion = ? WHERE id_estudiante = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, e.getNombres());
            ps.setString(2, e.getApellidos());
            ps.setString(3, e.getTelefono());
            ps.setString(4, e.getDireccion());
            ps.setInt(5, e.getIdEstudiante());
            ps.executeUpdate();
        }
    }

    public int crear(String nombres, String apellidos, LocalDate fechaNacimiento,
            String direccion, String telefono, String tipoSangre,
            String alergias, String padecimientos, int idUsuario) throws SQLException {
        String sql = "INSERT INTO estudiante (carnet, nombres, apellidos, fecha_nacimiento, "
                + "direccion, telefono, tipo_sangre, alergias, padecimientos, estado, id_usuario) "
                + "VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, 'Activo', ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombres);
            ps.setString(2, apellidos);
            ps.setDate(3, Date.valueOf(fechaNacimiento));
            ps.setString(4, direccion);
            ps.setString(5, telefono);
            ps.setString(6, tipoSangre);
            ps.setString(7, alergias);
            ps.setString(8, padecimientos);
            ps.setInt(9, idUsuario);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizarCarnet(int idEstudiante, String carnet) throws SQLException {
        String sql = "UPDATE estudiante SET carnet = ? WHERE id_estudiante = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, carnet);
            ps.setInt(2, idEstudiante);
            ps.executeUpdate();
        }
    }

    public Map<String, Object> buscarPorId(int idEstudiante) throws SQLException {
        String sql = "SELECT e.id_estudiante, e.carnet, e.nombres, e.apellidos, e.fecha_nacimiento, "
                + "e.direccion, e.telefono, e.tipo_sangre, e.alergias, e.padecimientos, "
                + "e.estado, e.id_usuario, u.correo "
                + "FROM estudiante e JOIN usuario u ON u.id_usuario = e.id_usuario "
                + "WHERE e.id_estudiante = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapearFila(rs);
            }
        }
    }

    public int contar(String nombre, String carne, String estado,
            Integer grado, Integer seccion) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT COUNT(DISTINCT e.id_estudiante) FROM estudiante e "
                + "LEFT JOIN inscripcion i ON i.id_estudiante = e.id_estudiante "
                + "AND i.id_año_lectivo = (SELECT id_año_lectivo FROM año_lectivo WHERE estado = 'Activo' LIMIT 1) "
                + "LEFT JOIN seccion s ON s.id_seccion = i.id_seccion WHERE 1=1");
        List<Object> p = new ArrayList<>();
        whereFiltros(sb, p, nombre, carne, estado, grado, seccion);
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

    public List<Map<String, Object>> listar(String nombre, String carne, String estado,
            Integer grado, Integer seccion, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT DISTINCT e.id_estudiante, e.carnet, e.nombres, e.apellidos, e.estado, "
                + "u.correo, g.nombre AS grado, s.nombre AS seccion "
                + "FROM estudiante e JOIN usuario u ON u.id_usuario = e.id_usuario "
                + "LEFT JOIN inscripcion i ON i.id_estudiante = e.id_estudiante "
                + "AND i.id_año_lectivo = (SELECT id_año_lectivo FROM año_lectivo WHERE estado = 'Activo' LIMIT 1) "
                + "LEFT JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "LEFT JOIN grado g ON g.id_grado = s.id_grado WHERE 1=1");
        List<Object> p = new ArrayList<>();
        whereFiltros(sb, p, nombre, carne, estado, grado, seccion);
        sb.append(" ORDER BY e.apellidos, e.nombres LIMIT ? OFFSET ?");
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
                    m.put("idEstudiante", rs.getInt("id_estudiante"));
                    m.put("carnet", rs.getString("carnet"));
                    m.put("nombres", rs.getString("nombres"));
                    m.put("apellidos", rs.getString("apellidos"));
                    m.put("estado", rs.getString("estado"));
                    m.put("correo", rs.getString("correo"));
                    m.put("grado", rs.getString("grado"));
                    m.put("seccion", rs.getString("seccion"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    private void whereFiltros(StringBuilder sb, List<Object> p,
            String nombre, String carne, String estado, Integer grado, Integer seccion) {
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND CONCAT(e.nombres, ' ', e.apellidos) LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (carne != null && !carne.isBlank()) {
            sb.append(" AND e.carnet LIKE ?");
            p.add("%" + carne.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND e.estado = ?");
            p.add(estado);
        }
        if (grado != null) {
            sb.append(" AND s.id_grado = ?");
            p.add(grado);
        }
        if (seccion != null) {
            sb.append(" AND i.id_seccion = ?");
            p.add(seccion);
        }
    }

    public void modificar(int idEstudiante, String nombres, String apellidos,
            LocalDate fechaNacimiento, String direccion, String telefono,
            String tipoSangre, String alergias, String padecimientos) throws SQLException {
        String sql = "UPDATE estudiante SET nombres = ?, apellidos = ?, fecha_nacimiento = ?, "
                + "direccion = ?, telefono = ?, tipo_sangre = ?, alergias = ?, padecimientos = ? "
                + "WHERE id_estudiante = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombres);
            ps.setString(2, apellidos);
            ps.setDate(3, Date.valueOf(fechaNacimiento));
            ps.setString(4, direccion);
            ps.setString(5, telefono);
            ps.setString(6, tipoSangre);
            ps.setString(7, alergias);
            ps.setString(8, padecimientos);
            ps.setInt(9, idEstudiante);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(int idEstudiante, String estado) throws SQLException {
        String sql = "UPDATE estudiante SET estado = ? WHERE id_estudiante = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idEstudiante);
            ps.executeUpdate();
        }
    }

    private Map<String, Object> mapearFila(ResultSet rs) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("idEstudiante", rs.getInt("id_estudiante"));
        m.put("carnet", rs.getString("carnet"));
        m.put("nombres", rs.getString("nombres"));
        m.put("apellidos", rs.getString("apellidos"));
        Date fn = rs.getDate("fecha_nacimiento");
        m.put("fechaNacimiento", fn != null ? fn.toLocalDate().toString() : null);
        m.put("direccion", rs.getString("direccion"));
        m.put("telefono", rs.getString("telefono"));
        m.put("tipoSangre", rs.getString("tipo_sangre"));
        m.put("alergias", rs.getString("alergias"));
        m.put("padecimientos", rs.getString("padecimientos"));
        m.put("estado", rs.getString("estado"));
        m.put("idUsuario", rs.getInt("id_usuario"));
        m.put("correo", rs.getString("correo"));
        return m;
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        Estudiante e = new Estudiante();
        e.setIdEstudiante(rs.getInt("id_estudiante"));
        e.setCarnet(rs.getString("carnet"));
        e.setNombres(rs.getString("nombres"));
        e.setApellidos(rs.getString("apellidos"));
        Date fn = rs.getDate("fecha_nacimiento");
        e.setFechaNacimiento(fn != null ? fn.toLocalDate() : null);
        e.setDireccion(rs.getString("direccion"));
        e.setTelefono(rs.getString("telefono"));
        e.setTipoSangre(rs.getString("tipo_sangre"));
        e.setAlergias(rs.getString("alergias"));
        e.setPadecimientos(rs.getString("padecimientos"));
        e.setEstado(rs.getString("estado"));
        int idU = rs.getInt("id_usuario");
        e.setIdUsuario(rs.wasNull() ? null : idU);
        return e;
    }
}
