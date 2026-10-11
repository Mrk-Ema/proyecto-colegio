package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.CarreraDAO;
import com.mycompany.backend.dao.EstructuraDAO;
import com.mycompany.backend.dao.GradoDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.NivelDAO;
import com.mycompany.backend.dto.AnioLectivo;
import jakarta.ws.rs.NotFoundException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradoService {

    private final GradoDAO dao = new GradoDAO();
    private final NivelDAO nivelDAO = new NivelDAO();
    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final EstructuraDAO estructuraDAO = new EstructuraDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    public int crear(String nombre, Integer idNivel, Integer idCarrera, String destinoAnio) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        if (idNivel == null) {
            throw new IllegalArgumentException("Nivel obligatorio");
        }
        nombre = nombre.trim();
        if (!nivelDAO.existe(idNivel)) {
            throw new IllegalArgumentException("Nivel inválido");
        }
        boolean diversificado = nivelDAO.esDiversificado(idNivel);
        if (diversificado) {
            if (idCarrera == null) {
                throw new IllegalArgumentException("La carrera es obligatoria en Diversificado");
            }
            Map<String, Object> c = carreraDAO.buscarPorId(idCarrera);
            if (c == null) {
                throw new IllegalArgumentException("La carrera no existe");
            }
            if (!"Activo".equals(c.get("estado"))) {
                throw new IllegalArgumentException("La carrera está inactiva");
            }
        } else if (idCarrera != null) {
            throw new IllegalArgumentException("La carrera solo aplica a Diversificado");
        }
        if (dao.existePorNombreEnNivelCarrera(nombre, idNivel, idCarrera)) {
            throw new IllegalArgumentException("Ya existe un grado con ese nombre en el mismo nivel y carrera");
        }
        if (destinoAnio == null || destinoAnio.isBlank()) {
            return dao.crear(nombre, idNivel, idCarrera);
        }
        int idGrado = dao.crear(nombre, idNivel, idCarrera);
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            if (destinoAnio.equals("defecto")) {
                Integer est = estructuraDAO.buscarDefectoPorGrado(idGrado);
                if (est == null) {
                    estructuraDAO.crearDefecto(idGrado);
                }
            } else {
                int anio = Integer.parseInt(destinoAnio);
                AnioLectivo a = anioDAO.buscarPorAnio(anio);
                if (a == null) {
                    throw new IllegalArgumentException("Año lectivo no encontrado");
                }
                if ("Cerrado".equals(a.getEstado())) {
                    throw new IllegalArgumentException("No se agrega a un año Cerrado");
                }
                estructuraDAO.crear(anio, idGrado);
            }
            cn.commit();
            return idGrado;
        } catch (Exception e) {
            try {
                cn.rollback();
            } catch (Exception ex) {
                System.out.println("Error al revertir crearGrado");
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

    public Map<String, Object> listar(String nombre, Integer nivel, Integer carrera, String estado, String anio, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = dao.contar(nombre, nivel, carrera, estado, anio);
        List<Map<String, Object>> datos = dao.listar(nombre, nivel, carrera, estado, anio, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public Map<String, Object> obtener(int id) throws Exception {
        Map<String, Object> g = dao.buscarPorId(id);
        if (g == null) {
            throw new NotFoundException("Grado no encontrado");
        }
        return g;
    }

    public void modificar(int id, String nombre, Integer idNivel, Integer idCarrera) throws Exception {
        Map<String, Object> g = dao.buscarPorId(id);
        if (g == null) {
            throw new NotFoundException("Grado no encontrado");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        nombre = nombre.trim();
        boolean bloqueado = dao.tieneSecciones(id) || dao.tieneCurriculoDefecto(id);
        if (!bloqueado) {
            AnioLectivo activo = anioDAO.buscarActivo();
            if (activo != null && inscripcionDAO.existePorGradoDeAnio(activo.getAnio(), id)) {
                bloqueado = true;
            }
        }
        if (bloqueado) {
            if (dao.existeOtroConNombre(id, nombre, (Integer) g.get("idNivel"), (Integer) g.get("idCarrera"))) {
                throw new IllegalArgumentException("Ya existe un grado con ese nombre en el mismo nivel y carrera");
            }
            dao.modificar(id, nombre, 0, null, false);
            return;
        }
        if (idNivel == null) {
            throw new IllegalArgumentException("Nivel obligatorio");
        }
        if (!nivelDAO.existe(idNivel)) {
            throw new IllegalArgumentException("Nivel inválido");
        }
        boolean diversificado = nivelDAO.esDiversificado(idNivel);
        if (diversificado) {
            if (idCarrera == null) {
                throw new IllegalArgumentException("La carrera es obligatoria en Diversificado");
            }
            Map<String, Object> c = carreraDAO.buscarPorId(idCarrera);
            if (c == null) {
                throw new IllegalArgumentException("La carrera no existe");
            }
            if (!"Activo".equals(c.get("estado"))) {
                throw new IllegalArgumentException("La carrera está inactiva");
            }
        } else if (idCarrera != null) {
            throw new IllegalArgumentException("La carrera solo aplica a Diversificado");
        }
        if (dao.existeOtroConNombre(id, nombre, idNivel, idCarrera)) {
            throw new IllegalArgumentException("Ya existe un grado con ese nombre en el mismo nivel y carrera");
        }
        dao.modificar(id, nombre, idNivel, idCarrera, true);
    }

    public void desactivar(int id) throws Exception {
        Map<String, Object> g = dao.buscarPorId(id);
        if (g == null) {
            throw new NotFoundException("Grado no encontrado");
        }
        if (!"Activo".equals(g.get("estado"))) {
            throw new IllegalArgumentException("Solo se desactiva en estado Activo");
        }
        dao.actualizarEstado(id, "Inactivo");
    }

    public void activar(int id) throws Exception {
        Map<String, Object> g = dao.buscarPorId(id);
        if (g == null) {
            throw new NotFoundException("Grado no encontrado");
        }
        if (!"Inactivo".equals(g.get("estado"))) {
            throw new IllegalArgumentException("Solo se activa en estado Inactivo");
        }
        Object idCarrera = g.get("idCarrera");
        if (idCarrera != null) {
            Map<String, Object> c = carreraDAO.buscarPorId((Integer) idCarrera);
            if (c == null || !"Activo".equals(c.get("estado"))) {
                throw new IllegalArgumentException("Activa primero la carrera (CU033)");
            }
        }
        dao.actualizarEstado(id, "Activo");
    }
}
