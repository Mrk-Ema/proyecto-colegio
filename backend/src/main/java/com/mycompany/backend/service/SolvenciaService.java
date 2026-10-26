package com.mycompany.backend.service;

import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.CuotaDAO;
import com.mycompany.backend.dao.EstudianteDAO;
import com.mycompany.backend.dao.InscripcionDAO;
import com.mycompany.backend.dao.PagoDAO;
import com.mycompany.backend.dto.AnioLectivo;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula si un estudiante está solvente en un año lectivo y, si lo está,
 * genera la solvencia en PDF con JasperReports.
 */
public class SolvenciaService {

    private final PagoDAO pagoDAO = new PagoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();
    private final CuotaDAO cuotaDAO = new CuotaDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();

    /**
     * Datos que el formulario necesita antes de calcular: el estudiante y los
     * años en los que está inscrito (solo los que no están Cerrados).
     */
    public Map<String, Object> contexto(String carnet) throws Exception {
        Map<String, Object> estudiante = buscarEstudiante(carnet);
        if (estudiante == null) {
            throw new IllegalArgumentException("No se encontró ningún estudiante con ese carnet");
        }
        int idEstudiante = (Integer) estudiante.get("idEstudiante");
        List<Integer> anios = new ArrayList<>();
        for (Map<String, Object> i : inscripcionDAO.historialPorEstudiante(idEstudiante)) {
            Integer anio = (Integer) i.get("anio");
            AnioLectivo a = anioDAO.buscarPorAnio(anio);
            if (a != null && !"Cerrado".equals(a.getEstado())) {
                anios.add(anio);
            }
        }
        Map<String, Object> r = new HashMap<>();
        r.put("estudiante", estudiante);
        r.put("anios", anios);
        r.put("porDefecto", anios.isEmpty() ? null : anios.get(0));
        return r;
    }

    /**
     * Calcula el saldo del estudiante en el año. Si el saldo es cero, se puede
     * generar la solvencia; si no, se devuelve el detalle de lo pendiente.
     */
    public Map<String, Object> calcular(String carnet, Integer anio) throws Exception {
        Map<String, Object> estudiante = buscarEstudiante(carnet);
        if (estudiante == null) {
            throw new IllegalArgumentException("No se encontró ningún estudiante con ese carnet");
        }
        if (!"Activo".equals(estudiante.get("estado"))) {
            throw new IllegalArgumentException("El estudiante no está Activo");
        }
        int idEstudiante = (Integer) estudiante.get("idEstudiante");
        if (anio == null || anio == 0) {
            throw new IllegalArgumentException("Selecciona el año lectivo");
        }
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }
        if ("Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("El año lectivo está Cerrado");
        }

        Map<String, Object> inscripcion = inscripcionDAO.buscarPorEstudianteYAnio(idEstudiante, anio);
        if (inscripcion == null) {
            throw new IllegalArgumentException("El estudiante no está inscrito en ese año lectivo");
        }
        int idInscripcion = (Integer) inscripcion.get("idInscripcion");
        int idGrado = (Integer) inscripcion.get("idGrado");
        String grado = (String) inscripcion.get("grado");
        String seccion = (String) inscripcion.get("seccion");

        Map<String, Object> cuota = cuotaDAO.buscarPorGradoYAnio(idGrado, anio);
        if (cuota == null) {
            throw new IllegalArgumentException("Ese grado no tiene cuotas configuradas para el año");
        }
        BigDecimal montoInscripcion = (BigDecimal) cuota.get("montoInscripcion");
        BigDecimal montoMensualidad = (BigDecimal) cuota.get("montoMensualidad");

        int mesesTranscurridos = mesesTranscurridos(a);
        BigDecimal adeudadoInscripcion = montoInscripcion;
        BigDecimal subtotalMensualidades = montoMensualidad.multiply(BigDecimal.valueOf(mesesTranscurridos));
        BigDecimal totalAdeudado = adeudadoInscripcion.add(subtotalMensualidades);

        BigDecimal totalPagado = pagoDAO.sumarPagosRegistrados(idInscripcion);
        BigDecimal saldo = totalAdeudado.subtract(totalPagado);
        if (saldo.signum() < 0) {
            saldo = BigDecimal.ZERO;
        }
        boolean puedeGenerar = saldo.compareTo(BigDecimal.ZERO) == 0;

        // Detalle de los pagos registrados de solo inscripción y mensualidad
        List<Map<String, Object>> pagos = new ArrayList<>();
        // Se reutiliza el listado de pagos filtrando por carnet y estado Registrado.
            List<Map<String, Object>> detallePagos = new ArrayList<>();
        for (Map<String, Object> p : new com.mycompany.backend.dao.PagoDAO().listar(
                (String) estudiante.get("carnet"), idEstudiante, null, "Registrado", null, null, 100, 0)) {
            if (Integer.parseInt(String.valueOf(p.get("idInscripcion"))) == idInscripcion) {
                Map<String, Object> d = new HashMap<>();
                d.put("numeroCorrelativo", p.get("numeroCorrelativo"));
                d.put("concepto", p.get("concepto"));
                d.put("mes", p.get("mes"));
                d.put("monto", p.get("monto"));
                d.put("fechaPago", p.get("fechaPago"));
                detallePagos.add(d);
            }
        }

        Map<String, Object> detalle = new HashMap<>();
        detalle.put("inscripcion", montoInscripcion);
        detalle.put("mesesTranscurridos", mesesTranscurridos);
        detalle.put("montoMensualidad", montoMensualidad);
        detalle.put("subtotalMensualidades", subtotalMensualidades);
        detalle.put("totalAdeudado", totalAdeudado);
        detalle.put("totalPagado", totalPagado);

        Map<String, Object> r = new HashMap<>();
        r.put("saldo", saldo);
        r.put("puedeGenerarSolvencia", puedeGenerar);
        r.put("detalle", detalle);
        r.put("pagos", detallePagos);
        r.put("estudiante", estudiante);
        r.put("anio", anio);
        r.put("grado", grado);
        r.put("seccion", seccion);
        r.put("carnet", estudiante.get("carnet"));
        r.put("nombres", estudiante.get("nombres"));
        r.put("apellidos", estudiante.get("apellidos"));
        return r;
    }

    /** Genera la solvencia en PDF si el saldo es cero. */
    public byte[] generarPdf(String carnet, Integer anio) throws Exception {
        Map<String, Object> calc = calcular(carnet, anio);
        if (!Boolean.TRUE.equals(calc.get("puedeGenerarSolvencia"))) {
            throw new IllegalArgumentException("El estudiante tiene un saldo pendiente de "
                    + calc.get("saldo") + ", no se puede generar la solvencia");
        }
        String carnetVal = String.valueOf(calc.get("carnet"));
        String estudiante = calc.get("nombres") + " " + calc.get("apellidos");
        String grado = String.valueOf(calc.get("grado"));
        String seccion = String.valueOf(calc.get("seccion"));
        String anioVal = String.valueOf(anio);

        Map<String, Object> params = new HashMap<>();
        params.put("carnet", carnetVal);
        params.put("estudiante", estudiante);
        params.put("grado", grado);
        params.put("seccion", seccion);
        params.put("anio", anioVal);
        params.put("fechaEmision", LocalDate.now().toString());

        try (java.io.InputStream in = getClass().getResourceAsStream("/comprobantes/solvencia.jrxml")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el reporte solvencia.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(in);
            net.sf.jasperreports.engine.JasperPrint print =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(
                            jasperReport, params, new net.sf.jasperreports.engine.JREmptyDataSource(1));
            return net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(print);
        }
    }

    private int mesesTranscurridos(AnioLectivo anio) {
        LocalDate fechaInicio = anio.getFechaInicio();
        LocalDate fechaCierre = anio.getFechaCierre();
        LocalDate hoy = LocalDate.now();
        if (fechaInicio == null || fechaCierre == null) {
            return 0;
        }
        LocalDate fin = hoy.isBefore(fechaCierre) ? hoy : fechaCierre;
        if (fin.isBefore(fechaInicio)) {
            return 0;
        }
        java.time.Period p = java.time.Period.between(fechaInicio, fin);
        int meses = p.getYears() * 12 + p.getMonths();
        if (p.getDays() > 0) {
            meses++;
        }
        return meses;
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
}