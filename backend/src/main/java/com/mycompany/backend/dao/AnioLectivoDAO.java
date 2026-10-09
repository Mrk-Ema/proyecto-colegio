package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dto.AnioLectivo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AnioLectivoDAO {

    public void crear(AnioLectivo a) throws SQLException {
        String sql = "INSERT INTO año_lectivo (id_año_lectivo, fecha_inicio, fecha_cierre, estado) "
                + "VALUES (?, ?, ?, 'Planificado')";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, a.getAnio());
            ps.setDate(2, Date.valueOf(a.getFechaInicio()));
            ps.setDate(3, Date.valueOf(a.getFechaCierre()));
            ps.executeUpdate();
        }
    }

    public List<AnioLectivo> listarTodos() throws SQLException {
        String sql = "SELECT id_año_lectivo, fecha_inicio, fecha_cierre, estado "
                + "FROM año_lectivo ORDER BY id_año_lectivo DESC";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<AnioLectivo> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        }
    }

    public AnioLectivo buscarPorAnio(int anio) throws SQLException {
        String sql = "SELECT id_año_lectivo, fecha_inicio, fecha_cierre, estado "
                + "FROM año_lectivo WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        }
    }

    public void modificarFechas(int anio, java.time.LocalDate inicio, java.time.LocalDate cierre) throws SQLException {
        String sql = "UPDATE año_lectivo SET fecha_inicio = ?, fecha_cierre = ? "
                + "WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(cierre));
            ps.setInt(3, anio);
            ps.executeUpdate();
        }
    }

    public boolean existe(int anio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM año_lectivo WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void actualizarEstado(int anio, String estado) throws SQLException {
        String sql = "UPDATE año_lectivo SET estado = ? WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, anio);
            ps.executeUpdate();
        }
    }

    public AnioLectivo buscarActivo() throws SQLException {
        String sql = "SELECT id_año_lectivo, fecha_inicio, fecha_cierre, estado "
                + "FROM año_lectivo WHERE estado = 'Activo' LIMIT 1";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapear(rs);
            }
            return null;
        }
    }

    public void eliminar(int anio) throws SQLException {
        String sql = "DELETE FROM año_lectivo WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.executeUpdate();
        }
    }

    private AnioLectivo mapear(ResultSet rs) throws SQLException {
        AnioLectivo a = new AnioLectivo();
        a.setAnio(rs.getInt("id_año_lectivo"));
        Date ini = rs.getDate("fecha_inicio");
        a.setFechaInicio(ini != null ? ini.toLocalDate() : null);
        Date fin = rs.getDate("fecha_cierre");
        a.setFechaCierre(fin != null ? fin.toLocalDate() : null);
        a.setEstado(rs.getString("estado"));
        return a;
    }
}
