package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CurriculoDAO {

    public List<Integer> cursosActivosDeGradoDefecto(int idGrado) throws SQLException {
        String sql = "SELECT cu.id_curso FROM curriculo cu "
                + "JOIN estructura_año e ON e.id_estructura = cu.id_estructura "
                + "JOIN curso c ON c.id_curso = cu.id_curso "
                + "WHERE e.id_año_lectivo IS NULL AND e.id_grado = ? AND c.estado = 'Activo'";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGrado);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> cursos = new ArrayList<>();
                while (rs.next()) {
                    cursos.add(rs.getInt("id_curso"));
                }
                return cursos;
            }
        }
    }

    public void crear(int idEstructura, int idCurso) throws SQLException {
        String sql = "INSERT INTO curriculo (id_estructura, id_curso) VALUES (?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstructura);
            ps.setInt(2, idCurso);
            ps.executeUpdate();
        }
    }

    public void eliminarPorEstructura(int idEstructura) throws SQLException {
        String sql = "DELETE FROM curriculo WHERE id_estructura = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstructura);
            ps.executeUpdate();
        }
    }

    public boolean existeEnEstructura(int idEstructura, int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM curriculo WHERE id_estructura = ? AND id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstructura);
            ps.setInt(2, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
