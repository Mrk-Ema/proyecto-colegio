package com.mycompany.backend.service;

import com.mycompany.backend.dao.CarreraDAO;
import jakarta.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarreraService {

    private final CarreraDAO dao = new CarreraDAO();

    public void crear(String nombre, String descripcion, Integer duracion) throws Exception {
        nombre = validar(nombre, duracion);
        if (dao.existePorNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe una carrera con ese nombre");
        }
        dao.crear(nombre, descripcion == null ? null : descripcion.trim(), duracion);
    }

    public Map<String, Object> listar(String nombre, String estado, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = dao.contar(nombre, estado);
        List<Map<String, Object>> datos = dao.listar(nombre, estado, size, (page - 1) * size);
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
            throw new NotFoundException("Carrera no encontrada");
        }
        return c;
    }

    public void modificar(int id, String nombre, String descripcion, Integer duracion) throws Exception {
        if (dao.buscarPorId(id) == null) {
            throw new NotFoundException("Carrera no encontrada");
        }
        nombre = validar(nombre, duracion);
        if (dao.existeOtroConNombre(id, nombre)) {
            throw new IllegalArgumentException("El nombre ya pertenece a otra carrera");
        }
        dao.modificar(id, nombre, descripcion == null ? null : descripcion.trim(), duracion);
    }

    public void desactivar(int id) throws Exception {
        Map<String, Object> c = dao.buscarPorId(id);
        if (c == null) {
            throw new NotFoundException("Carrera no encontrada");
        }
        if (!"Activo".equals(c.get("estado"))) {
            throw new IllegalArgumentException("Solo se desactiva en estado Activo");
        }
        if (dao.tieneInscritosEnActivo(id)) {
            throw new IllegalArgumentException("Tiene estudiantes inscritos en el año Activo");
        }
        dao.actualizarEstado(id, "Inactivo");
    }

    public void activar(int id) throws Exception {
        Map<String, Object> c = dao.buscarPorId(id);
        if (c == null) {
            throw new NotFoundException("Carrera no encontrada");
        }
        if (!"Inactivo".equals(c.get("estado"))) {
            throw new IllegalArgumentException("Solo se activa en estado Inactivo");
        }
        dao.actualizarEstado(id, "Activo");
    }

    private String validar(String nombre, Integer duracion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        if (duracion != null && duracion <= 0) {
            throw new IllegalArgumentException("Duración inválida");
        }
        return nombre.trim();
    }
}
