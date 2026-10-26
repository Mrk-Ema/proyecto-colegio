package com.mycompany.backend.resources;

import com.mycompany.backend.service.ParametroBibliotecaService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * GET /api/v1/parametros-biblioteca
 * PUT /api/v1/parametros-biblioteca {diasPrestamo, multaPorDia}
 * Solo el Bibliotecario y el Super Admin configuran los parámetros.
 */
@Path("parametros-biblioteca")
public class ParametroBibliotecaResource {

    private final ParametroBibliotecaService servicio = new ParametroBibliotecaService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.obtener()).build();
        } catch (Exception e) {
            System.out.println("Error al obtener los parámetros de biblioteca");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response guardar(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.guardar(datos)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al guardar los parámetros de biblioteca");
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