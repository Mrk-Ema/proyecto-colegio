package com.mycompany.backend.resources;

import com.mycompany.backend.service.LibroService;
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
 * GET  /api/v1/libros?page&size&titulo&autor&estado
 * GET  /api/v1/libros/{id}
 * POST /api/v1/libros {titulo, autor, editorial, cantidad}
 * PUT  /api/v1/libros/{id} {titulo, autor, editorial, cantidad}
 * PUT  /api/v1/libros/{id}/desactivar
 * PUT  /api/v1/libros/{id}/activar
 * Bibliotecario escribe, Estudiante solo lista, Super Admin todo.
 */
@Path("libros")
public class LibroResource {

    private final LibroService servicio = new LibroService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("titulo") String titulo,
            @QueryParam("autor") String autor,
            @QueryParam("estado") String estado,
            @Context ContainerRequestContext req) {
        if (!esBibliotecarioOAdmin(req)) {
            return prohibido();
        }
        try {
            Map<String, Object> r = servicio.listar(titulo, autor, estado, page, size);
            return Response.ok(r).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar los libros");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esBibliotecarioOAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.obtener(id)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar el libro");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.crear(datos)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al registrar el libro");
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
        if (!esBibliotecario(req)) {
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
            System.out.println("Error al modificar el libro");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivar(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            servicio.desactivar(id);
            return Response.ok(Map.of("mensaje", "Libro desactivado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al desactivar el libro");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/activar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activar(@PathParam("id") int id, @Context ContainerRequestContext req) {
        if (!esBibliotecario(req)) {
            return prohibido();
        }
        try {
            servicio.activar(id);
            return Response.ok(Map.of("mensaje", "Libro activado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al activar el libro");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esBibliotecario(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Bibliotecario".equals(rol) || "Super Admin".equals(rol);
    }

    private boolean esBibliotecarioOAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        // El Estudiante también puede listar libros (CU079)
        return "Bibliotecario".equals(rol) || "Super Admin".equals(rol)
                || "Admin".equals(rol) || "Secretaria".equals(rol) || "Estudiante".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta acción")).build();
    }
}