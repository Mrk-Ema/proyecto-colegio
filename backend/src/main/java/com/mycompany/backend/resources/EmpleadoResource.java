package com.mycompany.backend.resources;

import com.mycompany.backend.service.EmpleadoService;
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
 * POST /api/v1/empleados
 * GET /api/v1/empleados?page&size&nombre&puesto&estado
 * GET /api/v1/empleados/{id}
 * PUT /api/v1/empleados/{id}
 * PUT /api/v1/empleados/{id}/desactivar
 * PUT /api/v1/empleados/{id}/activar
 */
@Path("empleados")
public class EmpleadoResource {

    private final EmpleadoService servicio = new EmpleadoService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.crear(datos, rol(req))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al crear empleado");
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
            @QueryParam("nombre") String nombre,
            @QueryParam("puesto") String puesto,
            @QueryParam("estado") String estado,
            @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.listar(nombre, puesto, estado, rol(req), page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al listar empleados");
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
            return Response.ok(servicio.obtener(id, rol(req))).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al consultar empleado");
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
            servicio.modificar(id, datos, rol(req));
            return Response.ok(Map.of("mensaje", "Empleado modificado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al modificar empleado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivar(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            servicio.desactivar(id, rol(req));
            return Response.ok(Map.of("mensaje", "Empleado desactivado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al desactivar empleado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/activar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activar(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esAutorizado(req)) {
            return prohibido();
        }
        try {
            servicio.activar(id, rol(req));
            return Response.ok(Map.of("mensaje", "Empleado activado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al activar empleado");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esAutorizado(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Admin".equals(rol) || "Super Admin".equals(rol);
    }

    private String rol(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return rol == null ? "" : String.valueOf(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta accion")).build();
    }
}
