package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibroDAO {

    public int contar(String titulo, String autor, String estado) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM libro WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (titulo != null && !titulo.isBlank()) {
            sb.append(" AND titulo LIKE ?");
            p.add("%" + titulo.trim() + "%");
        }
        if (autor != null && !autor.isBlank()) {
            sb.append(" AND autor LIKE ?");
            p.add("%" + autor.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND estado = ?");
            p.add(estado);
        }
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

    public List<Map<String, Object>> listar(String titulo, String autor, String estado,
            int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT id_libro, titulo, autor, editorial, "
                + "cantidad_total, cantidad_disponible, estado FROM libro WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (titulo != null && !titulo.isBlank()) {
            sb.append(" AND titulo LIKE ?");
            p.add("%" + titulo.trim() + "%");
        }
        if (autor != null && !autor.isBlank()) {
            sb.append(" AND autor LIKE ?");
            p.add("%" + autor.trim() + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND estado = ?");
            p.add(estado);
        }
        sb.append(" ORDER BY titulo LIMIT ? OFFSET ?");
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
                    m.put("idLibro", rs.getInt("id_libro"));
                    m.put("titulo", rs.getString("titulo"));
                    m.put("autor", rs.getString("autor"));
                    m.put("editorial", rs.getString("editorial"));
                    m.put("cantidadTotal", rs.getInt("cantidad_total"));
                    m.put("cantidadDisponible", rs.getInt("cantidad_disponible"));
                    m.put("estado", rs.getString("estado"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_libro, titulo, autor, editorial, cantidad_total, cantidad_disponible, estado "
                + "FROM libro WHERE id_libro = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idLibro", rs.getInt("id_libro"));
                m.put("titulo", rs.getString("titulo"));
                m.put("autor", rs.getString("autor"));
                m.put("editorial", rs.getString("editorial"));
                m.put("cantidadTotal", rs.getInt("cantidad_total"));
                m.put("cantidadDisponible", rs.getInt("cantidad_disponible"));
                m.put("estado", rs.getString("estado"));
                return m;
            }
        }
    }

    public int crear(String titulo, String autor, String editorial, int cantidad) throws SQLException {
        String sql = "INSERT INTO libro (titulo, autor, editorial, cantidad_total, cantidad_disponible, estado) "
                + "VALUES (?, ?, ?, ?, ?, 'Activo')";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, titulo);
            if (autor == null) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, autor);
            }
            if (editorial == null) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, editorial);
            }
            ps.setInt(4, cantidad);
            ps.setInt(5, cantidad);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void modificar(int id, String titulo, String autor, String editorial, int cantidad)
            throws SQLException {
        String sql = "UPDATE libro SET titulo = ?, autor = ?, editorial = ?, "
                + "cantidad_disponible = cantidad_disponible + (? - cantidad_total), "
                + "cantidad_total = ? WHERE id_libro = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, titulo);
            if (autor == null) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, autor);
            }
            if (editorial == null) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, editorial);
            }
            ps.setInt(4, cantidad);
            ps.setInt(5, cantidad);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE libro SET estado = ? WHERE id_libro = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean existePorTitulo(String titulo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM libro WHERE titulo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, titulo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeOtroConTitulo(int id, String titulo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM libro WHERE titulo = ? AND id_libro <> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, titulo);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}