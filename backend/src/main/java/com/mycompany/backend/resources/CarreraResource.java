package com.mycompany.backend.resources;

import com.mycompany.backend.service.CarreraService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * POST /api/v1/carreras
 * GET /api/v1/carreras?page&size&nombre&estado
 * PUT /api/v1/carreras/{id}
 * PUT /api/v1/carreras/{id}/desactivar
 * PUT /api/v1/carreras/{id}/activar
 */
@Path("carreras")
public class CarreraResource {

    private final CarreraService servicio = new CarreraService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("nombre") String nombre,
            @QueryParam("estado") String estado) {
        try {
            return Response.ok(servicio.listar(nombre, estado, page, size)).build();
        } catch (Exception e) {
            System.out.println("Error al listar carreras");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") int id) {
        try {
            return Response.ok(servicio.obtener(id)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar carrera");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Map<String, Object> datos) {
        try {
            String nombre = datos != null ? String.valueOf(datos.getOrDefault("nombre", "")) : null;
            Object desc = datos != null ? datos.get("descripcion") : null;
            String descripcion = desc == null ? null : String.valueOf(desc);
            Integer duracion = numero(datos != null ? datos.get("duracionAnios") : null);
            servicio.crear(nombre, descripcion, duracion);
            return Response.ok(Map.of("mensaje", "Carrera creada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al crear carrera");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificar(@PathParam("id") int id, Map<String, Object> datos) {
        try {
            String nombre = datos != null ? String.valueOf(datos.getOrDefault("nombre", "")) : null;
            Object desc = datos != null ? datos.get("descripcion") : null;
            String descripcion = desc == null ? null : String.valueOf(desc);
            Integer duracion = numero(datos != null ? datos.get("duracionAnios") : null);
            servicio.modificar(id, nombre, descripcion, duracion);
            return Response.ok(Map.of("mensaje", "Carrera modificada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar carrera");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivar(@PathParam("id") int id) {
        try {
            servicio.desactivar(id);
            return Response.ok(Map.of("mensaje", "Carrera desactivada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al desactivar carrera");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}/activar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activar(@PathParam("id") int id) {
        try {
            servicio.activar(id);
            return Response.ok(Map.of("mensaje", "Carrera activada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al activar carrera");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private Integer numero(Object v) {
        if (v == null || String.valueOf(v).isBlank() || "null".equals(String.valueOf(v))) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v).trim());
    }
}
