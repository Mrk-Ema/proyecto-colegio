package com.mycompany.backend.service;

import com.mycompany.backend.conexion.Conexion;
import com.mycompany.backend.dao.AnioLectivoDAO;
import com.mycompany.backend.dao.AsignacionDAO;
import com.mycompany.backend.dao.CurriculoDAO;
import com.mycompany.backend.dao.CursoDAO;
import com.mycompany.backend.dao.EstructuraDAO;
import com.mycompany.backend.dao.GradoDAO;
import com.mycompany.backend.dto.AnioLectivo;
import jakarta.ws.rs.NotFoundException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CurriculoService {

    private final GradoDAO gradoDAO = new GradoDAO();
    private final CursoDAO cursoDAO = new CursoDAO();
    private final EstructuraDAO estDAO = new EstructuraDAO();
    private final CurriculoDAO curDAO = new CurriculoDAO();
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAO();
    private final AsignacionDAO asigDAO = new AsignacionDAO();

    private int resolverEstructura(int idGrado, String contexto, boolean admiteCerrado) throws Exception {
        Map<String, Object> g = gradoDAO.buscarPorId(idGrado);
        if (g == null) {
            throw new NotFoundException("Grado no encontrado");
        }
        if (contexto == null || contexto.isBlank() || contexto.equals("defecto")) {
            Integer e = estDAO.buscarDefectoPorGrado(idGrado);
            return e == null ? -1 : e;
        }
        int anio = Integer.parseInt(contexto.trim());
        AnioLectivo a = anioDAO.buscarPorAnio(anio);
        if (a == null) {
            throw new NotFoundException("Año no encontrado");
        }
        if (!admiteCerrado && "Cerrado".equals(a.getEstado())) {
            throw new IllegalArgumentException("Año Cerrado: solo lectura");
        }
        Integer e = estDAO.buscarPorGradoYAnio(idGrado, anio);
        return e == null ? -1 : e;
    }

    public void crear(int idGrado, String contexto, List<Integer> cursos) throws Exception {
        validarLista(cursos);
        int e = resolverEstructura(idGrado, contexto, false);
        if (e != -1 && !curDAO.cursosDeEstructura(e).isEmpty()) {
            throw new IllegalArgumentException("Ya tiene currículo, debe modificarlo (CU036)");
        }
        if (cursoDAO.contarActivos(cursos) != cursos.size()) {
            throw new IllegalArgumentException("Hay cursos inválidos o inactivos");
        }
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            int est = e;
            if (est == -1) {
                if (contexto == null || contexto.isBlank() || contexto.equals("defecto")) {
                    est = estDAO.crearDefecto(idGrado);
                } else {
                    est = estDAO.crear(Integer.parseInt(contexto.trim()), idGrado);
                }
            }
            for (Integer c : new HashSet<>(cursos)) {
                curDAO.crear(est, c);
            }
            cn.commit();
        } catch (Exception ex) {
            try {
                cn.rollback();
            } catch (Exception x) {
                System.out.println("Error al revertir crearCurriculo");
                x.printStackTrace();
            }
            throw ex;
        } finally {
            try {
                cn.setAutoCommit(anterior);
            } catch (Exception x) {
                System.out.println("Error al restaurar autoCommit");
                x.printStackTrace();
            }
        }
    }

    public Map<String, Object> consultar(int idGrado, String contexto) throws Exception {
        int e = resolverEstructura(idGrado, contexto, true);
        if (e == -1) {
            throw new NotFoundException("Sin currículo, debe crearlo (CU034)");
        }
        Map<String, Object> r = new HashMap<>();
        r.put("cursos", curDAO.listarPorEstructura(e));
        return r;
    }

    public void modificar(int idGrado, String contexto, List<Integer> cursos) throws Exception {
        validarLista(cursos);
        int e = resolverEstructura(idGrado, contexto, false);
        if (e == -1) {
            throw new NotFoundException("Sin currículo, debe crearlo (CU034)");
        }
        Set<Integer> nuevos = new HashSet<>(cursos);
        List<Integer> actuales = curDAO.cursosDeEstructura(e);
        if (cursoDAO.contarActivos(new ArrayList<>(nuevos)) < nuevos.size()) {
            for (Integer c : nuevos) {
                if (!actuales.contains(c)) {
                    Map<String, Object> cu = cursoDAO.buscarPorId(c);
                    if (cu == null || !"Activo".equals(cu.get("estado"))) {
                        throw new IllegalArgumentException("Solo cursos activos para agregar");
                    }
                }
            }
        }
        boolean esAnio = contexto != null && !contexto.isBlank() && !contexto.equals("defecto");
        int anio = esAnio ? Integer.parseInt(contexto.trim()) : 0;
        Connection cn = Conexion.obtener();
        boolean anterior = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            for (Integer c : actuales) {
                if (!nuevos.contains(c)) {
                    if (esAnio && (asigDAO.cursoTieneMaestro(anio, idGrado, c)
                            || asigDAO.cursoTienePonderacion(anio, idGrado, c)
                            || asigDAO.cursoTieneNotas(anio, idGrado, c))) {
                        throw new IllegalArgumentException("El curso tiene maestro/ponderación/notas y se conserva");
                    }
                    curDAO.eliminar(e, c);
                }
            }
            for (Integer c : nuevos) {
                if (!actuales.contains(c) && !curDAO.existeEnEstructura(e, c)) {
                    curDAO.crear(e, c);
                }
            }
            cn.commit();
        } catch (Exception ex) {
            try {
                cn.rollback();
            } catch (Exception x) {
                System.out.println("Error al revertir modificarCurriculo");
                x.printStackTrace();
            }
            throw ex;
        } finally {
            try {
                cn.setAutoCommit(anterior);
            } catch (Exception x) {
                System.out.println("Error al restaurar autoCommit");
                x.printStackTrace();
            }
        }
    }

    private void validarLista(List<Integer> cursos) {
        if (cursos == null || cursos.isEmpty()) {
            throw new IllegalArgumentException("Marque al menos un curso");
        }
        if (new HashSet<>(cursos).size() != cursos.size()) {
            throw new IllegalArgumentException("Cursos repetidos");
        }
    }
}
