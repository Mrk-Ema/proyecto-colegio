package com.mycompany.backend.resources;

import com.mycompany.backend.service.SeccionService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
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
 * POST /api/v1/secciones {nombre, idGrado}
 * GET /api/v1/secciones?page&size&nivel&carrera&grado&nombre&estado
 * GET /api/v1/secciones/{id}
 * PUT /api/v1/secciones/{id} {nombre}
 * PUT /api/v1/secciones/{id}/desactivar
 * PUT /api/v1/secciones/{id}/activar
 */
@Path("secciones")
public class SeccionResource {

    private final SeccionService servicio = new SeccionService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("nivel") Integer nivel,
            @QueryParam("carrera") Integer carrera,
            @QueryParam("grado") Integer grado,
            @QueryParam("nombre") String nombre,
            @QueryParam("estado") String estado,
            @Context ContainerRequestContext req) {
        if (!esAdminOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.listar(nivel, carrera, grado, nombre, estado, page, size)).build();
        } catch (Exception e) {
            System.out.println("Error al listar secciones");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") int id,
    @Context ContainerRequestContext req) {
        if (!esAdminOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.obtener(id)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar sección");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esAdminOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            String nombre = datos != null ? String.valueOf(datos.getOrDefault("nombre", "")) : null;
            Integer idGrado = entero(datos != null ? datos.get("idGrado") : null);
            servicio.crear(nombre, idGrado);
            return Response.ok(Map.of("mensaje", "Sección creada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al crear sección");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificar(@PathParam("id") int id, Map<String, String> datos,
    @Context ContainerRequestContext req) {
        if (!esAdminOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            String nombre = datos != null ? datos.get("nombre") : null;
            servicio.modificar(id, nombre);
            return Response.ok(Map.of("mensaje", "Sección modificada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar sección");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivar(@PathParam("id") int id,
    @Context ContainerRequestContext req) {
        if (!esAdminOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            servicio.desactivar(id);
            return Response.ok(Map.of("mensaje", "Sección desactivada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al desactivar sección");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/activar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activar(@PathParam("id") int id,
    @Context ContainerRequestContext req) {
        if (!esAdminOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            servicio.activar(id);
            return Response.ok(Map.of("mensaje", "Sección activada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al activar sección");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private Integer entero(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v).trim());
    }

    private boolean esSuperAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Super Admin".equals(rol);
    }

    private boolean esAdminOSuperAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Admin".equals(rol) || "Super Admin".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta accion")).build();
    }
}
