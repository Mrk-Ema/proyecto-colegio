package com.mycompany.backend.dao;

import com.mycompany.backend.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AsignacionDAO {

    public boolean existePorAnio(int anio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM asignacion_maestro WHERE id_año_lectivo = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean cursoTieneMaestro(int anio, int idGrado, int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM asignacion_maestro am JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "WHERE am.id_año_lectivo = ? AND s.id_grado = ? AND am.id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idGrado);
            ps.setInt(3, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean cursoTienePonderacion(int anio, int idGrado, int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM actividad_evaluable ae JOIN asignacion_maestro am ON am.id_asignacion = ae.id_asignacion "
                + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "WHERE am.id_año_lectivo = ? AND s.id_grado = ? AND am.id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idGrado);
            ps.setInt(3, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean cursoTieneNotas(int anio, int idGrado, int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nota n JOIN actividad_evaluable ae ON ae.id_actividad_evaluable = n.id_actividad_evaluable "
                + "JOIN asignacion_maestro am ON am.id_asignacion = ae.id_asignacion "
                + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "WHERE am.id_año_lectivo = ? AND s.id_grado = ? AND am.id_curso = ?";
        Connection cn = Conexion.obtener();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idGrado);
            ps.setInt(3, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
