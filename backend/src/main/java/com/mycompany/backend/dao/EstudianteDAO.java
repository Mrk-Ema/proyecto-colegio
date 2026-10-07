package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dto.Estudiante;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
