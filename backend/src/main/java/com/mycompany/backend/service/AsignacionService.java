package com.mycompany.backend.service;

import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.AsignacionDAO;
import com.mycompany.backend.dao.CurriculoDAO;
import com.mycompany.backend.dao.EmpleadoDAO;
import com.mycompany.backend.dao.EstructuraDAO;
import com.mycompany.backend.dao.SeccionDAO;
import com.mycompany.backend.dao.UsuarioDAO;
import com.mycompany.backend.dto.AnioLectivo;
import com.mycompany.backend.dto.Usuario;
import jakarta.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionService {

    private final AsignacionDAO dao = new AsignacionDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();
    private final SeccionDAO seccionDAO = new SeccionDAO();
    private final EstructuraDAO estDAO = new EstructuraDAO();
    private final CurriculoDAO curDAO = new CurriculoDAO();
    private final EmpleadoDAO empDAO = new EmpleadoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void crear(int anio, int idSeccion, int idCurso, int idEmpleado) throws Exception {
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new NotFoundException("Año no encontrado");
        }
        if ("Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("Año Cerrado: solo lectura");
        }
        Map<String, Object> s = seccionDAO.buscarPorId(idSeccion);
        if (s == null) {
            throw new IllegalArgumentException("Sección inválida");
        }
        if (!"Activo".equals(s.get("estado"))) {
            throw new IllegalArgumentException("Sección inactiva");
        }
        Integer idGrado = (Integer) s.get("idGrado");
        Integer est = estDAO.buscarPorGradoYAnio(idGrado, anio);
        if (est == null || !curDAO.existeEnEstructura(est, idCurso)) {
            throw new IllegalArgumentException("El curso no pertenece al currículo");
        }
        Map<String, Object> m = empDAO.buscarPorId(idEmpleado);
        if (m == null) {
            throw new IllegalArgumentException("Maestro inválido");
        }
        if (!"Activo".equals(m.get("estado"))) {
            throw new IllegalArgumentException("El maestro está inactivo");
        }
        validarEsMaestro(m);
        if (dao.existePorSeccionCursoAnio(anio, idSeccion, idCurso)) {
            throw new IllegalArgumentException("Ya tiene maestro, use reasignar (CU039)");
        }
        dao.crear(anio, idSeccion, idCurso, idEmpleado);
    }

    public Map<String, Object> listar(Integer anio, Integer grado, Integer seccion, Integer curso, String maestro, boolean sinMaestro, int page, int size) throws Exception {
        if (sinMaestro) {
            if (anio == null || seccion == null) {
                throw new IllegalArgumentException("sinMaestro exige año y sección");
            }
            List<Map<String, Object>> d = dao.listarSinMaestro(anio, seccion);
            Map<String, Object> r = new HashMap<>();
            r.put("datos", d);
            r.put("total", d.size());
            return r;
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int total = dao.contar(anio, grado, seccion, curso, maestro);
        Map<String, Object> r = new HashMap<>();
        r.put("datos", dao.listar(anio, grado, seccion, curso, maestro, size, (page - 1) * size));
        r.put("page", page);
        r.put("size", size);
        r.put("total", total);
        r.put("totalPaginas", (total + size - 1) / size);
        return r;
    }

    public void reasignar(int id, int nuevoEmpleado) throws Exception {
        Map<String, Object> a = dao.buscarPorId(id);
        if (a == null) {
            throw new NotFoundException("Asignación no encontrada");
        }
        AnioLectivo an = anioDAO.buscarPorAnio((Integer) a.get("anio"));
        if (an != null && "Cerrado".equals(an.getEstado())) {
            throw new IllegalArgumentException("Año Cerrado: solo lectura");
        }
        if (nuevoEmpleado == (Integer) a.get("idEmpleado")) {
            throw new IllegalArgumentException("Es el mismo maestro");
        }
        Map<String, Object> m = empDAO.buscarPorId(nuevoEmpleado);
        if (m == null) {
            throw new IllegalArgumentException("Maestro inválido");
        }
        if (!"Activo".equals(m.get("estado"))) {
            throw new IllegalArgumentException("El maestro está inactivo");
        }
        validarEsMaestro(m);
        dao.reasignar(id, nuevoEmpleado);
    }

    public void quitar(int id) throws Exception {
        Map<String, Object> a = dao.buscarPorId(id);
        if (a == null) {
            throw new NotFoundException("Asignación no encontrada");
        }
        int anio = (Integer) a.get("anio");
        AnioLectivo an = anioDAO.buscarPorAnio(anio);
        if (an != null && "Cerrado".equals(an.getEstado())) {
            throw new IllegalArgumentException("Año Cerrado: solo lectura");
        }
        if (dao.cursoTienePonderacion(anio, (Integer) a.get("idGrado"), (Integer) a.get("idCurso"))
                || dao.cursoTieneNotas(anio, (Integer) a.get("idGrado"), (Integer) a.get("idCurso"))) {
            throw new IllegalArgumentException("Tiene ponderación o notas, no se puede quitar");
        }
        dao.eliminar(id);
    }

    private void validarEsMaestro(Map<String, Object> empleado) throws Exception {
        Object puesto = empleado.get("puesto");
        if (puesto != null && String.valueOf(puesto).toLowerCase().contains("maestr")) {
            return;
        }
        Object idUsuario = empleado.get("idUsuario");
        if (idUsuario instanceof Number n) {
            Usuario u = usuarioDAO.buscarPorId(n.intValue());
            if (u != null && "Maestro".equals(u.getRol()) && "Activo".equals(u.getEstado())) {
                return;
            }
        }
        throw new IllegalArgumentException("No es un maestro, solo maestros activos");
    }
}
