package com.mycompany.backend.resources;

import com.mycompany.backend.service.CurriculoService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GET /api/v1/curriculo?grado=&contexto(defecto|anio)
 * POST /api/v1/curriculo {idGrado, contexto, cursos[]}
 * PUT /api/v1/curriculo {idGrado, contexto, cursos[]}
 */
@Path("curriculo")
public class CurriculoResource {

    private final CurriculoService servicio = new CurriculoService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response consultar(
            @QueryParam("grado") int grado,
            @QueryParam("contexto") @DefaultValue("defecto") String contexto) {
        try {
            return Response.ok(servicio.consultar(grado, contexto)).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al consultar currículo");
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
            servicio.crear(numero(datos.get("idGrado")), contexto(datos), lista(datos.get("cursos")));
            return Response.ok(Map.of("mensaje", "Currículo creado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al crear currículo");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Response modificar(Map<String, Object> datos) {
        try {
            servicio.modificar(numero(datos.get("idGrado")), contexto(datos), lista(datos.get("cursos")));
            return Response.ok(Map.of("mensaje", "Currículo modificado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar currículo");
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

    private String contexto(Map<String, Object> datos) {
        Object c = datos != null ? datos.get("contexto") : null;
        return c == null ? "defecto" : String.valueOf(c);
    }

    private List<Integer> lista(Object v) {
        List<Integer> l = new ArrayList<>();
        if (v instanceof List<?> x) {
            for (Object o : x) {
                l.add(o instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(o)));
            }
        }
        return l;
    }
}
