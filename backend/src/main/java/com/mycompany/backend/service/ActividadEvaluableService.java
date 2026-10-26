package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.ActividadEvaluableDAO;
import com.mycompany.backend.dao.AsignacionDAO;
import com.mycompany.backend.dao.EmpleadoDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.NotaDAO;
import com.mycompany.backend.dto.Empleado;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ponderación de actividades de un curso y notas de los estudiantes.
 * La ponderación solo se define en ciclo Activo y el total debe ser 100.
 */
public class ActividadEvaluableService {

    private final ActividadEvaluableDAO dao = new ActividadEvaluableDAO();
    private final NotaDAO notaDAO = new NotaDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    /** Lista de actividades de la ponderación de un curso. */
    public List<Map<String, Object>> listar(int idAsignacion, int idUsuario, String rol) throws Exception {
        validarAsignacionDelMaestro(idAsignacion, idUsuario, rol);
        List<Map<String, Object>> actividades = dao.listarPorAsignacion(idAsignacion);
        for (Map<String, Object> a : actividades) {
            a.put("tieneNotas", dao.tieneNotas((Integer) a.get("idActividadEvaluable")));
        }
        return actividades;
    }

    /** Crea o modifica la ponderación de un curso. */
    public Map<String, Object> guardar(int idAsignacion, int idUsuario, String rol,
            List<Map<String, Object>> actividades, boolean confirmar) throws Exception {
        validarAsignacionDelMaestro(idAsignacion, idUsuario, rol);
        validarCicloActivo(idAsignacion);

        if (actividades == null || actividades.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos una actividad");
        }
        BigDecimal total = BigDecimal.ZERO;
        Set<String> nombres = new HashSet<>();
        for (Map<String, Object> a : actividades) {
            String nombre = texto(a.get("nombre"));
            if (nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre de la actividad es obligatorio");
            }
            if (!nombres.add(nombre.toLowerCase())) {
                throw new IllegalArgumentException("El nombre de la actividad no se repite");
            }
            BigDecimal puntos = decimal(a.get("puntos"));
            if (puntos == null || puntos.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Los puntos deben ser mayores a cero");
            }
            total = total.add(puntos);
        }
        if (total.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("El total debe ser exactamente 100");
        }

        List<Map<String, Object>> existentes = dao.listarPorAsignacion(idAsignacion);
        boolean hayExistentes = !existentes.isEmpty();

        // Detectar si hay notas que se recalcularán
        int notasAfectadas = 0;
        if (hayExistentes) {
            for (Map<String, Object> nueva : actividades) {
                Integer id = enteroONulo(nueva.get("idActividadEvaluable"));
                if (id != null) {
                    for (Map<String, Object> vieja : existentes) {
                        if (((Integer) vieja.get("idActividadEvaluable")) == id) {
                            BigDecimal viejosPuntos = (BigDecimal) vieja.get("puntos");
                            BigDecimal nuevosPuntos = decimal(nueva.get("puntos"));
                            if (viejosPuntos.compareTo(nuevosPuntos) != 0 && dao.tieneNotas(id)) {
                                notasAfectadas += notaDAO.contarPorActividad(id);
                            }
                        }
                    }
                }
            }
        }

        if (notasAfectadas > 0 && !confirmar) {
            throw new IllegalArgumentException("Hay " + notasAfectadas
                    + " notas que se recalcularán. ¿Confirmar?");
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            if (hayExistentes) {
                Set<Integer> idsNuevos = new HashSet<>();
                for (Map<String, Object> a : actividades) {
                    Integer id = enteroONulo(a.get("idActividadEvaluable"));
                    if (id != null) {
                        idsNuevos.add(id);
                    }
                }
                // Eliminar las que ya no están (solo si no tienen notas)
                for (Map<String, Object> vieja : existentes) {
                    int idVieja = (Integer) vieja.get("idActividadEvaluable");
                    if (!idsNuevos.contains(idVieja)) {
                        if (dao.tieneNotas(idVieja)) {
                            throw new IllegalArgumentException("No se puede quitar la actividad '"
                                    + vieja.get("nombre") + "' porque tiene notas registradas");
                        }
                        notaDAO.eliminarPorActividad(idVieja);
                        dao.eliminar(idVieja);
                    }
                }
                // Crear o actualizar
                for (Map<String, Object> a : actividades) {
                    String nombre = texto(a.get("nombre"));
                    BigDecimal puntos = decimal(a.get("puntos"));
                    Integer id = enteroONulo(a.get("idActividadEvaluable"));
                    if (id == null) {
                        dao.crear(idAsignacion, nombre, puntos);
                    } else {
                        Map<String, Object> vieja = buscarActividad(existentes, id);
                        BigDecimal viejosPuntos = vieja != null ? (BigDecimal) vieja.get("puntos") : puntos;
                        dao.actualizar(id, nombre, puntos);
                        // Recalcular notas si cambió los puntos y hay notas
                        if (confirmar && viejosPuntos.compareTo(puntos) != 0 && dao.tieneNotas(id)) {
                            rescalarNotas(id, viejosPuntos, puntos);
                        }
                    }
                }
            } else {
                for (Map<String, Object> a : actividades) {
                    dao.crear(idAsignacion, texto(a.get("nombre")), decimal(a.get("puntos")));
                }
            }
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("mensaje", hayExistentes ? "Ponderación modificada" : "Ponderación definida");
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir la ponderación");
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                cn.setAutoCommit(anterior);
            } catch (SQLException ex) {
                System.out.println("Error al restaurar autoCommit");
                ex.printStackTrace();
            }
        }
    }

    public void eliminar(int idAsignacion, int idUsuario, String rol) throws Exception {
        validarAsignacionDelMaestro(idAsignacion, idUsuario, rol);
        validarCicloActivo(idAsignacion);
        List<Map<String, Object>> actividades = dao.listarPorAsignacion(idAsignacion);
        for (Map<String, Object> a : actividades) {
            if (dao.tieneNotas((Integer) a.get("idActividadEvaluable"))) {
                throw new IllegalArgumentException("No se puede eliminar porque hay notas registradas");
            }
        }
        for (Map<String, Object> a : actividades) {
            notaDAO.eliminarPorActividad((Integer) a.get("idActividadEvaluable"));
        }
        dao.eliminarPorAsignacion(idAsignacion);
    }

    public boolean tieneNotas(int idAsignacion) throws SQLException {
        List<Map<String, Object>> actividades = dao.listarPorAsignacion(idAsignacion);
        for (Map<String, Object> a : actividades) {
            if (dao.tieneNotas((Integer) a.get("idActividadEvaluable"))) {
                return true;
            }
        }
        return false;
    }

    private void rescalarNotas(int idActividad, BigDecimal viejosPuntos, BigDecimal nuevosPuntos)
            throws SQLException {
        List<Map<String, Object>> notas = notaDAO.listarPorActividad(idActividad);
        for (Map<String, Object> n : notas) {
            BigDecimal valorActual = (BigDecimal) n.get("valor");
            BigDecimal nuevoValor = valorActual.multiply(nuevosPuntos).divide(viejosPuntos, 2, RoundingMode.HALF_UP);
            if (nuevoValor.compareTo(nuevosPuntos) > 0) {
                nuevoValor = nuevosPuntos;
            }
            if (nuevoValor.compareTo(BigDecimal.ZERO) < 0) {
                nuevoValor = BigDecimal.ZERO;
            }
            notaDAO.actualizar((Integer) n.get("idNota"), nuevoValor);
        }
    }

    private Map<String, Object> buscarActividad(List<Map<String, Object>> lista, int id) {
        for (Map<String, Object> a : lista) {
            if (((Integer) a.get("idActividadEvaluable")) == id) {
                return a;
            }
        }
        return null;
    }

    private void validarAsignacionDelMaestro(int idAsignacion, int idUsuario, String rol) throws Exception {
        Map<String, Object> a = asignacionDAO.buscarPorId(idAsignacion);
        if (a == null) {
            throw new NotFoundException("Asignación no encontrada");
        }
        if ("Super Admin".equals(rol)) {
            return; // Super Admin puede acceder a cualquier asignación
        }
        Empleado e = empleadoDAO.buscarPorUsuario(idUsuario);
        if (e == null || !e.getIdEmpleado().equals(a.get("idEmpleado"))) {
            throw new SecurityException("No autorizado para esta asignación");
        }
    }

    private void validarCicloActivo(int idAsignacion) throws Exception {
        Map<String, Object> a = asignacionDAO.buscarPorId(idAsignacion);
        if (a == null) {
            throw new NotFoundException("Asignación no encontrada");
        }
        int anio = (Integer) a.get("anio");
        com.mycompany.backend.dao.AnioLectivoDAO anioDAO = new com.mycompany.backend.dao.AnioLectivoDAO();
        com.mycompany.backend.dto.AnioLectivo al = anioDAO.buscarPorAnio(anio);
        if (al == null || !"Activo".equals(al.getEstado())) {
            throw new IllegalArgumentException("Solo se puede definir la ponderación en un ciclo Activo");
        }
    }

    private String texto(Object v) {
        return v == null || "null".equals(String.valueOf(v)) ? "" : String.valueOf(v).trim();
    }

    private BigDecimal decimal(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer enteroONulo(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}