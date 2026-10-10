package com.mycompany.backend.resources;

import com.mycompany.backend.service.CursoService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * POST /api/v1/cursos {nombre, grados?[]}
 * GET /api/v1/cursos?page&size&nombre&estado&grado
 * PUT /api/v1/cursos/{id} {nombre}
 * PUT /api/v1/cursos/{id}/desactivar
 * PUT /api/v1/cursos/{id}/activar
 */
@Path("cursos")
public class CursoResource {

    private final CursoService servicio = new CursoService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("nombre") String nombre,
            @QueryParam("estado") String estado,
            @QueryParam("grado") Integer grado) {
        try {
            return Response.ok(servicio.listar(nombre, estado, grado, page, size)).build();
        } catch (Exception e) {
            System.out.println("Error al listar cursos");
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
            System.out.println("Error al consultar curso");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Response crear(Map<String, Object> datos) {
        try {
            String nombre = datos != null ? String.valueOf(datos.getOrDefault("nombre", "")) : null;
            List<Integer> grados = new ArrayList<>();
            if (datos != null && datos.get("grados") instanceof List<?> cruda) {
                for (Object o : cruda) {
                    if (o instanceof Number n) {
                        grados.add(n.intValue());
                    } else if (o != null) {
                        grados.add(Integer.parseInt(String.valueOf(o)));
                    }
                }
            }
            servicio.crear(nombre, grados);
            return Response.ok(Map.of("mensaje", "Curso creado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al crear curso");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificar(@PathParam("id") int id, Map<String, String> datos) {
        try {
            String nombre = datos != null ? datos.get("nombre") : null;
            servicio.modificar(id, nombre);
            return Response.ok(Map.of("mensaje", "Curso modificado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar curso");
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
            return Response.ok(Map.of("mensaje", "Curso desactivado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al desactivar curso");
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
            return Response.ok(Map.of("mensaje", "Curso activado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al activar curso");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }
}
