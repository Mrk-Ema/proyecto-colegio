package com.mycompany.backend.service;

import com.mycompany.backend.dao.MultaDAO;
import com.mycompany.backend.dao.ParametroBibliotecaDAO;
import com.mycompany.backend.dao.PrestamoDAO;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Multas de biblioteca: listar, filtrar y registrar pago de multas Pendientes.
 */
public class MultaService {

    private final MultaDAO dao = new MultaDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ParametroBibliotecaDAO paramDAO = new ParametroBibliotecaDAO();

    public Map<String, Object> listar(Integer idPrestatario, String tipoPrestatario, String estado,
            String desde, String hasta, int page, int size) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = dao.contar(idPrestatario, tipoPrestatario, estado, desde, hasta);
        List<Map<String, Object>> datos = dao.listar(idPrestatario, tipoPrestatario, estado,
                desde, hasta, size, (page - 1) * size);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", datos);
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public Map<String, Object> obtener(int id) throws SQLException {
        Map<String, Object> m = dao.buscarPorId(id);
        if (m == null) {
            throw new NotFoundException("Multa no encontrada");
        }
        return m;
    }

    /** Registra el pago de una multa Pendiente por el monto exacto. */
    public Map<String, Object> registrarPago(int idMulta, int idUsuario) throws Exception {
        Map<String, Object> multa = dao.buscarPorId(idMulta);
        if (multa == null) {
            throw new NotFoundException("Multa no encontrada");
        }
        if (!"Pendiente".equals(multa.get("estado"))) {
            throw new IllegalArgumentException("La multa ya está Pagada");
        }
        BigDecimal monto = (BigDecimal) multa.get("monto");
        String correlativo = "MULTA-" + idMulta;
        dao.registrarPago(idMulta, correlativo, idUsuario);
        Map<String, Object> r = new HashMap<>();
        r.put("mensaje", "Pago de multa registrado");
        r.put("numeroCorrelativo", correlativo);
        r.put("monto", monto);
        return r;
    }
}