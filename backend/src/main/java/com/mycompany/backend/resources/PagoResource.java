package com.mycompany.backend.resources;

import com.mycompany.backend.service.PagoService;
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
 * GET  /api/v1/pagos?carnet&concepto&estado&desde&hasta&page&size   listado (el carné es obligatorio)
 * GET  /api/v1/pagos/contexto?carnet&anio   datos para cobrar: inscripción, cuota y actividades
 * POST /api/v1/pagos                        {carnet, anio, concepto, mes}
 * POST /api/v1/pagos/actividad              {carnet, anio, idActividad}
 * PUT  /api/v1/pagos/{id}/anular            {motivo}
 * Registrar y anular es solo de la Secretaria; los demás roles consultan.
 */
@Path("pagos")
public class PagoResource {

    private final PagoService servicio = new PagoService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("carnet") String carnet,
            @QueryParam("idEstudiante") Integer idEstudiante,
            @QueryParam("concepto") String concepto,
            @QueryParam("estado") String estado,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @Context ContainerRequestContext req) {
        if (!esSecretariaOAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.listar(carnet, idEstudiante, concepto, estado,
                    desde, hasta, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar los pagos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("contexto")
    @Produces(MediaType.APPLICATION_JSON)
    public Response contexto(@QueryParam("carnet") String carnet,
            @QueryParam("anio") Integer anio,
            @Context ContainerRequestContext req) {
        if (!esSecretariaOAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.contexto(carnet, anio)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar el contexto del cobro");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{id}/comprobante")
    public Response comprobante(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esSecretariaOAdmin(req)) {
            return prohibido();
        }
        try {
            byte[] pdf = servicio.generarComprobante(id);
            return Response.ok(pdf)
                    .type("application/pdf")
                    .header("Content-Disposition", "inline; filename=\"comprobante-" + id + ".pdf\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el comprobante");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
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
            System.out.println("Error al consultar el pago");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrar(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esSecretaria(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.registrar(datos, idUsuario(req))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al registrar el pago");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Path("actividad")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarActividad(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esSecretaria(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.registrarActividad(datos, idUsuario(req))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al registrar el pago de actividad");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/anular")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response anular(@PathParam("id") int id, Map<String, Object> datos,
            @Context ContainerRequestContext req) {
        if (!esSecretaria(req)) {
            return prohibido();
        }
        try {
            String motivo = datos.get("motivo") == null ? null : String.valueOf(datos.get("motivo"));
            servicio.anular(id, motivo, idUsuario(req));
            return Response.ok(Map.of("mensaje", "Pago anulado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al anular el pago");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
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

    private int idUsuario(ContainerRequestContext req) {
        Object id = req.getProperty("idUsuario");
        return id == null ? 0 : Integer.parseInt(String.valueOf(id));
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta acción")).build();
    }
}