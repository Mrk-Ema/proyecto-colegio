package com.mycompany.backend.resources;

import com.mycompany.backend.service.AnioService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * GET /api/v1/anios                 listar años
 * GET /api/v1/anios/{anio}          detalle con estructura, puro filtro
 * POST /api/v1/anios                {anio, fechaInicio, fechaCierre, grados[]}  para crear el año
 * PUT /api/v1/anios/{anio}          {fechaInicio, fechaCierre} para modificar el año
 */
@Path("anios")
public class AnioResource {

    private final AnioService servicio = new AnioService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar() {
        try {
            return Response.ok(servicio.listar()).build();
        } catch (Exception e) {
            System.out.println("Error al listar años lectivos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("{anio}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("anio") int anio) {
        try {
            return Response.ok(servicio.obtener(anio)).build();
        } catch (Exception e) {
            System.out.println("Error al consultar año lectivo");
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
            int anio = numero(datos.get("anio"));
            LocalDate inicio = fecha(datos.get("fechaInicio"));
            LocalDate cierre = fecha(datos.get("fechaCierre"));
            List<Integer> grados = (List<Integer>) datos.get("grados");
            servicio.crearAnio(anio, inicio, cierre, grados);
            return Response.ok(Map.of("mensaje", "Año lectivo creado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al crear año lectivo");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{anio}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificar(@PathParam("anio") int anio, Map<String, String> datos) {
        try {
            LocalDate inicio = fecha(datos != null ? datos.get("fechaInicio") : null);
            LocalDate cierre = fecha(datos != null ? datos.get("fechaCierre") : null);
            servicio.modificar(anio, inicio, cierre);
            return Response.ok(Map.of("mensaje", "Año lectivo modificado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar año lectivo");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{anio}/activar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activar(@PathParam("anio") int anio) {
        try {
            servicio.activar(anio);
            return Response.ok(Map.of("mensaje", "Año lectivo activado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al activar año lectivo");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("{anio}/cerrar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cerrar(@PathParam("anio") int anio) {
        try {
            servicio.cerrar(anio);
            return Response.ok(Map.of("mensaje", "Año lectivo cerrado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al cerrar año lectivo");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @DELETE
    @Path("{anio}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("anio") int anio) {
        try {
            servicio.eliminar(anio);
            return Response.ok(Map.of("mensaje", "Año lectivo eliminado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al eliminar año lectivo");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private int numero(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }

    private LocalDate fecha(Object v) {
        if (v == null || String.valueOf(v).isBlank()) {
            return null;
        }
        return LocalDate.parse(String.valueOf(v).trim());
    }
}
