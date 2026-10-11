package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GradoDAO {

    public List<Map<String, Object>> listarActivos() throws SQLException {
        String sql = "SELECT g.id_grado, g.nombre, n.nombre AS nivel, g.id_nivel, g.id_carrera, c.nombre AS carrera "
                + "FROM grado g JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera "
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
                fila.put("idNivel", rs.getInt("id_nivel"));
                int ic = rs.getInt("id_carrera");
                fila.put("idCarrera", rs.wasNull() ? null : ic);
                fila.put("carrera", rs.getString("carrera"));
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

    public int crear(String nombre, int idNivel, Integer idCarrera) throws SQLException {
        String sql = "INSERT INTO grado (nombre, estado, id_nivel, id_carrera) VALUES (?, 'Activo', ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setInt(2, idNivel);
            if (idCarrera == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, idCarrera);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT g.id_grado, g.nombre, g.estado, g.id_nivel, n.nombre AS nivel, "
                + "g.id_carrera, c.nombre AS carrera "
                + "FROM grado g JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera WHERE g.id_grado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idGrado", rs.getInt("id_grado"));
                m.put("nombre", rs.getString("nombre"));
                m.put("estado", rs.getString("estado"));
                m.put("idNivel", rs.getInt("id_nivel"));
                m.put("nivel", rs.getString("nivel"));
                int ic = rs.getInt("id_carrera");
                m.put("idCarrera", rs.wasNull() ? null : ic);
                m.put("carrera", rs.getString("carrera"));
                return m;
            }
        }
    }

    public boolean existePorNombreEnNivelCarrera(String nombre, int nivel, Integer carrera) throws SQLException {
        String sql = "SELECT COUNT(*) FROM grado WHERE nombre = ? AND id_nivel = ? AND id_carrera <=> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, nivel);
            if (carrera == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, carrera);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeOtroConNombre(int id, String nombre, int nivel, Integer carrera) throws SQLException {
        String sql = "SELECT COUNT(*) FROM grado WHERE nombre = ? AND id_nivel = ? AND id_carrera <=> ? AND id_grado <> ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, nivel);
            if (carrera == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, carrera);
            }
            ps.setInt(4, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void modificar(int id, String nombre, int nivel, Integer carrera, boolean tocaUbicacion) throws SQLException {
        String sql = tocaUbicacion
                ? "UPDATE grado SET nombre = ?, id_nivel = ?, id_carrera = ? WHERE id_grado = ?"
                : "UPDATE grado SET nombre = ? WHERE id_grado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            if (tocaUbicacion) {
                ps.setInt(2, nivel);
                if (carrera == null) {
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setInt(3, carrera);
                }
                ps.setInt(4, id);
            } else {
                ps.setInt(2, id);
            }
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE grado SET estado = ? WHERE id_grado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneSecciones(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seccion WHERE id_grado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean tieneCurriculoDefecto(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM estructura_año e JOIN curriculo cu ON cu.id_estructura = e.id_estructura "
                + "WHERE e.id_año_lectivo IS NULL AND e.id_grado = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int contar(String nombre, Integer nivel, Integer carrera, String estado, String anio) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(DISTINCT g.id_grado) FROM grado g ");
        List<Object> p = new ArrayList<>();
        if (anio != null && !anio.isBlank()) {
            if (anio.equals("defecto")) {
                sb.append("JOIN estructura_año e ON e.id_grado = g.id_grado AND e.id_año_lectivo IS NULL ");
            } else {
                sb.append("JOIN estructura_año e ON e.id_grado = g.id_grado AND e.id_año_lectivo = ? ");
                p.add(Integer.parseInt(anio));
            }
        }
        sb.append("WHERE 1=1");
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND g.nombre LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (nivel != null) {
            sb.append(" AND g.id_nivel = ?");
            p.add(nivel);
        }
        if (carrera != null) {
            sb.append(" AND g.id_carrera = ?");
            p.add(carrera);
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND g.estado = ?");
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

    public List<Map<String, Object>> listar(String nombre, Integer nivel, Integer carrera, String estado, String anio, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT g.id_grado, g.nombre, g.estado, g.id_nivel, n.nombre AS nivel, "
                + "g.id_carrera, c.nombre AS carrera FROM grado g "
                + "JOIN nivel n ON n.id_nivel = g.id_nivel "
                + "LEFT JOIN carrera c ON c.id_carrera = g.id_carrera ");
        List<Object> p = new ArrayList<>();
        if (anio != null && !anio.isBlank()) {
            if (anio.equals("defecto")) {
                sb.append("JOIN estructura_año e ON e.id_grado = g.id_grado AND e.id_año_lectivo IS NULL ");
            } else {
                sb.append("JOIN estructura_año e ON e.id_grado = g.id_grado AND e.id_año_lectivo = ? ");
                p.add(Integer.parseInt(anio));
            }
        }
        sb.append("WHERE 1=1");
        if (nombre != null && !nombre.isBlank()) {
            sb.append(" AND g.nombre LIKE ?");
            p.add("%" + nombre.trim() + "%");
        }
        if (nivel != null) {
            sb.append(" AND g.id_nivel = ?");
            p.add(nivel);
        }
        if (carrera != null) {
            sb.append(" AND g.id_carrera = ?");
            p.add(carrera);
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND g.estado = ?");
            p.add(estado);
        }
        sb.append(" GROUP BY g.id_grado ORDER BY n.id_nivel, g.nombre LIMIT ? OFFSET ?");
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
                    m.put("idGrado", rs.getInt("id_grado"));
                    m.put("nombre", rs.getString("nombre"));
                    m.put("estado", rs.getString("estado"));
                    m.put("idNivel", rs.getInt("id_nivel"));
                    m.put("nivel", rs.getString("nivel"));
                    int ic = rs.getInt("id_carrera");
                    m.put("idCarrera", rs.wasNull() ? null : ic);
                    m.put("carrera", rs.getString("carrera"));
                    l.add(m);
                }
                return l;
            }
        }
    }
}
