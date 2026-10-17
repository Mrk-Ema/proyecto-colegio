package com.mycompany.backend.resources;

import com.mycompany.backend.dao.NivelDAO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
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
    public Response listar(@Context ContainerRequestContext req) {
        if (!esAdminOSuperAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(dao.listar()).build();
        } catch (Exception e) {
            System.out.println("Error al listar niveles");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esSuperAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Super Admin".equals(rol);
    }

    private boolean esAdminOSuperAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Admin".equals(rol) || "Super Admin".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta accion")).build();
    }
}
