package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NivelDAO {

    public boolean existe(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nivel WHERE id_nivel = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<Map<String, Object>> listar() throws SQLException {
        String sql = "SELECT id_nivel, nombre FROM nivel ORDER BY id_nivel";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> l = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("idNivel", rs.getInt("id_nivel"));
                m.put("nombre", rs.getString("nombre"));
                l.add(m);
            }
            return l;
        }
    }

    public boolean esDiversificado(int id) throws SQLException {
        String sql = "SELECT nombre FROM nivel WHERE id_nivel = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getString(1).toLowerCase().contains("diversif");
            }
        }
    }
}
