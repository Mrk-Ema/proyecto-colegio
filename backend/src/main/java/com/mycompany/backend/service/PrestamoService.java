package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.LibroDAO;
import com.mycompany.backend.dao.MultaDAO;
import com.mycompany.backend.dao.ParametroBibliotecaDAO;
import com.mycompany.backend.dao.PrestamoDAO;
import com.mycompany.backend.dao.EstudianteDAO;
import com.mycompany.backend.dao.EmpleadoDAO;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Préstamos de libros: registrar préstamo, devolución con cálculo de multa, y listado.
 */
public class PrestamoService {

    private final PrestamoDAO dao = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final ParametroBibliotecaDAO paramDAO = new ParametroBibliotecaDAO();
    private final MultaDAO multaDAO = new MultaDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    /** Registra un préstamo de un libro Activo a un estudiante o maestro. */
    public Map<String, Object> registrar(Map<String, Object> datos) throws Exception {
        int idLibro = entero(datos.get("idLibro"));
        String tipo = texto(datos.get("tipoPrestatario"));
        int idPrestatario = entero(datos.get("idPrestatario"));

        Map<String, Object> libro = libroDAO.buscarPorId(idLibro);
        if (libro == null) {
            throw new NotFoundException("Libro no encontrado");
        }
        if (!"Activo".equals(libro.get("estado"))) {
            throw new IllegalArgumentException("El libro está Inactivo, no se puede prestar");
        }
        int disponibles = (Integer) libro.get("cantidadDisponible");
        if (disponibles <= 0) {
            throw new IllegalArgumentException("El libro no tiene ejemplares disponibles");
        }

        // Validar que el prestatario existe
        Integer idEstudiante = null;
        Integer idEmpleado = null;
        if ("estudiante".equalsIgnoreCase(tipo)) {
            Map<String, Object> est = estudianteDAO.buscarPorId(idPrestatario);
            if (est == null) {
                throw new IllegalArgumentException("El estudiante no existe");
            }
            idEstudiante = idPrestatario;
        } else if ("maestro".equalsIgnoreCase(tipo)) {
            Map<String, Object> emp = empleadoDAO.buscarPorId(idPrestatario);
            if (emp == null) {
                throw new IllegalArgumentException("El empleado no existe");
            }
            if (!"Activo".equals(emp.get("estado"))) {
                throw new IllegalArgumentException("El empleado no está Activo");
            }
            idEmpleado = idPrestatario;
        } else {
            throw new IllegalArgumentException("Tipo de prestatario inválido (estudiante o maestro)");
        }

        // Verificar que no tenga un préstamo activo del mismo libro
        if (dao.existePrestamoActivo(idLibro, idEstudiante, idEmpleado)) {
            throw new IllegalArgumentException("El prestatario ya tiene un préstamo activo de este libro");
        }

        // Obtener parámetros
        Map<String, Object> params = paramDAO.obtener();
        if (params == null) {
            throw new IllegalArgumentException("No hay parámetros de biblioteca configurados");
        }
        int diasPrestamo = (Integer) params.get("diasPrestamo");

        LocalDate fechaPrestamo = LocalDate.now();
        LocalDate fechaEsperada = fechaPrestamo.plusDays(diasPrestamo);

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            int idPrestamo = dao.crear(idLibro, fechaPrestamo, fechaEsperada, idEstudiante, idEmpleado);
            dao.decrementarDisponible(idLibro);
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("idPrestamo", idPrestamo);
            r.put("mensaje", "Préstamo registrado");
            r.put("fechaEsperada", fechaEsperada.toString());
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir el préstamo");
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

    /** Registra la devolución de un préstamo activo, calcula atraso y multa si aplica. */
    public Map<String, Object> devolver(int idPrestamo) throws Exception {
        Map<String, Object> prestamo = dao.buscarPorId(idPrestamo);
        if (prestamo == null) {
            throw new NotFoundException("Préstamo no encontrado");
        }
        if (!"Activo".equals(prestamo.get("estado"))) {
            throw new IllegalArgumentException("El préstamo ya está Devuelto");
        }
        int idLibro = (Integer) prestamo.get("idLibro");
        LocalDate fechaEsperada = LocalDate.parse((String) prestamo.get("fechaEsperada"));
        LocalDate fechaDevolucion = LocalDate.now();

        int diasAtraso = 0;
        if (fechaDevolucion.isAfter(fechaEsperada)) {
            diasAtraso = (int) java.time.temporal.ChronoUnit.DAYS.between(fechaEsperada, fechaDevolucion);
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            dao.registrarDevolucion(idPrestamo, fechaDevolucion);
            dao.incrementarDisponible(idLibro);

            Map<String, Object> params = paramDAO.obtener();
            BigDecimal multaPorDia = (BigDecimal) params.get("multaPorDia");
            BigDecimal montoMulta = BigDecimal.ZERO;
            if (diasAtraso > 0) {
                montoMulta = multaPorDia.multiply(BigDecimal.valueOf(diasAtraso));
                int idMulta = multaDAO.crear(idPrestamo, diasAtraso, montoMulta);
                Map<String, Object> r = new HashMap<>();
                r.put("idMulta", idMulta);
                r.put("diasAtraso", diasAtraso);
                r.put("montoMulta", montoMulta);
                r.put("mensaje", "Devolución registrada con multa de " + montoMulta);
                cn.commit();
                return r;
            }
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("mensaje", "Devolución registrada sin atraso");
            r.put("diasAtraso", 0);
            r.put("montoMulta", BigDecimal.ZERO);
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir la devolución");
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

    public Map<String, Object> listar(Integer libro, Integer idPrestatario, String tipoPrestatario,
            String estado, String desde, String hasta, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = dao.contar(libro, idPrestatario, tipoPrestatario, estado, desde, hasta);
        List<Map<String, Object>> datos = dao.listar(libro, idPrestatario, tipoPrestatario,
                estado, desde, hasta, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public Map<String, Object> obtener(int id) throws SQLException {
        Map<String, Object> p = dao.buscarPorId(id);
        if (p == null) {
            throw new NotFoundException("Préstamo no encontrado");
        }
        return p;
    }

    private String texto(Object v) {
        return v == null || "null".equals(String.valueOf(v)) ? "" : String.valueOf(v).trim();
    }

    private int entero(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }
}