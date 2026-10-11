package com.mycompany.backend.resources;

import com.mycompany.backend.dao.GradoDAO;
import com.mycompany.backend.service.GradoService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * GET /api/v1/grados  listar los grados activos (array simple para combos)
 * GET /api/v1/grados/paginado?page&size&nombre&nivel&carrera&estado&anio(defecto|id)
 * GET /api/v1/grados/{id}
 * POST /api/v1/grados {nombre, idNivel, idCarrera?, destinoAnio?(defecto|anio)}
 * PUT /api/v1/grados/{id} {nombre, idNivel?, idCarrera?}
 * PUT /api/v1/grados/{id}/desactivar
 * PUT /api/v1/grados/{id}/activar
 */
@Path("grados")
public class GradoResource {

    private final GradoDAO grados = new GradoDAO();
    private final GradoService servicio = new GradoService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarActivos() {
        try {
            return Response.ok(grados.listarActivos()).build();
        } catch (Exception e) {
            System.out.println("Error al listar grados");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("paginado")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("nombre") String nombre,
            @QueryParam("nivel") Integer nivel,
            @QueryParam("carrera") Integer carrera,
            @QueryParam("estado") String estado,
            @QueryParam("anio") String anio) {
        try {
            return Response.ok(servicio.listar(nombre, nivel, carrera, estado, anio, page, size)).build();
        } catch (Exception e) {
            System.out.println("Error al listar grados");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") int id) {
        try {
            return Response.ok(servicio.obtener(id)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar grado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Map<String, Object> datos) {
        try {
            String nombre = datos != null ? String.valueOf(datos.getOrDefault("nombre", "")) : null;
            Integer idNivel = entero(datos != null ? datos.get("idNivel") : null);
            Integer idCarrera = entero(datos != null ? datos.get("idCarrera") : null);
            Object dest = datos != null ? datos.get("destinoAnio") : null;
            String destinoAnio = dest == null || "null".equals(String.valueOf(dest)) ? null : String.valueOf(dest);
            if (destinoAnio != null && destinoAnio.isBlank()) {
                destinoAnio = null;
            }
            int id = servicio.crear(nombre, idNivel, idCarrera, destinoAnio);
            return Response.ok(Map.of("mensaje", "Grado creado, requiere currículo (CU034)", "idGrado", id)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al crear grado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificar(@PathParam("id") int id, Map<String, Object> datos) {
        try {
            String nombre = datos != null ? String.valueOf(datos.getOrDefault("nombre", "")) : null;
            Integer idNivel = entero(datos != null ? datos.get("idNivel") : null);
            Integer idCarrera = entero(datos != null ? datos.get("idCarrera") : null);
            servicio.modificar(id, nombre, idNivel, idCarrera);
            return Response.ok(Map.of("mensaje", "Grado modificado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar grado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivar(@PathParam("id") int id) {
        try {
            servicio.desactivar(id);
            return Response.ok(Map.of("mensaje", "Grado desactivado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al desactivar grado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/activar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activar(@PathParam("id") int id) {
        try {
            servicio.activar(id);
            return Response.ok(Map.of("mensaje", "Grado activado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al activar grado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private Integer entero(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v).trim());
    }
}
