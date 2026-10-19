package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.ActividadDAO;
import com.mycompany.backend.dao.GradoDAO;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Actividades extracurriculares: nombre único en el catálogo, costo no negativo y
 * alcance de todo el colegio o de grados específicos. Un grado sale del alcance
 * únicamente cuando ninguno de sus estudiantes tiene un pago registrado.
 */
public class ActividadService {

    public static final String ALCANCE_TODOS = "Todo el colegio";
    public static final String ALCANCE_GRADOS = "Grados especificos";

    private final ActividadDAO dao = new ActividadDAO();
    private final GradoDAO gradoDAO = new GradoDAO();

    /** Listado paginado con los grados del alcance ya resueltos. */
    public Map<String, Object> listar(String nombre, String estado, int page, int size) throws SQLException {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        if (estado != null && !estado.isBlank()
                && !estado.equals("Activo") && !estado.equals("Inactivo")) {
            throw new IllegalArgumentException("Estado inválido");
        }
        int total = dao.contar(nombre, estado);
        List<Map<String, Object>> datos = dao.listar(nombre, estado, size, (page - 1) * size);
        for (Map<String, Object> m : datos) {
            m.put("grados", dao.listarGradosConNombre((Integer) m.get("idActividad")));
        }
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    /** Detalle de una actividad con los grados de su alcance. */
    public Map<String, Object> obtener(int idActividad) throws SQLException {
        Map<String, Object> a = dao.buscarPorId(idActividad);
        if (a == null) {
            throw new NotFoundException("Actividad no encontrada");
        }
        a.put("grados", dao.listarGradosConNombre(idActividad));
        return a;
    }

    /** Alta de la actividad y, si el alcance es por grados, de sus grados. */
    public Map<String, Object> crear(Map<String, Object> datos) throws Exception {
        String nombre = texto(datos.get("nombre"));
        String descripcion = textoONulo(datos.get("descripcion"));
        BigDecimal costo = monto(datos.get("costo"));
        String alcance = texto(datos.get("alcance"));
        List<Integer> grados = grados(datos.get("grados"));

        if (nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        validarAlcance(alcance, grados);
        if (dao.existeConNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe una actividad con ese nombre");
        }
        for (Integer idGrado : grados) {
            if (gradoDAO.buscarPorId(idGrado) == null) {
                throw new IllegalArgumentException("Uno de los grados seleccionados no existe");
            }
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            int id = dao.crear(nombre, descripcion, costo, alcance);
            if (ALCANCE_GRADOS.equals(alcance)) {
                dao.agregarGrados(id, grados);
            }
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("idActividad", id);
            r.put("mensaje", "Actividad creada");
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir la creación de la actividad");
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

    /** Edición de la actividad y ajuste de los grados del alcance. */
    public Map<String, Object> modificar(int idActividad, Map<String, Object> datos) throws Exception {
        Map<String, Object> actual = dao.buscarPorId(idActividad);
        if (actual == null) {
            throw new NotFoundException("Actividad no encontrada");
        }
        String nombre = texto(datos.get("nombre"));
        String descripcion = textoONulo(datos.get("descripcion"));
        BigDecimal costo = monto(datos.get("costo"));
        String alcance = texto(datos.get("alcance"));
        List<Integer> grados = grados(datos.get("grados"));

        if (nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre obligatorio");
        }
        validarAlcance(alcance, grados);
        if (dao.existeOtroConNombre(nombre, idActividad)) {
            throw new IllegalArgumentException("Ya existe una actividad con ese nombre");
        }
        for (Integer idGrado : grados) {
            if (gradoDAO.buscarPorId(idGrado) == null) {
                throw new IllegalArgumentException("Uno de los grados seleccionados no existe");
            }
        }

        List<Integer> actuales = dao.listarGrados(idActividad);
        List<Integer> aQuitar = new ArrayList<>();
        List<Integer> aAgregar = new ArrayList<>();
        if (!ALCANCE_GRADOS.equals(alcance)) {
            aQuitar.addAll(actuales);
        } else {
            for (Integer g : actuales) {
                if (!grados.contains(g)) {
                    aQuitar.add(g);
                }
            }
            for (Integer g : grados) {
                if (!actuales.contains(g)) {
                    aAgregar.add(g);
                }
            }
        }
        // Quitar un grado con pagos cobrados dejaría esos pagos fuera del alcance.
        for (Integer g : aQuitar) {
            if (dao.tienePagosRegistradosEnGrado(idActividad, g)) {
                throw new IllegalArgumentException("No se puede quitar un grado que ya tiene pagos registrados");
            }
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            dao.modificar(idActividad, nombre, descripcion, costo, alcance);
            dao.quitarGrados(idActividad, aQuitar);
            dao.agregarGrados(idActividad, aAgregar);
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("mensaje", "Actividad modificada");
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir la modificación de la actividad");
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

    /** Desactivar: deja de ofrecerse al cobrar, pero los pagos hechos se conservan. */
    public void desactivar(int idActividad) throws SQLException {
        Map<String, Object> actual = dao.buscarPorId(idActividad);
        if (actual == null) {
            throw new NotFoundException("Actividad no encontrada");
        }
        if (!"Activo".equals(actual.get("estado"))) {
            throw new IllegalArgumentException("Solo se desactiva una actividad Activa");
        }
        dao.actualizarEstado(idActividad, "Inactivo");
    }

    public void activar(int idActividad) throws SQLException {
        Map<String, Object> actual = dao.buscarPorId(idActividad);
        if (actual == null) {
            throw new NotFoundException("Actividad no encontrada");
        }
        if (!"Inactivo".equals(actual.get("estado"))) {
            throw new IllegalArgumentException("Solo se activa una actividad Inactiva");
        }
        dao.actualizarEstado(idActividad, "Activo");
    }

    private void validarAlcance(String alcance, List<Integer> grados) {
        if (!ALCANCE_TODOS.equals(alcance) && !ALCANCE_GRADOS.equals(alcance)) {
            throw new IllegalArgumentException("Alcance inválido");
        }
        if (ALCANCE_GRADOS.equals(alcance) && grados.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos un grado");
        }
    }

    private BigDecimal monto(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            throw new IllegalArgumentException("Costo obligatorio");
        }
        BigDecimal b;
        try {
            b = new BigDecimal(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Costo inválido");
        }
        if (b.signum() < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo");
        }
        if (b.scale() > 2) {
            throw new IllegalArgumentException("El costo admite solo dos decimales");
        }
        return b.setScale(2, RoundingMode.HALF_UP);
    }

    @SuppressWarnings("unchecked")
    private List<Integer> grados(Object v) {
        if (!(v instanceof List l)) {
            return List.of();
        }
        Set<Integer> sinRepetir = new LinkedHashSet<>();
        for (Object o : l) {
            if (o == null || String.valueOf(o).isBlank()) {
                continue;
            }
            try {
                sinRepetir.add(Integer.valueOf(String.valueOf(o).trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Grado inválido");
            }
        }
        return new ArrayList<>(sinRepetir);
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
}