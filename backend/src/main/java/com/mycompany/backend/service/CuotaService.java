package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.CuotaDAO;
import com.mycompany.backend.dto.AnioLectivo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Montos de inscripción y mensualidad por grado y año lectivo.
 * Los montos no pueden ser negativos y los grados siempre tienen que
 * pertenecer a la estructura del año que se está configurando.
 */
public class CuotaService {

    private final CuotaDAO dao = new CuotaDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();

    /** Grados del año con los montos actuales, listos para editar en pantalla. */
    public List<Map<String, Object>> listar(int anio) throws SQLException {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }
        List<Map<String, Object>> grados = dao.listarGradosConCuota(anio);
        if (grados.isEmpty()) {
            throw new IllegalArgumentException("El año lectivo no tiene grados en su estructura, "
                    + "el Super Admin debe completarla");
        }
        return grados;
    }

    /** Registra o actualiza los montos de uno o varios grados del mismo año. */
    public Map<String, Object> guardar(Map<String, Object> datos) throws Exception {
        Integer anio = enteroONulo(datos.get("anio"));
        if (anio == null) {
            throw new IllegalArgumentException("Año lectivo obligatorio");
        }
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new IllegalArgumentException("Año lectivo no encontrado");
        }
        if ("Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("El año lectivo está Cerrado");
        }
        List<Map<String, Object>> enviados = lista(datos.get("cuotas"));
        if (enviados.isEmpty()) {
            throw new IllegalArgumentException("Indica al menos un monto");
        }

        Set<Integer> gradosDelAnio = new java.util.HashSet<>();
        for (Map<String, Object> g : dao.listarGradosConCuota(anio)) {
            gradosDelAnio.add((Integer) g.get("idGrado"));
        }
        if (gradosDelAnio.isEmpty()) {
            throw new IllegalArgumentException("El año lectivo no tiene grados en su estructura, "
                    + "el Super Admin debe completarla");
        }

        // Se valida todo antes de escribir para no dejar cuotas a medias.
        List<Object[]> porGuardar = new ArrayList<>();
        for (Map<String, Object> c : enviados) {
            int idGrado = entero(c.get("idGrado"));
            if (!gradosDelAnio.contains(idGrado)) {
                throw new IllegalArgumentException("Un grado seleccionado no pertenece al año lectivo");
            }
            BigDecimal inscripcion = monto(c.get("montoInscripcion"));
            BigDecimal mensualidad = monto(c.get("montoMensualidad"));
            porGuardar.add(new Object[] { idGrado, inscripcion, mensualidad });
        }

        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            int guardadas = 0;
            for (Object[] fila : porGuardar) {
                int idGrado = (Integer) fila[0];
                BigDecimal inscripcion = (BigDecimal) fila[1];
                BigDecimal mensualidad = (BigDecimal) fila[2];
                Integer idCuota = dao.buscarIdCuota(idGrado, anio);
                if (idCuota == null) {
                    dao.crear(anio, idGrado, inscripcion, mensualidad);
                } else {
                    dao.actualizar(idCuota, inscripcion, mensualidad);
                }
                guardadas++;
            }
            cn.commit();
            Map<String, Object> r = new HashMap<>();
            r.put("mensaje", "Cuotas guardadas");
            r.put("guardadas", guardadas);
            return r;
        } catch (SQLException | RuntimeException e) {
            try {
                cn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al revertir el guardado de cuotas");
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

    private BigDecimal monto(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            throw new IllegalArgumentException("Los montos son obligatorios");
        }
        BigDecimal b;
        try {
            b = new BigDecimal(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Monto inválido");
        }
        if (b.signum() < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        if (b.scale() > 2) {
            throw new IllegalArgumentException("El monto admite solo dos decimales");
        }
        return b.setScale(2, RoundingMode.HALF_UP);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> lista(Object v) {
        if (v instanceof List l) {
            return (List<Map<String, Object>>) l;
        }
        return List.of();
    }

    private int entero(Object v) {
        Integer i = enteroONulo(v);
        if (i == null) {
            throw new IllegalArgumentException("Grado obligatorio");
        }
        return i;
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