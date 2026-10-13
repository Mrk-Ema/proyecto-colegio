package com.mycompany.backend.resources;

import com.mycompany.backend.dao.NivelDAO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * GET /api/v1/niveles  catalogo para combos
 */
@Path("niveles")
public class NivelResource {

    private final NivelDAO dao = new NivelDAO();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar() {
        try {
            return Response.ok(dao.listar()).build();
        } catch (Exception e) {
            System.out.println("Error al listar niveles");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }
}
