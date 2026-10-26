package com.mycompany.backend.resources;

import com.mycompany.backend.service.MultaService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
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
 * GET  /api/v1/multas?page&size&prestatario&tipoPrestatario&estado&desde&hasta
 * GET  /api/v1/multas/{id}
 * PUT  /api/v1/multas/{id}/pagar
 * Solo el Bibliotecario y el Super Admin gestionan multas.
 */
@Path("multas")
public class MultaResource {

    private final MultaService servicio = new MultaService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
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
            return Response.ok(servicio.listar(prestatario, tipoPrestatario, estado, desde, hasta, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar las multas");
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
            System.out.println("Error al consultar la multa");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/pagar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pagar(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.registrarPago(id, idUsuario)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al registrar el pago de la multa");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private int idUsuario(ContainerRequestContext req) {
        Object id = req.getProperty("idUsuario");
        return id == null ? 0 : Integer.parseInt(String.valueOf(id));
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