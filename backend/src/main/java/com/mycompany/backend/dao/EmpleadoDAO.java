package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dto.Empleado;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
