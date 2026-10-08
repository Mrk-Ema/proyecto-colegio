package com.mycompany.backend.resources;

import com.mycompany.backend.dao.GradoDAO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * GET /api/v1/grados  listar los grados activos 
 */
@Path("grados")
public class GradoResource {

    private final GradoDAO grados = new GradoDAO();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarActivos() {
        try {
            return Response.ok(grados.listarActivos()).build();
        } catch (Exception e) {
            System.out.println("Error al listar grados");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }
}
