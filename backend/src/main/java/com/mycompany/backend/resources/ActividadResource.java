package com.mycompany.backend.resources;

import com.mycompany.backend.service.ActividadService;
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
 * GET  /api/v1/actividades?page&size&nombre&estado   listado (la Secretaria solo consulta)
 * GET  /api/v1/actividades/{id}                      detalle con los grados del alcance
 * POST /api/v1/actividades                           {nombre, descripcion, costo, alcance, grados[]}
 * PUT  /api/v1/actividades/{id}                      edición
 * PUT  /api/v1/actividades/{id}/desactivar           deja de ofrecerse al cobrar
 * PUT  /api/v1/actividades/{id}/activar              vuelve a ofrecerse
 */
@Path("actividades")
public class ActividadResource {

    private final ActividadService servicio = new ActividadService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("nombre") String nombre,
            @QueryParam("estado") String estado,
            @Context ContainerRequestContext req) {
        if (!esSecretariaOAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.listar(nombre, estado, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar actividades");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esSecretariaOAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.obtener(id)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar la actividad");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.crear(datos)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al crear la actividad");
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
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.modificar(id, datos)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar la actividad");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivar(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            servicio.desactivar(id);
            return Response.ok(Map.of("mensaje", "Actividad desactivada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al desactivar la actividad");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/activar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activar(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            servicio.activar(id);
            return Response.ok(Map.of("mensaje", "Actividad activada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al activar la actividad");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Admin".equals(rol) || "Super Admin".equals(rol);
    }

    private boolean esSecretariaOAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Secretaria".equals(rol) || "Admin".equals(rol) || "Super Admin".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta acción")).build();
    }
}