package com.mycompany.backend.resources;

import com.mycompany.backend.service.ActividadEvaluableService;
import com.mycompany.backend.service.NotaService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
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
import java.util.List;
import java.util.Map;

/**
 * Ponderación y notas del maestro.
 * GET  /api/v1/maestro/ponderacion?idAsignacion
 * POST /api/v1/maestro/ponderacion {idAsignacion, actividades:[{nombre, puntos}], confirmar?}
 * PUT  /api/v1/maestro/ponderacion {idAsignacion, actividades:[{idActividadEvaluable?, nombre, puntos}], confirmar?}
 * DELETE /api/v1/maestro/ponderacion?idAsignacion
 * GET  /api/v1/maestro/notas?idAsignacion
 * POST /api/v1/maestro/notas {idAsignacion, idActividadEvaluable, notas:[{idInscripcion, valor}]}
 * PUT  /api/v1/maestro/notas/{id} {valor}
 */
@Path("maestro")
public class ActividadEvaluableResource {

    private final ActividadEvaluableService servicio = new ActividadEvaluableService();
    private final NotaService notaService = new NotaService();

    @GET
    @Path("ponderacion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarPonderacion(@QueryParam("idAsignacion") @DefaultValue("0") int idAsignacion,
            @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            String rol = String.valueOf(req.getProperty("rol"));
            return Response.ok(servicio.listar(idAsignacion, idUsuario, rol)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al listar la ponderación");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Path("ponderacion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response guardarPonderacion(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            int idAsignacion = numero(datos.get("idAsignacion"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actividades = (List<Map<String, Object>>) datos.get("actividades");
            boolean confirmar = Boolean.TRUE.equals(datos.get("confirmar"));
            String rol = String.valueOf(req.getProperty("rol"));
            return Response.ok(servicio.guardar(idAsignacion, idUsuario, rol, actividades, confirmar)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al guardar la ponderación");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @DELETE
    @Path("ponderacion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarPonderacion(@QueryParam("idAsignacion") @DefaultValue("0") int idAsignacion,
            @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            String rol = String.valueOf(req.getProperty("rol"));
            servicio.eliminar(idAsignacion, idUsuario, rol);
            return Response.ok(Map.of("mensaje", "Ponderación eliminada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al eliminar la ponderación");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("notas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarNotas(@QueryParam("idAsignacion") @DefaultValue("0") int idAsignacion,
            @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            String rol = String.valueOf(req.getProperty("rol"));
            return Response.ok(notaService.listar(idAsignacion, idUsuario, rol)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al listar las notas");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Path("notas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ingresarNotas(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            String rol = String.valueOf(req.getProperty("rol"));
            int idAsignacion = numero(datos.get("idAsignacion"));
            int idActividadEvaluable = numero(datos.get("idActividadEvaluable"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> notas = (List<Map<String, Object>>) datos.get("notas");
            return Response.ok(notaService.ingresar(idAsignacion, idActividadEvaluable, notas, idUsuario, rol)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al ingresar las notas");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("notas/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificarNota(@PathParam("id") int id, Map<String, Object> datos,
            @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            String rol = String.valueOf(req.getProperty("rol"));
            java.math.BigDecimal valor = new java.math.BigDecimal(String.valueOf(datos.get("valor")));
            notaService.modificar(id, idUsuario, rol, valor);
            return Response.ok(Map.of("mensaje", "Nota modificada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al modificar la nota");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private int idUsuario(ContainerRequestContext req) {
        Object id = req.getProperty("idUsuario");
        return id == null ? 0 : Integer.parseInt(String.valueOf(id));
    }

    private int numero(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }

    private boolean esMaestroOSuperAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Maestro".equals(rol) || "Super Admin".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta acción")).build();
    }
}