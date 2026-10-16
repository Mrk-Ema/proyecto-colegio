package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dto.Empleado;
import java.math.BigDecimal;
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

public class EmpleadoDAO {

    public Empleado buscarPorUsuario(Integer idUsuario) throws SQLException {
        String sql = "SELECT id_empleado, nombres, apellidos, dpi, fecha_nacimiento, "
                + "telefono, direccion, puesto, estado, id_usuario "
                + "FROM empleado WHERE id_usuario = ?";
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

    public void actualizarDatos(Empleado e) throws SQLException {
        String sql = "UPDATE empleado SET nombres = ?, apellidos = ?, telefono = ?, "
                + "direccion = ? WHERE id_empleado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, e.getNombres());
            ps.setString(2, e.getApellidos());
            ps.setString(3, e.getTelefono());
            ps.setString(4, e.getDireccion());
            ps.setInt(5, e.getIdEmpleado());
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> listarMaestrosActivos() throws SQLException {
        String sql = "SELECT id_empleado, nombres, apellidos, puesto FROM empleado "
                + "WHERE estado = 'Activo' AND LOWER(puesto) LIKE '%maestr%' ORDER BY nombres, apellidos";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> l = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("idEmpleado", rs.getInt(1));
                m.put("maestro", rs.getString(2) + " " + rs.getString(3));
                m.put("puesto", rs.getString(4));
                l.add(m);
            }
            return l;
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_empleado, nombres, apellidos, puesto, estado, id_usuario FROM empleado WHERE id_empleado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idEmpleado", rs.getInt(1));
                m.put("maestro", rs.getString(2) + " " + rs.getString(3));
                m.put("puesto", rs.getString(4));
                m.put("estado", rs.getString(5));
                int idU = rs.getInt(6);
                m.put("idUsuario", rs.wasNull() ? null : idU);
                return m;
            }
        }
    }

    public int crear(String nombres, String apellidos, String dpi, LocalDate fechaNacimiento,
            String telefono, String direccion, String puesto, LocalDate fechaContratacion,
            BigDecimal salario, Integer idUsuario) throws SQLException {
        String sql = "INSERT INTO empleado (nombres, apellidos, dpi, fecha_nacimiento, telefono, "
                + "direccion, puesto, fecha_contratacion, salario, estado, id_usuario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Activo', ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombres);
            ps.setString(2, apellidos);
            ps.setString(3, dpi);
            ps.setDate(4, Date.valueOf(fechaNacimiento));
            ps.setString(5, telefono);
            ps.setString(6, direccion);
            ps.setString(7, puesto);
            ps.setDate(8, Date.valueOf(fechaContratacion));
            ps.setBigDecimal(9, salario);
            if (idUsuario == null) {
                ps.setNull(10, java.sql.Types.INTEGER);
            } else {
                ps.setInt(10, idUsuario);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Map<String, Object> buscarDetalle(int idEmpleado) throws SQLException {
        String sql = "SELECT e.id_empleado, e.nombres, e.apellidos, e.dpi, e.fecha_nacimiento, "
                + "e.telefono, e.direccion, e.puesto, e.fecha_contratacion, e.salario, "
                + "e.estado, e.id_usuario, u.correo "
                + "FROM empleado e LEFT JOIN usuario u ON u.id_usuario = e.id_usuario "
                + "WHERE e.id_empleado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idEmpleado", rs.getInt("id_empleado"));
                m.put("nombres", rs.getString("nombres"));
                m.put("apellidos", rs.getString("apellidos"));
                m.put("dpi", rs.getString("dpi"));
                Date fn = rs.getDate("fecha_nacimiento");
                m.put("fechaNacimiento", fn != null ? fn.toLocalDate().toString() : null);
                m.put("telefono", rs.getString("telefono"));
                m.put("direccion", rs.getString("direccion"));
                m.put("puesto", rs.getString("puesto"));
                Date fc = rs.getDate("fecha_contratacion");
                m.put("fechaContratacion", fc != null ? fc.toLocalDate().toString() : null);
                m.put("salario", rs.getBigDecimal("salario"));
                m.put("estado", rs.getString("estado"));
                int idU = rs.getInt("id_usuario");
                m.put("idUsuario", rs.wasNull() ? null : idU);
                m.put("correo", rs.getString("correo"));
                return m;
            }
        }
    }

    public boolean existeDpi(String dpi) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleado WHERE dpi = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dpi);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeOtroConDpi(int idEmpleado, String dpi) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleado WHERE dpi = ? AND id_empleado <> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dpi);
            ps.setInt(2, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int contar(String nombre, String puesto, String estado, boolean excluirAdmin) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM empleado e WHERE 1=1");
        List<Object> p = new ArrayList<>();
        whereFiltros(sb, p, nombre, puesto, estado, excluirAdmin);
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

    public List<Map<String, Object>> listar(String nombre, String puesto, String estado,
            boolean excluirAdmin, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT e.id_empleado, e.nombres, e.apellidos, e.puesto, e.estado, u.correo "
                + "FROM empleado e LEFT JOIN usuario u ON u.id_usuario = e.id_usuario WHERE 1=1");
        List<Object> p = new ArrayList<>();
        whereFiltros(sb, p, nombre, puesto, estado, excluirAdmin);
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
                    m.put("idEmpleado", rs.getInt("id_empleado"));
                    m.put("nombres", rs.getString("nombres"));
                    m.put("apellidos", rs.getString("apellidos"));
                    m.put("puesto", rs.getString("puesto"));
                    m.put("estado", rs.getString("estado"));
                    m.put("correo", rs.getString("correo"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    private void whereFiltros(StringBuilder sb, List<Object> p,
            String nombre, String puesto, String estado, boolean excluirAdmin) {
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND CONCAT(e.nombres, ' ', e.apellidos) LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (puesto != null && !puesto.isBlank()) {
            sb.append(" AND e.puesto = ?");
            p.add(puesto);
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND e.estado = ?");
            p.add(estado);
        }
        if (excluirAdmin) {
            sb.append(" AND e.puesto NOT IN ('Admin', 'Super Admin')");
        }
    }

    public void modificar(int idEmpleado, String nombres, String apellidos, String dpi,
            LocalDate fechaNacimiento, String telefono, String direccion, String puesto,
            LocalDate fechaContratacion, BigDecimal salario) throws SQLException {
        String sql = "UPDATE empleado SET nombres = ?, apellidos = ?, dpi = ?, fecha_nacimiento = ?, "
                + "telefono = ?, direccion = ?, puesto = ?, fecha_contratacion = ?, salario = ? "
                + "WHERE id_empleado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombres);
            ps.setString(2, apellidos);
            ps.setString(3, dpi);
            ps.setDate(4, Date.valueOf(fechaNacimiento));
            ps.setString(5, telefono);
            ps.setString(6, direccion);
            ps.setString(7, puesto);
            ps.setDate(8, Date.valueOf(fechaContratacion));
            ps.setBigDecimal(9, salario);
            ps.setInt(10, idEmpleado);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(int idEmpleado, String estado) throws SQLException {
        String sql = "UPDATE empleado SET estado = ? WHERE id_empleado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
        }
    }

    public void actualizarUsuario(int idEmpleado, Integer idUsuario) throws SQLException {
        String sql = "UPDATE empleado SET id_usuario = ? WHERE id_empleado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idUsuario == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, idUsuario);
            }
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
        }
    }

    public int contarSuperAdminActivosExcepto(int idEmpleado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleado e JOIN usuario u ON u.id_usuario = e.id_usuario "
                + "WHERE e.puesto = 'Super Admin' AND u.estado = 'Activo' AND e.id_empleado <> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Empleado mapear(ResultSet rs) throws SQLException {        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getInt("id_empleado"));
        e.setNombres(rs.getString("nombres"));
        e.setApellidos(rs.getString("apellidos"));
        e.setDpi(rs.getString("dpi"));
        Date fn = rs.getDate("fecha_nacimiento");
        e.setFechaNacimiento(fn != null ? fn.toLocalDate() : null);
        e.setTelefono(rs.getString("telefono"));
        e.setDireccion(rs.getString("direccion"));
        e.setPuesto(rs.getString("puesto"));
        e.setEstado(rs.getString("estado"));
        int idU = rs.getInt("id_usuario");
        e.setIdUsuario(rs.wasNull() ? null : idU);
        return e;
    }
}
