package com.mycompany.backend.service;

import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.ReporteDAO;
import com.mycompany.backend.dto.AnioLectivo;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes del sistema: solventes/morosos, rendimiento, historial de pagos,
 * ingresos y préstamos en mora. Tabla + PDF con JasperReports
 */
public class ReporteService {

    private final ReporteDAO dao = new ReporteDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();

    /**
     * Reporte de alumnos solventes y morosos por año, grado y sección.
     * Calcula adeudado, pagado y saldo por estudiante.
     */
    public Map<String, Object> solventesMorosos(int anio, Integer grado, Integer seccion,
            int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }

        List<Map<String, Object>> crudos = dao.listarSolventesMorosos(anio, grado, seccion);
        int mesesTranscurridos = mesesTranscurridos(a);

        List<Map<String, Object>> filas = new ArrayList<>();
        for (Map<String, Object> c : crudos) {
            BigDecimal montoInscripcion = (BigDecimal) c.get("montoInscripcion");
            BigDecimal montoMensualidad = (BigDecimal) c.get("montoMensualidad");
            BigDecimal totalPagado = (BigDecimal) c.get("totalPagado");

            if (montoInscripcion == null) {
                montoInscripcion = BigDecimal.ZERO;
            }
            if (montoMensualidad == null) {
                montoMensualidad = BigDecimal.ZERO;
            }
            if (totalPagado == null) {
                totalPagado = BigDecimal.ZERO;
            }

            BigDecimal adeudado = montoInscripcion.add(
                    montoMensualidad.multiply(BigDecimal.valueOf(mesesTranscurridos)));
            BigDecimal saldo = adeudado.subtract(totalPagado);
            if (saldo.signum() < 0) {
                saldo = BigDecimal.ZERO;
            }
            String estado = saldo.compareTo(BigDecimal.ZERO) == 0 ? "Solvente" : "Moroso";

            Map<String, Object> fila = new HashMap<>();
            fila.put("idEstudiante", c.get("idEstudiante"));
            fila.put("carnet", c.get("carnet"));
            fila.put("nombres", c.get("nombres"));
            fila.put("apellidos", c.get("apellidos"));
            fila.put("grado", c.get("grado"));
            fila.put("seccion", c.get("seccion"));
            fila.put("nivel", c.get("nivel"));
            fila.put("carrera", c.get("carrera"));
            fila.put("montoInscripcion", montoInscripcion);
            fila.put("montoMensualidad", montoMensualidad);
            fila.put("mesesTranscurridos", mesesTranscurridos);
            fila.put("totalAdeudado", adeudado);
            fila.put("totalPagado", totalPagado);
            fila.put("saldo", saldo);
            fila.put("estado", estado);
            filas.add(fila);
        }

        // Paginación
        int total = filas.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Map<String, Object>> pagina = filas.subList(from, to);

        Map<String, Object> r = new HashMap<>();
        r.put("datos", pagina);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        r.put("mesesTranscurridos", mesesTranscurridos);
        r.put("anio", anio);
        return r;
    }

    /** Genera el PDF del reporte de solventes y morosos. */
    public byte[] solventesMorososPdf(int anio, Integer grado, Integer seccion) throws Exception {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }
        List<Map<String, Object>> crudos = dao.listarSolventesMorosos(anio, grado, seccion);
        int mesesTranscurridos = mesesTranscurridos(a);

        List<Map<String, Object>> filas = new ArrayList<>();
        for (Map<String, Object> c : crudos) {
            BigDecimal montoInscripcion = (BigDecimal) c.get("montoInscripcion");
            BigDecimal montoMensualidad = (BigDecimal) c.get("montoMensualidad");
            BigDecimal totalPagado = (BigDecimal) c.get("totalPagado");

            if (montoInscripcion == null) montoInscripcion = BigDecimal.ZERO;
            if (montoMensualidad == null) montoMensualidad = BigDecimal.ZERO;
            if (totalPagado == null) totalPagado = BigDecimal.ZERO;

            BigDecimal adeudado = montoInscripcion.add(
                    montoMensualidad.multiply(BigDecimal.valueOf(mesesTranscurridos)));
            BigDecimal saldo = adeudado.subtract(totalPagado);
            if (saldo.signum() < 0) saldo = BigDecimal.ZERO;
            String estado = saldo.compareTo(BigDecimal.ZERO) == 0 ? "Solvente" : "Moroso";

            Map<String, Object> fila = new HashMap<>();
            fila.put("carnet", c.get("carnet"));
            fila.put("estudiante", c.get("nombres") + " " + c.get("apellidos"));
            fila.put("grado", c.get("grado"));
            fila.put("seccion", c.get("seccion"));
            fila.put("inscripcion", montoInscripcion);
            fila.put("mensualidades", montoMensualidad.multiply(BigDecimal.valueOf(mesesTranscurridos)));
            fila.put("adeudado", adeudado);
            fila.put("pagado", totalPagado);
            fila.put("saldo", saldo);
            fila.put("estado", estado);
            filas.add(fila);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("anio", String.valueOf(anio));
        params.put("mesesTranscurridos", mesesTranscurridos);
        params.put("totalEstudiantes", filas.size());

        try (java.io.InputStream in = getClass().getResourceAsStream("/reportes/solventes-morosos.jrxml")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el reporte solventes-morosos.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(in);
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource =
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(filas);
            net.sf.jasperreports.engine.JasperPrint print =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, params, dataSource);
            return net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(print);
        }
    }

    //
    private int mesesTranscurridos(AnioLectivo anio) {
        java.time.LocalDate fechaInicio = anio.getFechaInicio();
        java.time.LocalDate fechaCierre = anio.getFechaCierre();
        java.time.LocalDate hoy = java.time.LocalDate.now();
        if (fechaInicio == null || fechaCierre == null) {
            return 0;
        }
        java.time.LocalDate fin = hoy.isBefore(fechaCierre) ? hoy : fechaCierre;
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

    /**
     * Reporte de rendimiento académico por grado y sección.
     * Calcula el promedio por curso, las materias con promedio más bajo
     * y el listado de mejores promedios.
     */
    public Map<String, Object> rendimiento(int anio, int idGrado, int idSeccion) throws Exception {
        // Verificar que el año existe
        com.mycompany.backend.dao.AnioLectivoDAO anioDAO = new com.mycompany.backend.dao.AnioLectivoDAO();
        com.mycompany.backend.dto.AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }

        List<Map<String, Object>> cursos = dao.listarCursosDeSeccion(anio, idSeccion);
        List<Map<String, Object>> estudiantes = dao.listarEstudiantesDeSeccion(anio, idSeccion);
        List<Map<String, Object>> notas = dao.listarNotasDeSeccion(anio, idSeccion);

        // Organizar notas por curso → estudiante → total
        // Map<idAsignacion, Map<idEstudiante, BigDecimal total>>
        Map<Integer, Map<Integer, BigDecimal>> notasPorCursoYEstudiante = new HashMap<>();
        // Map<idAsignacion, Map<idActividadEvaluable, BigDecimal puntos>>
        Map<Integer, Map<Integer, BigDecimal>> puntosPorCurso = new HashMap<>();

        for (Map<String, Object> n : notas) {
            int idAsignacion = (Integer) n.get("idAsignacion");
            int idEstudiante = (Integer) n.get("idEstudiante");
            BigDecimal valor = (BigDecimal) n.get("valor");
            int idActividad = (Integer) n.get("idActividadEvaluable");
            BigDecimal puntos = (BigDecimal) n.get("puntos");

            notasPorCursoYEstudiante
                    .computeIfAbsent(idAsignacion, k -> new HashMap<>())
                    .merge(idEstudiante, valor, BigDecimal::add);

            puntosPorCurso
                    .computeIfAbsent(idAsignacion, k -> new HashMap<>())
                    .putIfAbsent(idActividad, puntos);
        }

        // Calcular promedio por curso
        List<Map<String, Object>> promedioPorCurso = new ArrayList<>();
        for (Map<String, Object> cur : cursos) {
            int idAsignacion = (Integer) cur.get("idAsignacion");
            Map<Integer, BigDecimal> notasEstudiantes = notasPorCursoYEstudiante.get(idAsignacion);
            int totalEstudiantes = (notasEstudiantes != null) ? notasEstudiantes.size() : 0;

            BigDecimal sumaNotas = BigDecimal.ZERO;
            if (notasEstudiantes != null) {
                for (BigDecimal v : notasEstudiantes.values()) {
                    sumaNotas = sumaNotas.add(v);
                }
            }
            BigDecimal promedio = (totalEstudiantes > 0)
                    ? sumaNotas.divide(BigDecimal.valueOf(totalEstudiantes), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            Map<String, Object> m = new HashMap<>();
            m.put("idAsignacion", idAsignacion);
            m.put("idCurso", cur.get("idCurso"));
            m.put("curso", cur.get("curso"));
            m.put("promedio", promedio);
            m.put("totalEstudiantes", totalEstudiantes);
            promedioPorCurso.add(m);
        }

        // Ordenar por promedio ascendente (materias con promedio más bajo primero)
        promedioPorCurso.sort((x, y) -> ((BigDecimal) x.get("promedio")).compareTo((BigDecimal) y.get("promedio")));

        // Calcular promedio general por estudiante
        // Map<idEstudiante, BigDecimal totalNotas>
        Map<Integer, BigDecimal> totalPorEstudiante = new HashMap<>();
        Map<Integer, Integer> cursosPorEstudiante = new HashMap<>();
        Map<Integer, String> nombreEstudiante = new HashMap<>();
        Map<Integer, String> carnetEstudiante = new HashMap<>();

        for (Map<String, Object> est : estudiantes) {
            int idEst = (Integer) est.get("idEstudiante");
            nombreEstudiante.put(idEst, est.get("nombres") + " " + est.get("apellidos"));
            carnetEstudiante.put(idEst, (String) est.get("carnet"));
        }

        for (Map.Entry<Integer, Map<Integer, BigDecimal>> entry : notasPorCursoYEstudiante.entrySet()) {
            for (Map.Entry<Integer, BigDecimal> e : entry.getValue().entrySet()) {
                int idEst = e.getKey();
                totalPorEstudiante.merge(idEst, e.getValue(), BigDecimal::add);
                cursosPorEstudiante.merge(idEst, 1, Integer::sum);
            }
        }

        List<Map<String, Object>> mejoresPromedios = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : totalPorEstudiante.entrySet()) {
            int idEst = entry.getKey();
            int numCursos = cursosPorEstudiante.getOrDefault(idEst, 1);
            BigDecimal promedio = entry.getValue().divide(BigDecimal.valueOf(numCursos), 2, java.math.RoundingMode.HALF_UP);
            Map<String, Object> m = new HashMap<>();
            m.put("idEstudiante", idEst);
            m.put("carnet", carnetEstudiante.get(idEst));
            m.put("estudiante", nombreEstudiante.get(idEst));
            m.put("promedio", promedio);
            m.put("totalCursos", numCursos);
            mejoresPromedios.add(m);
        }
        // Ordenar por promedio descendente (mejores primero)
        mejoresPromedios.sort((x, y) -> ((BigDecimal) y.get("promedio")).compareTo((BigDecimal) x.get("promedio")));

        Map<String, Object> r = new HashMap<>();
        r.put("promedioPorCurso", promedioPorCurso);
        r.put("materiasBajas", promedioPorCurso); // ya ordenado ascendente
        r.put("mejoresPromedios", mejoresPromedios);
        r.put("totalEstudiantes", estudiantes.size());
        r.put("totalCursos", cursos.size());
        r.put("anio", anio);
        r.put("idGrado", idGrado);
        r.put("idSeccion", idSeccion);
        return r;
    }

    /** Genera el PDF del reporte de rendimiento. */
    public byte[] rendimientoPdf(int anio, int idGrado, int idSeccion) throws Exception {
        Map<String, Object> datos = rendimiento(anio, idGrado, idSeccion);

        List<Map<String, Object>> promedioPorCurso = (List<Map<String, Object>>) datos.get("promedioPorCurso");
        List<Map<String, Object>> mejoresPromedios = (List<Map<String, Object>>) datos.get("mejoresPromedios");

        // Preparar datos para el PDF (cursos)
        List<Map<String, Object>> filasCursos = new ArrayList<>();
        for (Map<String, Object> c : promedioPorCurso) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("curso", c.get("curso"));
            fila.put("promedio", c.get("promedio"));
            fila.put("totalEstudiantes", c.get("totalEstudiantes"));
            filasCursos.add(fila);
        }

        // Preparar datos para el PDF (mejores promedios)
        List<Map<String, Object>> filasEstudiantes = new ArrayList<>();
        for (Map<String, Object> e : mejoresPromedios) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("carnet", e.get("carnet"));
            fila.put("estudiante", e.get("estudiante"));
            fila.put("promedio", e.get("promedio"));
            filasEstudiantes.add(fila);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("anio", String.valueOf(anio));
        params.put("idGrado", idGrado);
        params.put("idSeccion", idSeccion);
        params.put("totalEstudiantes", datos.get("totalEstudiantes"));
        params.put("totalCursos", datos.get("totalCursos"));

        // We'll use a combined approach: first page courses, second page students
        // For simplicity, we'll create two separate data sources and use subreports
        // Actually, let's just use one data source with all data combined
        // Better approach: use two separate reports or a single report with two detail bands

        // For now, let's create a single PDF with both sections using a combined list
        List<Map<String, Object>> combined = new ArrayList<>();
        for (Map<String, Object> f : filasCursos) {
            Map<String, Object> row = new HashMap<>(f);
            row.put("tipo", "curso");
            combined.add(row);
        }
        for (Map<String, Object> f : filasEstudiantes) {
            Map<String, Object> row = new HashMap<>(f);
            row.put("tipo", "estudiante");
            combined.add(row);
        }

        try (java.io.InputStream in = getClass().getResourceAsStream("/reportes/rendimiento.jrxml")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el reporte rendimiento.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(in);
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource =
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(combined);
            net.sf.jasperreports.engine.JasperPrint print =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, params, dataSource);
            return net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(print);
        }
    }

    /**
     * Reporte de historial de pagos por estudiante.
     * Lista los pagos en el rango de fechas (o todos si no se indica rango)
     * y calcula los saldos pendientes actuales (inscripción + mensualidades).
     */
    public Map<String, Object> historialPagos(int idEstudiante, String desde, String hasta,
            int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        // Verify student exists
        com.mycompany.backend.dao.EstudianteDAO estudianteDAO = new com.mycompany.backend.dao.EstudianteDAO();
        Map<String, Object> estudiante = estudianteDAO.buscarPorId(idEstudiante);
        if (estudiante == null) {
            throw new IllegalArgumentException("Estudiante no encontrado");
        }

        int total = dao.contarHistorialPagos(idEstudiante, desde, hasta);
        List<Map<String, Object>> datos = dao.listarHistorialPagos(idEstudiante, desde, hasta,
                size, (page - 1) * size);

        // Calculate current balance (not filtered by date range)
        // Find the student's inscription in the active year
        int saldo = 0;
        Map<String, Object> saldoInfo = new HashMap<>();
        saldoInfo.put("totalAdeudado", 0);
        saldoInfo.put("totalPagado", 0);
        saldoInfo.put("saldo", 0);

        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        r.put("estudiante", estudiante);
        r.put("saldoPendiente", saldoInfo);
        return r;
    }

    /** Genera el PDF del reporte de historial de pagos. */
    public byte[] historialPagosPdf(int idEstudiante, String desde, String hasta) throws Exception {
        com.mycompany.backend.dao.EstudianteDAO estudianteDAO = new com.mycompany.backend.dao.EstudianteDAO();
        Map<String, Object> estudiante = estudianteDAO.buscarPorId(idEstudiante);
        if (estudiante == null) {
            throw new IllegalArgumentException("Estudiante no encontrado");
        }

        List<Map<String, Object>> pagos = dao.listarHistorialPagos(idEstudiante, desde, hasta, 1000, 0);

        List<Map<String, Object>> filas = new ArrayList<>();
        for (Map<String, Object> p : pagos) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("numeroCorrelativo", p.get("numeroCorrelativo"));
            fila.put("fechaPago", p.get("fechaPago"));
            fila.put("concepto", p.get("concepto"));
            fila.put("mes", p.get("mes") != null ? p.get("mes") : "-");
            fila.put("monto", p.get("monto"));
            fila.put("estado", p.get("estado"));
            fila.put("grado", p.get("grado"));
            fila.put("seccion", p.get("seccion"));
            filas.add(fila);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("carnet", estudiante.get("carnet"));
        params.put("estudiante", estudiante.get("nombres") + " " + estudiante.get("apellidos"));
        params.put("desde", desde != null ? desde : "Todos");
        params.put("hasta", hasta != null ? hasta : "Todos");
        params.put("totalPagos", filas.size());

        try (java.io.InputStream in = getClass().getResourceAsStream("/reportes/historial-pagos.jrxml")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el reporte historial-pagos.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(in);
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource =
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(filas);
            net.sf.jasperreports.engine.JasperPrint print =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, params, dataSource);
            return net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(print);
        }
    }

    /**
     * Reporte de ingresos: total bruto y desglose por concepto.
     * Solo pagos Registrados, sin multas de biblioteca.
     */
    public Map<String, Object> ingresos(String desde, String hasta) throws SQLException {
        List<Map<String, Object>> desglose = dao.listarIngresos(desde, hasta);

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        List<Map<String, Object>> conceptos = new ArrayList<>();
        String[] todos = {"Inscripcion", "Mensualidad", "Actividad"};
        Map<String, java.math.BigDecimal> montos = new HashMap<>();
        for (Map<String, Object> d : desglose) {
            montos.put(String.valueOf(d.get("concepto")), (java.math.BigDecimal) d.get("monto"));
        }
        for (String c : todos) {
            java.math.BigDecimal m = montos.getOrDefault(c, java.math.BigDecimal.ZERO);
            total = total.add(m);
            Map<String, Object> fila = new HashMap<>();
            fila.put("concepto", c.equals("Inscripcion") ? "Inscripción" : c.equals("Mensualidad") ? "Mensualidad" : "Actividad");
            fila.put("conceptoClave", c);
            fila.put("monto", m);
            conceptos.add(fila);
        }

        Map<String, Object> r = new HashMap<>();
        r.put("total", total);
        r.put("desglose", conceptos);
        r.put("desde", desde != null ? desde : "Todos");
        r.put("hasta", hasta != null ? hasta : "Todos");
        return r;
    }

    /** Genera el PDF del reporte de ingresos. */
    public byte[] ingresosPdf(String desde, String hasta) throws Exception {
        Map<String, Object> datos = ingresos(desde, hasta);

        List<Map<String, Object>> desglose = (List<Map<String, Object>>) datos.get("desglose");

        List<Map<String, Object>> filas = new ArrayList<>();
        for (Map<String, Object> d : desglose) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("concepto", d.get("concepto"));
            fila.put("monto", d.get("monto"));
            filas.add(fila);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("desde", datos.get("desde"));
        params.put("hasta", datos.get("hasta"));
        params.put("total", datos.get("total"));

        try (java.io.InputStream in = getClass().getResourceAsStream("/reportes/ingresos.jrxml")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el reporte ingresos.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(in);
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource =
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(filas);
            net.sf.jasperreports.engine.JasperPrint print =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, params, dataSource);
            return net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(print);
        }
    }

    /**
     * Reporte de préstamos en mora: préstamos Activos vencidos con días de
     * atraso y recargo calculado con los parámetros de biblioteca.
     */
    public Map<String, Object> prestamosEnMora(String desde, String hasta, Integer libro,
            int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        Map<String, Object> params = paramDAO();
        int diasPrestamo = (Integer) params.get("diasPrestamo");
        java.math.BigDecimal multaPorDia = (java.math.BigDecimal) params.get("multaPorDia");

        int total = dao.contarPrestamosEnMora(desde, hasta, libro);
        List<Map<String, Object>> crudos = dao.listarPrestamosEnMora(desde, hasta, libro, size, (page - 1) * size);

        java.time.LocalDate hoy = java.time.LocalDate.now();
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Map<String, Object> c : crudos) {
            java.time.LocalDate fechaEsperada = java.time.LocalDate.parse((String) c.get("fechaEsperada"));
            int diasAtraso = (int) java.time.temporal.ChronoUnit.DAYS.between(fechaEsperada, hoy);
            if (diasAtraso < 0) {
                diasAtraso = 0;
            }
            java.math.BigDecimal recargo = multaPorDia.multiply(java.math.BigDecimal.valueOf(diasAtraso));

            Map<String, Object> fila = new HashMap<>();
            fila.put("idPrestamo", c.get("idPrestamo"));
            fila.put("prestatario", c.get("prestatario"));
            fila.put("tipoPrestatario", c.get("tipoPrestatario"));
            fila.put("carnet", c.get("carnet"));
            fila.put("libro", c.get("libro"));
            fila.put("fechaPrestamo", c.get("fechaPrestamo"));
            fila.put("fechaEsperada", c.get("fechaEsperada"));
            fila.put("diasAtraso", diasAtraso);
            fila.put("recargo", recargo);
            filas.add(fila);
        }

        Map<String, Object> r = new HashMap<>();
        r.put("datos", filas);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        r.put("diasPrestamo", diasPrestamo);
        r.put("multaPorDia", multaPorDia);
        r.put("desde", desde != null ? desde : "Todos");
        r.put("hasta", hasta != null ? hasta : "Todos");
        return r;
    }

    /** Genera el PDF del reporte de préstamos en mora. */
    public byte[] prestamosEnMoraPdf(String desde, String hasta, Integer libro) throws Exception {
        Map<String, Object> datos = prestamosEnMora(desde, hasta, libro, 1, 10000);
        List<Map<String, Object>> filas = (List<Map<String, Object>>) datos.get("datos");

        List<Map<String, Object>> filasPdf = new ArrayList<>();
        for (Map<String, Object> f : filas) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("prestatario", f.get("prestatario"));
            fila.put("libro", f.get("libro"));
            fila.put("fechaEsperada", f.get("fechaEsperada"));
            fila.put("diasAtraso", f.get("diasAtraso"));
            fila.put("recargo", f.get("recargo"));
            filasPdf.add(fila);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("desde", datos.get("desde"));
        params.put("hasta", datos.get("hasta"));
        params.put("multaPorDia", datos.get("multaPorDia"));
        params.put("total", filas.size());

        try (java.io.InputStream in = getClass().getResourceAsStream("/reportes/prestamos-mora.jrxml")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el reporte prestamos-mora.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport =
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(in);
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource =
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(filasPdf);
            net.sf.jasperreports.engine.JasperPrint print =
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, params, dataSource);
            return net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(print);
        }
    }

    private Map<String, Object> paramDAO() throws SQLException {
        com.mycompany.backend.dao.ParametroBibliotecaDAO dao = new com.mycompany.backend.dao.ParametroBibliotecaDAO();
        Map<String, Object> p = dao.obtener();
        if (p == null) {
            throw new IllegalArgumentException("No hay parámetros de biblioteca configurados");
        }
        return p;
    }

    private String texto(Object v) {
        return v == null || "null".equals(String.valueOf(v)) ? "" : String.valueOf(v).trim();
    }
}