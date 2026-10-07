package com.mycompany.backend.config;

import com.mycompany.backend.dao.UsuarioDAO;
import com.mycompany.backend.dto.Usuario;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Provider
public class AutenticacionFilter implements ContainerRequestFilter {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void filter(ContainerRequestContext request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return;
        }
        String path = request.getUriInfo().getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if ("usuarios/login".equals(path) || "usuarios/recuperar".equals(path)) {
            return;
        }
        String auth = request.getHeaderString("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            abortar(request);
            return;
        }
        String[] partes;
        try {
            String datos = new String(Base64.getUrlDecoder().decode(auth.substring(7).trim()),
                    StandardCharsets.UTF_8);
            partes = datos.split(":");
            if (partes.length != 3) {
                abortar(request);
                return;
            }
        } catch (IllegalArgumentException e) {
            abortar(request);
            return;
        }
        Integer idUsuario;
        long expira;
        try {
            idUsuario = Integer.valueOf(partes[0]);
            expira = Long.parseLong(partes[2]);
        } catch (NumberFormatException e) {
            abortar(request);
            return;
        }
        if (System.currentTimeMillis() > expira) {
            abortar(request);
            return;
        }
        try {
            Usuario u = usuarioDAO.buscarPorId(idUsuario);
            if (u == null || !"Activo".equals(u.getEstado())) {
                abortar(request);
                return;
            }
            request.setProperty("idUsuario", u.getIdUsuario());
            request.setProperty("rol", u.getRol());
        } catch (Exception e) {
            System.out.println("Error al validar token de autenticacion");
            e.printStackTrace();
            abortar(request);
        }
    }

    private void abortar(ContainerRequestContext request) {
        request.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "No autorizado"))
                .type(MediaType.APPLICATION_JSON)
                .build());
    }
}
