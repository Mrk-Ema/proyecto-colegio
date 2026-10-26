package com.mycompany.backend.resources;

import com.mycompany.backend.service.PrestamoService;
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
 * GET  /api/v1/prestamos?page&size&libro&prestatario&tipoPrestatario&estado&desde&hasta
 * GET  /api/v1/prestamos/{id}
 * POST /api/v1/prestamos {idLibro, tipoPrestatario, idPrestatario}
 * PUT  /api/v1/prestamos/{id}/devolver
 * Solo el Bibliotecario y el Super Admin gestionan préstamos.
 */
@Path("prestamos")
public class PrestamoResource {

    private final PrestamoService servicio = new PrestamoService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("libro") Integer libro,
            @QueryParam("prestatario") Integer prestatario,
            @QueryParam("tipoPrestatario") String tipoPrestatario,
            @QueryParam("estado") String estado,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.listar(libro, prestatario, tipoPrestatario, estado,
                    desde, hasta, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar los préstamos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.obtener(id)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar el préstamo");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrar(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.registrar(datos)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al registrar el préstamo");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/devolver")
    @Produces(MediaType.APPLICATION_JSON)
    public Response devolver(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.devolver(id)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al registrar la devolución");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esBibliotecario(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Bibliotecario".equals(rol) || "Super Admin".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta acción")).build();
    }
}