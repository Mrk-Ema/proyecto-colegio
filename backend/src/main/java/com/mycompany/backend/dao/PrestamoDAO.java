package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrestamoDAO {

    public int contar(Integer libro, Integer idPrestatario, String tipoPrestatario, String estado,
            String desde, String hasta) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM prestamo p "
                + "JOIN libro l ON l.id_libro = p.id_libro WHERE 1=1");
        List<Object> params = new ArrayList<>();
        whereFiltros(sb, params, libro, idPrestatario, tipoPrestatario, estado, desde, hasta);
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Map<String, Object>> listar(Integer libro, Integer idPrestatario, String tipoPrestatario,
            String estado, String desde, String hasta, int limit, int offset) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT p.id_prestamo, p.fecha_prestamo, p.fecha_esperada, "
                + "p.fecha_devolucion, p.estado, p.id_libro, l.titulo AS libro, "
                + "p.id_estudiante, p.id_empleado, "
                + "e.nombres AS est_nombres, e.apellidos AS est_apellidos, e.carnet AS est_carnet, "
                + "em.nombres AS emp_nombres, em.apellidos AS emp_apellidos "
                + "FROM prestamo p JOIN libro l ON l.id_libro = p.id_libro "
                + "LEFT JOIN estudiante e ON e.id_estudiante = p.id_estudiante "
                + "LEFT JOIN empleado em ON em.id_empleado = p.id_empleado WHERE 1=1");
        List<Object> params = new ArrayList<>();
        whereFiltros(sb, params, libro, idPrestatario, tipoPrestatario, estado, desde, hasta);
        sb.append(" ORDER BY p.fecha_prestamo DESC, p.id_prestamo DESC LIMIT ? OFFSET ?");
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            int i = 1;
            for (; i <= params.size(); i++) {
                ps.setObject(i, params.get(i - 1));
            }
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> lista = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idPrestamo", rs.getInt("id_prestamo"));
                    m.put("fechaPrestamo", rs.getDate("fecha_prestamo").toLocalDate().toString());
                    m.put("fechaEsperada", rs.getDate("fecha_esperada").toLocalDate().toString());
                    Date dev = rs.getDate("fecha_devolucion");
                    m.put("fechaDevolucion", dev != null ? dev.toLocalDate().toString() : null);
                    m.put("estado", rs.getString("estado"));
                    m.put("idLibro", rs.getInt("id_libro"));
                    m.put("libro", rs.getString("libro"));
                    int idEst = rs.getInt("id_estudiante");
                    m.put("idEstudiante", rs.wasNull() ? null : idEst);
                    int idEmp = rs.getInt("id_empleado");
                    m.put("idEmpleado", rs.wasNull() ? null : idEmp);
                    if (idEst > 0) {
                        m.put("prestatario", rs.getString("est_nombres") + " " + rs.getString("est_apellidos"));
                        m.put("tipoPrestatario", "Estudiante");
                        m.put("carnet", rs.getString("est_carnet"));
                    } else {
                        m.put("prestatario", rs.getString("emp_nombres") + " " + rs.getString("emp_apellidos"));
                        m.put("tipoPrestatario", "Maestro");
                        m.put("carnet", null);
                    }
                    lista.add(m);
                }
                return lista;
            }
        }
    }

    private void whereFiltros(StringBuilder sb, List<Object> params, Integer libro,
            Integer idPrestatario, String tipoPrestatario, String estado, String desde, String hasta) {
        if (libro != null) {
            sb.append(" AND p.id_libro = ?");
            params.add(libro);
        }
        if (idPrestatario != null) {
            if ("estudiante".equalsIgnoreCase(tipoPrestatario)) {
                sb.append(" AND p.id_estudiante = ?");
            } else if ("maestro".equalsIgnoreCase(tipoPrestatario)) {
                sb.append(" AND p.id_empleado = ?");
            } else {
                sb.append(" AND (p.id_estudiante = ? OR p.id_empleado = ?)");
                params.add(idPrestatario);
            }
            params.add(idPrestatario);
        }
        if (estado != null && !estado.isBlank()) {
            sb.append(" AND p.estado = ?");
            params.add(estado);
        }
        if (desde != null && !desde.isBlank()) {
            sb.append(" AND p.fecha_prestamo >= ?");
            params.add(Date.valueOf(desde.trim()));
        }
        if (hasta != null && !hasta.isBlank()) {
            sb.append(" AND p.fecha_prestamo <= ?");
            params.add(Date.valueOf(hasta.trim()));
        }
    }

    public Map<String, Object> buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.id_prestamo, p.fecha_prestamo, p.fecha_esperada, p.fecha_devolucion, "
                + "p.estado, p.id_libro, l.titulo AS libro, l.cantidad_disponible, "
                + "p.id_estudiante, p.id_empleado, "
                + "e.nombres AS est_nombres, e.apellidos AS est_apellidos, e.carnet AS est_carnet, "
                + "em.nombres AS emp_nombres, em.apellidos AS emp_apellidos "
                + "FROM prestamo p JOIN libro l ON l.id_libro = p.id_libro "
                + "LEFT JOIN estudiante e ON e.id_estudiante = p.id_estudiante "
                + "LEFT JOIN empleado em ON em.id_empleado = p.id_empleado "
                + "WHERE p.id_prestamo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("idPrestamo", rs.getInt("id_prestamo"));
                m.put("fechaPrestamo", rs.getDate("fecha_prestamo").toLocalDate().toString());
                m.put("fechaEsperada", rs.getDate("fecha_esperada").toLocalDate().toString());
                Date dev = rs.getDate("fecha_devolucion");
                m.put("fechaDevolucion", dev != null ? dev.toLocalDate().toString() : null);
                m.put("estado", rs.getString("estado"));
                m.put("idLibro", rs.getInt("id_libro"));
                m.put("libro", rs.getString("libro"));
                m.put("cantidadDisponible", rs.getInt("cantidad_disponible"));
                int idEst = rs.getInt("id_estudiante");
                m.put("idEstudiante", rs.wasNull() ? null : idEst);
                int idEmp = rs.getInt("id_empleado");
                m.put("idEmpleado", rs.wasNull() ? null : idEmp);
                if (idEst > 0) {
                    m.put("prestatario", rs.getString("est_nombres") + " " + rs.getString("est_apellidos"));
                    m.put("tipoPrestatario", "Estudiante");
                    m.put("carnet", rs.getString("est_carnet"));
                } else {
                    m.put("prestatario", rs.getString("emp_nombres") + " " + rs.getString("emp_apellidos"));
                    m.put("tipoPrestatario", "Maestro");
                    m.put("carnet", null);
                }
                return m;
            }
        }
    }

    public boolean existePrestamoActivo(int idLibro, Integer idEstudiante, Integer idEmpleado)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamo WHERE id_libro = ? AND estado = 'Activo' "
                + "AND (id_estudiante = ? OR id_empleado = ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            if (idEstudiante == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, idEstudiante);
            }
            if (idEmpleado == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, idEmpleado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int crear(int idLibro, LocalDate fechaPrestamo, LocalDate fechaEsperada,
            Integer idEstudiante, Integer idEmpleado) throws SQLException {
        String sql = "INSERT INTO prestamo (fecha_prestamo, fecha_esperada, estado, id_libro, "
                + "id_estudiante, id_empleado) VALUES (?, ?, 'Activo', ?, ?, ?)";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(fechaPrestamo));
            ps.setDate(2, Date.valueOf(fechaEsperada));
            ps.setInt(3, idLibro);
            if (idEstudiante == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, idEstudiante);
            }
            if (idEmpleado == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, idEmpleado);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void registrarDevolucion(int idPrestamo, LocalDate fechaDevolucion) throws SQLException {
        String sql = "UPDATE prestamo SET fecha_devolucion = ?, estado = 'Devuelto' WHERE id_prestamo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaDevolucion));
            ps.setInt(2, idPrestamo);
            ps.executeUpdate();
        }
    }

    public void decrementarDisponible(int idLibro) throws SQLException {
        String sql = "UPDATE libro SET cantidad_disponible = cantidad_disponible - 1 WHERE id_libro = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            ps.executeUpdate();
        }
    }

    public void incrementarDisponible(int idLibro) throws SQLException {
        String sql = "UPDATE libro SET cantidad_disponible = cantidad_disponible + 1 WHERE id_libro = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            ps.executeUpdate();
        }
    }
}