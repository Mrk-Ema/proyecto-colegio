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


public class GradoDAO {

    public List<Map<String, Object>> listarActivos() throws SQLException {
        String sql = "SELECT g.id_grado, g.nombre, n.nombre AS nivel "
                + "FROM grado g JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "WHERE g.estado = 'Activo' ORDER BY n.id_nivel, g.nombre";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("idGrado", rs.getInt("id_grado"));
                fila.put("nombre", rs.getString("nombre"));
                fila.put("nivel", rs.getString("nivel"));
                lista.add(fila);
            }
            return lista;
        }
    }

    public int contarActivos(java.util.List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        StringBuilder marcas = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                marcas.append(",");
            }
            marcas.append("?");
        }
        String sql = "SELECT COUNT(*) FROM grado WHERE estado = 'Activo' AND id_grado IN (" + marcas + ")";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
