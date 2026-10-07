package com.mycompany.backend.resources;

import com.mycompany.backend.service.AutenticacionService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * POST /api/v1/usuarios/login  {correo, password} -> {token, rol} 
 */
@Path("usuarios")
public class UsuarioResource {

    private final AutenticacionService autenticacion = new AutenticacionService();

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Map<String, String> credenciales) {
        try {
            String correo = credenciales != null ? credenciales.get("correo") : null;
            String password = credenciales != null ? credenciales.get("password") : null;
            return Response.ok(autenticacion.login(correo, password)).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error en login de usuarios");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }
}
