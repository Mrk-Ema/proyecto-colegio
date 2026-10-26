package com.mycompany.backend.service;

import com.mycompany.backend.dao.LibroDAO;
import jakarta.ws.rs.NotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Libros de la biblioteca: CRUD con título, autor, editorial y cantidad.
 * Al modificar la cantidad se recalculan los disponibles sin afectar los préstamos activos.
 */
public class LibroService {

    private final LibroDAO dao = new LibroDAO();

    public int contar(String titulo, String autor, String estado) throws SQLException {
        return dao.contar(titulo, autor, estado);
    }

    public Map<String, Object> listar(String titulo, String autor, String estado,
            int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = dao.contar(titulo, autor, estado);
        List<Map<String, Object>> datos = dao.listar(titulo, autor, estado, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public Map<String, Object> obtener(int id) throws SQLException {
        Map<String, Object> libro = dao.buscarPorId(id);
        if (libro == null) {
            throw new NotFoundException("Libro no encontrado");
        }
        return libro;
    }

    public Map<String, Object> crear(Map<String, Object> datos) throws Exception {
        String titulo = texto(datos.get("titulo"));
        String autor = textoONulo(datos.get("autor"));
        String editorial = textoONulo(datos.get("editorial"));
        Integer cantidad = entero(datos.get("cantidad"));
        if (titulo.isBlank()) {
            throw new IllegalArgumentException("Título obligatorio");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (dao.existePorTitulo(titulo)) {
            throw new IllegalArgumentException("Ya existe un libro con ese título");
        }
        int id = dao.crear(titulo, autor, editorial, cantidad);
        Map<String, Object> r = new HashMap<>();
        r.put("idLibro", id);
        r.put("mensaje", "Libro registrado");
        return r;
    }

    public Map<String, Object> modificar(int id, Map<String, Object> datos) throws Exception {
        Map<String, Object> actual = dao.buscarPorId(id);
        if (actual == null) {
            throw new NotFoundException("Libro no encontrado");
        }
        String titulo = texto(datos.get("titulo"));
        String autor = textoONulo(datos.get("autor"));
        String editorial = textoONulo(datos.get("editorial"));
        Integer cantidad = entero(datos.get("cantidad"));
        if (titulo.isBlank()) {
            throw new IllegalArgumentException("Título obligatorio");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (dao.existeOtroConTitulo(id, titulo)) {
            throw new IllegalArgumentException("Ya existe un libro con ese título");
        }
        // No se puede reducir la cantidad por debajo de los disponibles prestados
        int disponibles = (Integer) actual.get("cantidadDisponible");
        int prestados = (Integer) actual.get("cantidadTotal") - disponibles;
        if (cantidad < prestados) {
            throw new IllegalArgumentException("No se puede reducir la cantidad por debajo de los préstamos activos");
        }
        dao.modificar(id, titulo, autor, editorial, cantidad);
        Map<String, Object> r = new HashMap<>();
        r.put("mensaje", "Libro modificado");
        return r;
    }

    public void desactivar(int id) throws Exception {
        Map<String, Object> actual = dao.buscarPorId(id);
        if (actual == null) {
            throw new NotFoundException("Libro no encontrado");
        }
        if (!"Activo".equals(actual.get("estado"))) {
            throw new IllegalArgumentException("Solo se desactiva un libro Activo");
        }
        dao.actualizarEstado(id, "Inactivo");
    }

    public void activar(int id) throws Exception {
        Map<String, Object> actual = dao.buscarPorId(id);
        if (actual == null) {
            throw new NotFoundException("Libro no encontrado");
        }
        if (!"Inactivo".equals(actual.get("estado"))) {
            throw new IllegalArgumentException("Solo se activa un libro Inactivo");
        }
        dao.actualizarEstado(id, "Activo");
    }

    private String texto(Object v) {
        return v == null || "null".equals(String.valueOf(v)) ? "" : String.valueOf(v).trim();
    }

    private String textoONulo(Object v) {
        if (v == null || "null".equals(String.valueOf(v))) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private Integer entero(Object v) {
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