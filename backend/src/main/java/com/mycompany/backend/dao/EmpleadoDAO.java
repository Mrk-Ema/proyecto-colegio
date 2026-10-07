package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dto.Empleado;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
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
