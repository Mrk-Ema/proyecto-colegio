package com.mycompany.backend.resources;

import com.mycompany.backend.service.AsistenciaService;
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
import java.util.List;
import java.util.Map;

/**
 * GET  /api/v1/maestro/cursos?anio                      asignaciones del maestro en un ciclo no Cerrado
 * GET  /api/v1/maestro/cursos/{id}/nomina?page&size&nombre   inscritos del curso (paginado)
 * POST /api/v1/maestro/asistencia                      {idAsignacion, fecha, asistencias:[{idInscripcion, estado}]}
 * GET  /api/v1/maestro/asistencia?idAsignacion&desde&hasta&estado&estudiante&page&size
 * PUT  /api/v1/maestro/asistencia/{id}                  {estado}
 */
@Path("maestro")
public class AsistenciaResource {

    private final AsistenciaService servicio = new AsistenciaService();

    @GET
    @Path("anios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response anios(@Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.anios(idUsuario)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar los años del maestro");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("cursos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cursos(@QueryParam("anio") Integer anio,
            @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.cursos(idUsuario, anio)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al listar los cursos del maestro");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("cursos/{id}/nomina")
    @Produces(MediaType.APPLICATION_JSON)
    public Response nomina(@PathParam("id") int idAsignacion,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("nombre") String nombre,
            @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.nomina(idAsignacion, idUsuario, nombre, page, size)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al listar la nómina");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @POST
    @Path("asistencia")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response tomarAsistencia(Map<String, Object> datos, @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            int idAsignacion = numero(datos.get("idAsignacion"));
            String fecha = String.valueOf(datos.get("fecha"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> asistencias = (List<Map<String, Object>>) datos.get("asistencias");
            return Response.ok(servicio.tomarAsistencia(idAsignacion, idUsuario, fecha, asistencias)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al tomar la asistencia");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("asistencia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarAsistencia(
            @QueryParam("idAsignacion") @DefaultValue("0") int idAsignacion,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("estado") String estado,
            @QueryParam("estudiante") String estudiante,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            return Response.ok(servicio.listarAsistencia(idAsignacion, idUsuario, estado, estudiante,
                    desde, hasta, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al listar la asistencia");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("asistencia/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response corregirAsistencia(@PathParam("id") int id, Map<String, Object> datos,
            @Context ContainerRequestContext req) {
        if (!esMaestroOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            int idUsuario = idUsuario(req);
            String estado = String.valueOf(datos.get("estado"));
            servicio.corregirAsistencia(id, idUsuario, estado);
            return Response.ok(Map.of("mensaje", "Asistencia corregida")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (SecurityException e) {
            return prohibido();
        } catch (Exception e) {
            System.out.println("Error al corregir la asistencia");
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