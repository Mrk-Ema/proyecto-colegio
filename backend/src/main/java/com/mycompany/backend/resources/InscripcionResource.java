package com.mycompany.backend.resources;

import com.mycompany.backend.service.InscripcionService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * POST /api/v1/inscripciones                         inscribir estudiante 
 * GET  /api/v1/inscripciones?page&size&anio&nivel&grado&seccion&estudiante   
 * PUT  /api/v1/inscripciones/{id}                    cambiar sección 
 * GET  /api/v1/inscripciones/{id}                    detalle 
 * GET  /api/v1/inscripciones/anios                   años no cerrados para el formulario
 * GET  /api/v1/inscripciones/grados?anio             grados de la estructura del año
 * GET  /api/v1/inscripciones/secciones?grado         secciones Activas del grado
 * GET  /api/v1/inscripciones/niveles                 niveles para filtrar el listado
 */
@Path("inscripciones")
public class InscripcionResource {

    private final InscripcionService servicio = new InscripcionService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.crear(datos)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al inscribir estudiante");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("anio") Integer anio,
            @QueryParam("nivel") Integer nivel,
            @QueryParam("grado") Integer grado,
            @QueryParam("seccion") Integer seccion,
            @QueryParam("estudiante") String estudiante,
            @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.listar(anio, nivel, grado, seccion, estudiante, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar inscripciones");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificar(@PathParam("id") int id, Map<String, Object> datos,
            @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            servicio.modificar(id, datos);
            return Response.ok(Map.of("mensaje", "Inscripción modificada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar inscripción");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("anios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response anios(@Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.aniosDisponibles()).build();
        } catch (Exception e) {
            System.out.println("Error al listar años para inscripción");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("grados")
    @Produces(MediaType.APPLICATION_JSON)
    public Response grados(@QueryParam("anio") @DefaultValue("0") int anio,
            @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.gradosDisponibles(anio)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar grados del año");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("secciones")
    @Produces(MediaType.APPLICATION_JSON)
    public Response secciones(@QueryParam("grado") @DefaultValue("0") int grado,
            @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.seccionesDeGrado(grado)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar secciones del grado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("niveles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response niveles(@Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.niveles()).build();
        } catch (Exception e) {
            System.out.println("Error al listar niveles");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.obtener(id)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar inscripción");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esAutorizado(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Secretaria".equals(rol) || "Admin".equals(rol) || "Super Admin".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta accion")).build();
    }
}