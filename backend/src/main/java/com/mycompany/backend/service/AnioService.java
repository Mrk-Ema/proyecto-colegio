package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.AsignacionDAO;
import com.mycompany.backend.dao.CurriculoDAO;
import com.mycompany.backend.dao.EstructuraDAO;
import com.mycompany.backend.dao.GradoDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dto.AnioLectivo;
import jakarta.ws.rs.NotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * crea una copia de estructura y curriculo y se vincula a un año lectivo
 */
public class AnioService {

    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();
    private final EstructuraDAO estructuraDAO = new EstructuraDAO();
    private final CurriculoDAO curriculoDAO = new CurriculoDAO();
    private final GradoDAO gradoDAO = new GradoDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();

    public void crearAnio(int anio, LocalDate inicio, LocalDate cierre, List<Integer> grados) throws SQLException {
        if (inicio == null || cierre == null) {
            throw new IllegalArgumentException("Completa el año, fecha de inicio y fecha de cierre");
        }

        validarAnio(anio);
        validarFechas(inicio, cierre);
        validarCoherencia(anio, inicio, cierre);
        if (grados == null || grados.isEmpty()) {
            throw new IllegalArgumentException("Debe marcar al menos un grado");
        }
        if (anioDAO.existe(anio)) {
            throw new IllegalArgumentException("El año lectivo ya existe");
        }
        if (gradoDAO.contarActivos(grados) != grados.size()) {
            throw new IllegalArgumentException("Hay grados inválidos o inactivos");
        }
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            AnioLectivo a = new AnioLectivo();
            a.setAnio(anio);
            a.setFechaInicio(inicio);
            a.setFechaCierre(cierre);
            anioDAO.crear(a);
            for (Integer idGrado : grados) {
                int nuevaEstructura = estructuraDAO.crear(anio, idGrado);
                for (Integer idCurso : curriculoDAO.cursosActivosDeGradoDefecto(idGrado)) {
                    curriculoDAO.crear(nuevaEstructura, idCurso);
                }
            }
            cn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir crearAnio");
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

    public void modificar(int anio, LocalDate inicio, LocalDate cierre) throws SQLException {
        if (inicio == null || cierre == null) {
            throw new IllegalArgumentException("Completa el año, fecha de inicio y fecha de cierre");
        }

        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new NotFoundException("Año lectivo no encontrado");
        }
        if ("Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("Un año Cerrado no se modifica");
        }
        validarFechas(inicio, cierre);
        validarCoherencia(anio, inicio, cierre);
        anioDAO.modificarFechas(anio, inicio, cierre);
    }

    public List<AnioLectivo> listar() throws SQLException {
        return anioDAO.listarTodos();
    }

    /**
     * CU010: activa el año (solo desde Planificado y sin otro Activo).
     * El año Activo solo sale por cierre manual (CU011): nunca vuelve
     * a Planificado. Ciclo estrictamente hacia adelante:
     * Planificado -> Activo -> Cerrado.
     */
    public void activar(int anio) throws SQLException {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new NotFoundException("Año lectivo no encontrado");
        }
        if ("Activo".equals(a.getEstado())) {
            throw new IllegalArgumentException("El año ya está activo");
        }
        if ("Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("Un año Cerrado no se reactiva");
        }
        AnioLectivo actual = anioDAO.buscarActivo();
        if (actual != null) {
            throw new IllegalArgumentException(
                    "Cierre el año " + actual.getAnio() + " antes de activar uno nuevo");
        }
        anioDAO.actualizarEstado(anio, "Activo");
    }

  
    public void cerrar(int anio) throws SQLException {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new NotFoundException("Año lectivo no encontrado");
        }
        if (!"Activo".equals(a.getEstado())) {
            throw new IllegalArgumentException("Solo se cierra el año Activo");
        }
        anioDAO.actualizarEstado(anio, "Cerrado");
    }

    /**
     * elimina el año solo Planificado, y activos sin inscripciones ni
     * asignaciones
     */
    public void eliminar(int anio) throws SQLException {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new NotFoundException("Año lectivo no encontrado");
        }
        if (!"Planificado".equals(a.getEstado())) {
            throw new IllegalArgumentException("Solo se elimina en estado Planificado");
        }
        if (inscripcionDAO.existePorAnio(anio)) {
            throw new IllegalArgumentException("Tiene inscripciones, no se puede eliminar");
        }
        if (asignacionDAO.existePorAnio(anio)) {
            throw new IllegalArgumentException("Tiene maestros asignados, no se puede eliminar");
        }
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            for (Map<String, Object> fila : estructuraDAO.listarPorAnio(anio)) {
                int idEstructura = (Integer) fila.get("idEstructura");
                curriculoDAO.eliminarPorEstructura(idEstructura);
                estructuraDAO.eliminar(idEstructura);
            }
            anioDAO.eliminar(anio);
            cn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir eliminar");
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

    /**
     * modifica la estructura del año comparando lo que se tiene vs la lista nueva
     */
    public void modificarEstructura(int anio, List<Integer> grados) throws SQLException {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new NotFoundException("Año lectivo no encontrado");
        }
        if ("Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("La estructura de un año Cerrado no se modifica");
        }
        if (grados == null || grados.isEmpty()) {
            throw new IllegalArgumentException("Debe marcar al menos un grado");
        }
        if (gradoDAO.contarActivos(grados) != grados.size()) {
            throw new IllegalArgumentException("Hay grados inválidos o inactivos");
        }
        java.util.Set<Integer> nuevos = new java.util.HashSet<>(grados);
        java.util.Map<Integer, Integer> actuales = new java.util.HashMap<>();
        for (Map<String, Object> fila : estructuraDAO.listarPorAnio(anio)) {
            actuales.put((Integer) fila.get("idGrado"), (Integer) fila.get("idEstructura"));
        }
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            for (Map.Entry<Integer, Integer> e : actuales.entrySet()) {
                if (!nuevos.contains(e.getKey())) {
                    if (inscripcionDAO.existePorGradoDeAnio(anio, e.getKey())) {
                        throw new IllegalArgumentException(
                                "El grado tiene inscripciones y no se puede quitar");
                    }
                    curriculoDAO.eliminarPorEstructura(e.getValue());
                    estructuraDAO.eliminar(e.getValue());
                }
            }
            for (Integer idGrado : nuevos) {
                if (!actuales.containsKey(idGrado)) {
                    int nuevaEstructura = estructuraDAO.crear(anio, idGrado);
                    for (Integer idCurso : curriculoDAO.cursosActivosDeGradoDefecto(idGrado)) {
                        curriculoDAO.crear(nuevaEstructura, idCurso);
                    }
                }
            }
            cn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir estructura");
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

    public Map<String, Object> obtener(int anio) throws SQLException {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new NotFoundException("Año lectivo no encontrado");
        }
        Map<String, Object> detalle = new HashMap<>();
        detalle.put("anio", a.getAnio());
        detalle.put("fechaInicio", a.getFechaInicio() != null ? a.getFechaInicio().toString() : null);
        detalle.put("fechaCierre", a.getFechaCierre() != null ? a.getFechaCierre().toString() : null);
        detalle.put("estado", a.getEstado());
        detalle.put("estructura", estructuraDAO.listarPorAnio(anio));
        return detalle;
    }

    private void validarAnio(int anio) {
        if (anio < 2000 || anio > 2100) {
            throw new IllegalArgumentException("Año inválido");
        }
    }

    private void validarFechas(LocalDate inicio, LocalDate cierre) {
        if (inicio == null || cierre == null || !cierre.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha de cierre debe ser posterior al inicio");
        }
    }

    private void validarCoherencia(int anio, LocalDate inicio, LocalDate cierre) {
        if (inicio.getYear() != anio || cierre.getYear() != anio) {
            throw new IllegalArgumentException("Las fechas deben pertenecer al año " + anio);
        }
    }
}
