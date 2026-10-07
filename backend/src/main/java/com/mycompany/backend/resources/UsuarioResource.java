package com.mycompany.backend.resources;

import com.mycompany.backend.service.AutenticacionService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * POST /api/v1/usuarios/login      {correo, password} -> {token, rol} 
 * POST /api/v1/usuarios/recuperar  {correo, fechaNacimiento, nueva} -> token o error
 * PUT  /api/v1/usuarios/password   {actual, nueva} (auth)
 * GET  /api/v1/usuarios/perfil     (auth, id del token)
 * PUT  /api/v1/usuarios/perfil     (auth, datos basicos)
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

    @POST
    @Path("recuperar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recuperar(Map<String, String> datos) {
        try {
            String correo = datos != null ? datos.get("correo") : null;
            String fechaNacimiento = datos != null ? datos.get("fechaNacimiento") : null;
            String nueva = datos != null ? datos.get("nueva") : null;
            autenticacion.recuperarPassword(correo, fechaNacimiento, nueva);
            return Response.ok(Map.of("mensaje", "Contraseña restablecida")).build();
        } catch (SecurityException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error en recuperar contraseña");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cambiarPassword(@Context ContainerRequestContext ctx, Map<String, String> datos) {
        try {
            Integer idUsuario = (Integer) ctx.getProperty("idUsuario");
            String actual = datos != null ? datos.get("actual") : null;
            String nueva = datos != null ? datos.get("nueva") : null;
            autenticacion.cambiarPassword(idUsuario, actual, nueva);
            return Response.ok(Map.of("mensaje", "Contraseña actualizada")).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error en cambio de contraseña");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("perfil")
    @Produces(MediaType.APPLICATION_JSON)
    public Response perfil(@Context ContainerRequestContext ctx) {
        try {
            Integer idUsuario = (Integer) ctx.getProperty("idUsuario");
            return Response.ok(autenticacion.obtenerPerfil(idUsuario)).build();
        } catch (Exception e) {
            System.out.println("Error al consultar perfil");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @PUT
    @Path("perfil")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificarPerfil(@Context ContainerRequestContext ctx, Map<String, String> datos) {
        try {
            Integer idUsuario = (Integer) ctx.getProperty("idUsuario");
            autenticacion.actualizarPerfil(idUsuario, datos);
            return Response.ok(Map.of("mensaje", "Perfil actualizado")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al modificar perfil");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }
}
