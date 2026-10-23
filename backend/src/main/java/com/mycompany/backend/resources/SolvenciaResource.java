package com.mycompany.backend.resources;

import com.mycompany.backend.service.SolvenciaService;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * GET /api/v1/solvencias?carnet&anio          calcula el saldo y el detalle
 * GET /api/v1/solvencias/pdf?carnet&anio       genera el PDF (solo si el saldo es cero)
 * La secretaria genera la solvencia, los demas roles consultan el cálculoa
 */
@Path("solvencias")
public class SolvenciaResource {

    private final SolvenciaService servicio = new SolvenciaService();

    @GET
    @Path("contexto")
    @Produces(MediaType.APPLICATION_JSON)
    public Response contexto(@QueryParam("carnet") String carnet,
            @Context ContainerRequestContext req) {
        if (!esSecretariaOAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.contexto(carnet)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar el contexto de la solvencia");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response calcular(@QueryParam("carnet") String carnet,
            @QueryParam("anio") @DefaultValue("0") int anio,
            @Context ContainerRequestContext req) {
        if (!esSecretariaOAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.calcular(carnet, anio)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al calcular la solvencia");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("pdf")
    public Response pdf(@QueryParam("carnet") String carnet,
            @QueryParam("anio") @DefaultValue("0") int anio,
            @Context ContainerRequestContext req) {
        if (!esSecretaria(req)) {
            return prohibido();
        }
        try {
            byte[] pdf = servicio.generarPdf(carnet, anio);
            return Response.ok(pdf)
                    .type("application/pdf")
                    .header("Content-Disposition", "inline; filename=\"solvencia.pdf\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar la solvencia");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esSecretaria(ContainerRequestContext req) {
        return "Secretaria".equals(req.getProperty("rol"));
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