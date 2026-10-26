package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ParametroBibliotecaDAO {

    public Map<String, Object> obtener() throws SQLException {
        String sql = "SELECT id_parametro, dias_prestamo, multa_por_dia FROM parametro_biblioteca LIMIT 1";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("idParametro", rs.getInt("id_parametro"));
            m.put("diasPrestamo", rs.getInt("dias_prestamo"));
            m.put("multaPorDia", rs.getBigDecimal("multa_por_dia"));
            return m;
        }
    }

    public int crear(int diasPrestamo, BigDecimal multaPorDia) throws SQLException {
        String sql = "INSERT INTO parametro_biblioteca (dias_prestamo, multa_por_dia) VALUES (?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, diasPrestamo);
            ps.setBigDecimal(2, multaPorDia);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(int diasPrestamo, BigDecimal multaPorDia) throws SQLException {
        String sql = "UPDATE parametro_biblioteca SET dias_prestamo = ?, multa_por_dia = ? WHERE id_parametro = 1";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, diasPrestamo);
            ps.setBigDecimal(2, multaPorDia);
            ps.executeUpdate();
        }
    }
}