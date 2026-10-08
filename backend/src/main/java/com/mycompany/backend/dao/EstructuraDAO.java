package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstructuraDAO {

    public int crear(int anio, int idGrado) throws SQLException {
        String sql = "INSERT INTO estructura_año (id_año_lectivo, id_grado) VALUES (?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, anio);
            ps.setInt(2, idGrado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Integer> listarGradosDefecto() throws SQLException {
        String sql = "SELECT id_grado FROM estructura_año WHERE id_año_lectivo IS NULL";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Integer> grados = new ArrayList<>();
            while (rs.next()) {
                grados.add(rs.getInt("id_grado"));
            }
            return grados;
        }
    }

    public List<Map<String, Object>> listarPorAnio(int anio) throws SQLException {
        String sql = "SELECT e.id_estructura, g.id_grado, g.nombre AS grado "
                + "FROM estructura_año e JOIN grado g ON g.id_grado = e.id_grado "
                + "WHERE e.id_año_lectivo = ? ORDER BY g.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> lista = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("idEstructura", rs.getInt("id_estructura"));
                    fila.put("idGrado", rs.getInt("id_grado"));
                    fila.put("grado", rs.getString("grado"));
                    lista.add(fila);
                }
                return lista;
            }
        }
    }
}
