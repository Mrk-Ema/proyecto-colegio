package com.mycompany.backend.resources;

import com.mycompany.backend.service.EstudianteConsultaService;
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
 * GET /api/v1/estudiante/notas?anio=&curso=          notas del estudiante logueado
 * GET /api/v1/estudiante/estado-cuenta?anio=         estado de cuenta
 * GET /api/v1/estudiante/prestamos?estado=&desde=&hasta=&page=&size=
 * GET /api/v1/estudiante/multas?estado=&page=&size=
 * Solo el Estudiante (sus propios datos) y el Super Admin.
 */
@Path("estudiante")
public class EstudianteConsultaResource {

    private final EstudianteConsultaService servicio = new EstudianteConsultaService();

    @GET
    @Path("notas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notas(@QueryParam("anio") Integer anio,
            @QueryParam("curso") Integer curso,
            @Context ContainerRequestContext req) {
        if (!esEstudianteOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.notas(idUsuario, anio, curso)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar las notas del estudiante");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("estado-cuenta")
    @Produces(MediaType.APPLICATION_JSON)
    public Response estadoCuenta(@QueryParam("anio") @DefaultValue("0") int anio,
            @Context ContainerRequestContext req) {
        if (!esEstudianteOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.estadoCuenta(idUsuario, anio == 0 ? null : anio)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar el estado de cuenta");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("prestamos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response prestamos(
            @QueryParam("estado") String estado,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @Context ContainerRequestContext req) {
        if (!esEstudianteOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.prestamos(idUsuario, estado, desde, hasta, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar los préstamos del estudiante");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("multas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response multas(
            @QueryParam("estado") String estado,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @Context ContainerRequestContext req) {
        if (!esEstudianteOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.multas(idUsuario, estado, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar las multas del estudiante");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private int idUsuario(ContainerRequestContext req) {
        Object id = req.getProperty("idUsuario");
        return id == null ? 0 : Integer.parseInt(String.valueOf(id));
    }

    private boolean esEstudianteOSuperAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Estudiante".equals(rol) || "Super Admin".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta acción")).build();
    }
}