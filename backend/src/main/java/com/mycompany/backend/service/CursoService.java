package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.CurriculoDAO;
import com.mycompany.backend.dao.CursoDAO;
import com.mycompany.backend.dao.EstructuraDAO;
import com.mycompany.backend.dao.GradoDAO;
import jakarta.ws.rs.NotFoundException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CursoService {

    private final CursoDAO dao = new CursoDAO();
    private final GradoDAO gradoDAO = new GradoDAO();
    private final EstructuraDAO estDAO = new EstructuraDAO();
    private final CurriculoDAO curDAO = new CurriculoDAO();

    public void crear(String nombre, List<Integer> grados) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        nombre = nombre.trim();
        if (dao.existePorNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe un curso con ese nombre");
        }
        if (grados == null || grados.isEmpty()) {
            dao.crear(nombre);
            return;
        }
        if (gradoDAO.contarActivos(grados) != grados.size()) {
            throw new IllegalArgumentException("Hay grados inválidos o inactivos");
        }
        int idCurso = dao.crear(nombre);
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            for (Integer idGrado : new HashSet<>(grados)) {
                Integer est = estDAO.buscarDefectoPorGrado(idGrado);
                if (est == null) {
                    est = estDAO.crearDefecto(idGrado);
                }
                if (!curDAO.existeEnEstructura(est, idCurso)) {
                    curDAO.crear(est, idCurso);
                }
            }
            cn.commit();
        } catch (Exception e) {
            try {
                cn.rollback();
            } catch (Exception ex) {
                System.out.println("Error al revertir crearCurso");
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                cn.setAutoCommit(anterior);
            } catch (Exception ex) {
                System.out.println("Error al restaurar autoCommit");
                ex.printStackTrace();
            }
        }
    }

    public Map<String, Object> listar(String nombre, String estado, Integer grado, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = dao.contar(nombre, estado, grado);
        List<Map<String, Object>> datos = dao.listar(nombre, estado, grado, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public Map<String, Object> obtener(int id) throws Exception {
        Map<String, Object> c = dao.buscarPorId(id);
        if (c == null) {
            throw new NotFoundException("Curso no encontrado");
        }
        return c;
    }

    public void modificar(int id, String nombre) throws Exception {
        if (dao.buscarPorId(id) == null) {
            throw new NotFoundException("Curso no encontrado");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        nombre = nombre.trim();
        if (dao.existeOtroConNombre(id, nombre)) {
            throw new IllegalArgumentException("El nombre ya pertenece a otro curso");
        }
        dao.modificar(id, nombre);
    }

    public void desactivar(int id) throws Exception {
        Map<String, Object> c = dao.buscarPorId(id);
        if (c == null) {
            throw new NotFoundException("Curso no encontrado");
        }
        if (!"Activo".equals(c.get("estado"))) {
            throw new IllegalArgumentException("Solo se desactiva en estado Activo");
        }
        dao.actualizarEstado(id, "Inactivo");
    }

    public void activar(int id) throws Exception {
        Map<String, Object> c = dao.buscarPorId(id);
        if (c == null) {
            throw new NotFoundException("Curso no encontrado");
        }
        if (!"Inactivo".equals(c.get("estado"))) {
            throw new IllegalArgumentException("Solo se activa en estado Inactivo");
        }
        dao.actualizarEstado(id, "Activo");
    }
}
