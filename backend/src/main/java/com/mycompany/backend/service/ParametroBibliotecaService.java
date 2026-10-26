package com.mycompany.backend.service;

import com.mycompany.backend.dao.ParametroBibliotecaDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parámetros de la biblioteca: días de préstamo y multa por día.
 * Solo existe un registro global; si no hay, se crea; si ya hay, se actualiza.
 */
public class ParametroBibliotecaService {

    private final ParametroBibliotecaDAO dao = new ParametroBibliotecaDAO();

    public Map<String, Object> obtener() throws SQLException {
        Map<String, Object> p = dao.obtener();
        if (p == null) {
            // Crear con valores por defecto si no existe
            dao.crear(7, new BigDecimal("5.00"));
            p = dao.obtener();
        }
        return p;
    }

    public Map<String, Object> guardar(Map<String, Object> datos) throws Exception {
        Object dias = datos.get("diasPrestamo");
        Object multa = datos.get("multaPorDia");
        if (dias == null || String.valueOf(dias).isBlank()) {
            throw new IllegalArgumentException("Los días de préstamo son obligatorios");
        }
        if (multa == null || String.valueOf(multa).isBlank()) {
            throw new IllegalArgumentException("La multa por día es obligatoria");
        }
        int diasInt;
        try {
            diasInt = Integer.parseInt(String.valueOf(dias).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los días de préstamo deben ser un número");
        }
        if (diasInt <= 0) {
            throw new IllegalArgumentException("Los días de préstamo deben ser mayores a cero");
        }
        BigDecimal multaBd;
        try {
            multaBd = new BigDecimal(String.valueOf(multa).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La multa por día es inválida");
        }
        if (multaBd.signum() < 0) {
            throw new IllegalArgumentException("La multa por día no puede ser negativa");
        }
        if (multaBd.scale() > 2) {
            throw new IllegalArgumentException("La multa admite solo dos decimales");
        }
        multaBd = multaBd.setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> existente = dao.obtener();
        if (existente == null) {
            dao.crear(diasInt, multaBd);
        } else {
            dao.actualizar(diasInt, multaBd);
        }
        Map<String, Object> r = new HashMap<>();
        r.put("mensaje", "Parámetros guardados");
        r.put("diasPrestamo", diasInt);
        r.put("multaPorDia", multaBd);
        return r;
    }
}