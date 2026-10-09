package com.mycompany.backend.resources;

import com.mycompany.backend.service.AnioService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * PUT /api/v1/estructura/{anio} {grados[]} modica la estructura
 */
@Path("estructura")
public class EstructuraResource {

    private final AnioService servicio = new AnioService();

    @PUT
    @Path("{anio}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Response reconciliar(@PathParam("anio") int anio, Map<String, Object> datos) {
        try {
            List<Integer> grados = datos != null ? (List<Integer>) datos.get("grados") : null;
            servicio.modificarEstructura(anio, grados);
            return Response.ok(Map.of("mensaje", "Estructura actualizada")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar estructura");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }
}
