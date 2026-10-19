package com.mycompany.backend.resources;

import com.mycompany.backend.service.CuotaService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * GET /api/v1/cuotas?anio            grados del año con sus montos actuales
 * PUT /api/v1/cuotas                 {anio, cuotas:[{idGrado, montoInscripcion, montoMensualidad}]}
 * Solo el Admin y el Super Admin configuran los montos.
 */
@Path("cuotas")
public class CuotaResource {

    private final CuotaService servicio = new CuotaService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(@QueryParam("anio") @DefaultValue("0") int anio,
            @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.listar(anio)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar las cuotas");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response guardar(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.guardar(datos)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al guardar las cuotas");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Admin".equals(rol) || "Super Admin".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta acción")).build();
    }
}