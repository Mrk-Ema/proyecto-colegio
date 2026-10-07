package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dto.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario buscarPorCorreo(String correo) throws SQLException {
        String sql = "SELECT id_usuario, correo, `contraseña_hash`, rol, estado "
                + "FROM usuario WHERE correo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setCorreo(rs.getString("correo"));
                    u.setPassword(rs.getString("contraseña_hash"));
                    u.setRol(rs.getString("rol"));
                    u.setEstado(rs.getString("estado"));
                    return u;
                }
                return null;
            }
        }
    }

    public Usuario buscarPorId(Integer idUsuario) throws SQLException {
        String sql = "SELECT id_usuario, correo, `contraseña_hash`, rol, estado "
                + "FROM usuario WHERE id_usuario = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setCorreo(rs.getString("correo"));
                    u.setPassword(rs.getString("contraseña_hash"));
                    u.setRol(rs.getString("rol"));
                    u.setEstado(rs.getString("estado"));
                    return u;
                }
                return null;
            }
        }
    }

    public void actualizarPassword(Integer idUsuario, String hash) throws SQLException {
        String sql = "UPDATE usuario SET `contraseña_hash` = ? WHERE id_usuario = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void actualizarCorreo(Integer idUsuario, String correo) throws SQLException {
        String sql = "UPDATE usuario SET correo = ? WHERE id_usuario = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }
}
