package com.mycompany.backend.resources;

import com.mycompany.backend.dao.EmpleadoDAO;
import com.mycompany.backend.service.AsignacionService;
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
 * POST /api/v1/asignaciones {anio, idSeccion, idCurso, idEmpleado}
 * GET /api/v1/asignaciones?page&size&anio&grado&seccion&curso&maestro&sinMaestro
 * GET /api/v1/asignaciones/maestros
 * PUT /api/v1/asignaciones/{id}/reasignar {idEmpleado}
 * PUT /api/v1/asignaciones/{id}/quitar
 */
@Path("asignaciones")
public class AsignacionResource {

    private final AsignacionService servicio = new AsignacionService();
    private final EmpleadoDAO empleados = new EmpleadoDAO();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("anio") Integer anio,
            @QueryParam("grado") Integer grado,
            @QueryParam("seccion") Integer seccion,
            @QueryParam("curso") Integer curso,
            @QueryParam("maestro") String maestro,
            @QueryParam("sinMaestro") @DefaultValue("false") boolean sinMaestro) {
        try {
            return Response.ok(servicio.listar(anio, grado, seccion, curso, maestro, sinMaestro, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar asignaciones");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("maestros")
    @Produces(MediaType.APPLICATION_JSON)
    public Response maestros() {
        try {
            return Response.ok(empleados.listarMaestrosActivos()).build();
        } catch (Exception e) {
            System.out.println("Error al listar maestros");
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
            servicio.crear(numero(datos.get("anio")), numero(datos.get("idSeccion")),
                    numero(datos.get("idCurso")), numero(datos.get("idEmpleado")));
            return Response.ok(Map.of("mensaje", "Asignación creada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al crear asignación");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/reasignar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reasignar(@PathParam("id") int id, Map<String, Object> datos) {
        try {
            servicio.reasignar(id, numero(datos.get("idEmpleado")));
            return Response.ok(Map.of("mensaje", "Maestro reasignado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al reasignar maestro");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/quitar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response quitar(@PathParam("id") int id) {
        try {
            servicio.quitar(id);
            return Response.ok(Map.of("mensaje", "Asignación quitada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al quitar asignación");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private int numero(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }
}
