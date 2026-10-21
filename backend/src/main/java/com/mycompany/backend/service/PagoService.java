package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.ActividadDAO;
import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.CuotaDAO;
import com.mycompany.backend.dao.EstudianteDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.PagoDAO;
import com.mycompany.backend.dto.AnioLectivo;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registro de pagos de los estudiantes. El monto nunca se escribe a mano:
 * sale de la cuota del grado o del costo de la actividad, y solo una
 * Secretaria puede registrar y anular pagos.
 */
public class PagoService {

    public static final String CONCEPTO_INSCRIPCION = "Inscripcion";
    public static final String CONCEPTO_MENSUALIDAD = "Mensualidad";
    public static final String CONCEPTO_ACTIVIDAD = "Actividad";

    private static final List<String> MESES = List.of("Enero", "Febrero", "Marzo", "Abril", "Mayo",
            "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

    private final PagoDAO dao = new PagoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();
    private final CuotaDAO cuotaDAO = new CuotaDAO();
    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();

    /**
     * Datos que el formulario necesita para cobrar, el estudiante, su inscripción
     * del año, los montos de su grado y las actividades que le tocan.
     */
    public Map<String, Object> contexto(String carnet, Integer anio) throws Exception {
        Map<String, Object> estudiante = buscarEstudiante(carnet);
        if (estudiante == null) {
            throw new IllegalArgumentException("No se encontró ningún estudiante con ese carné");
        }
        if (!"Activo".equals(estudiante.get("estado"))) {
            throw new IllegalArgumentException("El estudiante no está Activo");
        }
        int idEstudiante = (Integer) estudiante.get("idEstudiante");
        List<Integer> anios = aniosDeEstudiante(idEstudiante);
        Integer anioElegido = anio != null ? anio : (anios.isEmpty() ? null : anios.get(0));

        Map<String, Object> r = new HashMap<>();
        r.put("estudiante", estudiante);
        r.put("anios", anios);
        r.put("anio", anioElegido);
        if (anioElegido == null) {
            r.put("inscripcion", null);
            r.put("cuota", null);
            r.put("actividades", List.of());
            return r;
        }
        AnioLectivo a = anioDAO.buscarPorAnio(anioElegido);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }
        Map<String, Object> inscripcion = inscripcionDAO.buscarPorEstudianteYAnio(idEstudiante, anioElegido);
        r.put("inscripcion", inscripcion);
        r.put("cuota", inscripcion == null ? null
                : cuotaDAO.buscarPorGradoYAnio((Integer) inscripcion.get("idGrado"), anioElegido));
        r.put("actividades", inscripcion == null ? List.of()
                : actividadDAO.listarActivasParaGrado((Integer) inscripcion.get("idGrado")));
        return r;
    }

    /** Registro de un pago de inscripción o de mensualidad. */
    public Map<String, Object> registrar(Map<String, Object> datos, int idUsuario) throws Exception {
        String concepto = texto(datos.get("concepto"));
        if (!CONCEPTO_INSCRIPCION.equals(concepto) && !CONCEPTO_MENSUALIDAD.equals(concepto)) {
            throw new IllegalArgumentException("Concepto inválido");
        }
        Map<String, Object> ctx = contexto(texto(datos.get("carnet")), enteroONulo(datos.get("anio")));
        Map<String, Object> inscripcion = (Map<String, Object>) ctx.get("inscripcion");
        if (inscripcion == null) {
            throw new IllegalArgumentException("El estudiante no está inscrito en ese año lectivo");
        }
        Map<String, Object> cuota = (Map<String, Object>) ctx.get("cuota");
        if (cuota == null) {
            throw new IllegalArgumentException("Ese grado no tiene cuotas configuradas para el año");
        }
        int idInscripcion = (Integer) inscripcion.get("idInscripcion");

        String mes = null;
        BigDecimal monto;
        if (CONCEPTO_INSCRIPCION.equals(concepto)) {
            if (dao.existePagoDeConcepto(idInscripcion, CONCEPTO_INSCRIPCION)) {
                throw new IllegalArgumentException("Ya existe un pago de inscripción registrado");
            }
            monto = (BigDecimal) cuota.get("montoInscripcion");
        } else {
            mes = texto(datos.get("mes"));
            if (mes.isEmpty()) {
                throw new IllegalArgumentException("El mes es obligatorio para la mensualidad");
            }
            if (!MESES.contains(mes)) {
                throw new IllegalArgumentException("Mes inválido");
            }
            if (dao.existePagoDelMes(idInscripcion, mes)) {
                throw new IllegalArgumentException("Ese mes ya está pagado");
            }
            monto = (BigDecimal) cuota.get("montoMensualidad");
        }
        if (monto == null) {
            throw new IllegalArgumentException("El monto de la cuota no está configurado");
        }
        return guardar(idInscripcion, null, concepto, mes, monto, idUsuario);
    }

    /** Registro del pago de una actividad extracurricular. */
    public Map<String, Object> registrarActividad(Map<String, Object> datos, int idUsuario)
            throws Exception {
        Integer idActividad = enteroONulo(datos.get("idActividad"));
        if (idActividad == null) {
            throw new IllegalArgumentException("Selecciona la actividad");
        }
        Map<String, Object> ctx = contexto(texto(datos.get("carnet")), enteroONulo(datos.get("anio")));
        Map<String, Object> inscripcion = (Map<String, Object>) ctx.get("inscripcion");
        if (inscripcion == null) {
            throw new IllegalArgumentException("El estudiante no está inscrito en ese año lectivo");
        }
        int idInscripcion = (Integer) inscripcion.get("idInscripcion");
        int idGrado = (Integer) inscripcion.get("idGrado");

        Map<String, Object> actividad = null;
        for (Map<String, Object> a : actividadDAO.listarActivasParaGrado(idGrado)) {
            if (((Integer) a.get("idActividad")) == idActividad) {
                actividad = a;
                break;
            }
        }
        if (actividad == null) {
            throw new IllegalArgumentException("La actividad no está disponible para ese grado");
        }
        if (dao.existePagoDeActividad(idInscripcion, idActividad)) {
            throw new IllegalArgumentException("Esa actividad ya tiene un pago registrado");
        }
        BigDecimal costo = (BigDecimal) actividad.get("costo");
        if (costo == null) {
            throw new IllegalArgumentException("La actividad no tiene costo configurado");
        }
        return guardar(idInscripcion, idActividad, CONCEPTO_ACTIVIDAD, null, costo, idUsuario);
    }

    /** Listado de los pagos de un estudiante, con filtros opcionales. */
    public Map<String, Object> listar(String carnet, Integer idEstudiante, String concepto,
            String estado, String desde, String hasta, int page, int size) throws Exception {
        if ((carnet == null || carnet.isBlank()) && idEstudiante == null) {
            throw new IllegalArgumentException("Ingresa el ID o carné del estudiante");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        if (concepto != null && !concepto.isBlank() && !CONCEPTO_INSCRIPCION.equals(concepto)
                && !CONCEPTO_MENSUALIDAD.equals(concepto) && !CONCEPTO_ACTIVIDAD.equals(concepto)) {
            throw new IllegalArgumentException("Concepto inválido");
        }
        if (estado != null && !estado.isBlank() && !"Registrado".equals(estado)
                && !"Anulado".equals(estado)) {
            throw new IllegalArgumentException("Estado inválido");
        }
        int total = dao.contar(carnet, idEstudiante, concepto, estado, desde, hasta);
        List<Map<String, Object>> datos = dao.listar(carnet, idEstudiante, concepto, estado,
                desde, hasta, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    /** Anulación, el registro y su número correlativo se conservan. */
    public void anular(int idPago, String motivo, int idUsuario) throws Exception {
        Map<String, Object> pago = dao.buscarPorId(idPago);
        if (pago == null) {
            throw new NotFoundException("Pago no encontrado");
        }
        if (!"Registrado".equals(pago.get("estado"))) {
            throw new IllegalArgumentException("El pago ya está Anulado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la anulación es obligatorio");
        }
        dao.anular(idPago, motivo.trim(), idUsuario);
    }

    /** Detalle de un pago, para el comprobante. */
    public Map<String, Object> obtener(int idPago) throws SQLException {
        Map<String, Object> pago = dao.buscarPorId(idPago);
        if (pago == null) {
            throw new NotFoundException("Pago no encontrado");
        }
        return pago;
    }

    private Map<String, Object> guardar(int idInscripcion, Integer idActividad, String concepto,
            String mes, BigDecimal monto, int idUsuario) throws Exception {
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            int idPago = dao.crear(idInscripcion, idActividad, concepto, mes, monto, idUsuario);
            String correlativo = "PAG-" + idPago;
            dao.asignarCorrelativo(idPago, correlativo);
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("idPago", idPago);
            r.put("numeroCorrelativo", correlativo);
            r.put("mensaje", "Pago registrado");
            return r;
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir el registro del pago");
                ex.printStackTrace();
            }
            throw new IllegalArgumentException("Ese pago ya estaba registrado");
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir el registro del pago");
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

    private List<Integer> aniosDeEstudiante(int idEstudiante) throws SQLException {
        List<Integer> anios = new ArrayList<>();
        for (Map<String, Object> i : inscripcionDAO.historialPorEstudiante(idEstudiante)) {
            Integer anio = (Integer) i.get("anio");
            AnioLectivo a = anioDAO.buscarPorAnio(anio);
            if (a != null && !"Cerrado".equals(a.getEstado())) {
                anios.add(anio);
            }
        }
        return anios;
    }

    private Map<String, Object> buscarEstudiante(String carnet) throws SQLException {
        if (carnet == null || carnet.isBlank()) {
            return null;
        }
        if (carnet.chars().allMatch(Character::isDigit)) {
            Map<String, Object> porId = estudianteDAO.buscarPorId(Integer.parseInt(carnet));
            if (porId != null) {
                return porId;
            }
        }
        return estudianteDAO.buscarPorCarne(carnet.trim());
    }

    private String texto(Object v) {
        return v == null || "null".equals(String.valueOf(v)) ? "" : String.valueOf(v).trim();
    }

    private Integer enteroONulo(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido");
        }
    }
}
