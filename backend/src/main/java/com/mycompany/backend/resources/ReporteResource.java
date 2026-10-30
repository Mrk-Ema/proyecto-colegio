package com.mycompany.backend.resources;

import com.mycompany.backend.service.ReporteService;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * GET /api/v1/reportes/solventes-morosos?anio=&grado=&seccion=&page=&size=   tabla paginada
 * GET /api/v1/reportes/solventes-morosos/pdf?anio=&grado=&seccion=           PDF
 * Admin, Super Admin y Secretaria consultan; solo Secretaria... no, todos los roles permitidos generan PDF.
 */
@Path("reportes")
public class ReporteResource {

    private final ReporteService servicio = new ReporteService();

    @GET
    @Path("solventes-morosos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solventesMorosos(
            @QueryParam("anio") @DefaultValue("0") int anio,
            @QueryParam("grado") Integer grado,
            @QueryParam("seccion") Integer seccion,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @Context ContainerRequestContext req) {
        if (!esAdminOSecretaria(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.solventesMorosos(anio, grado, seccion, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el reporte de solventes y morosos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("solventes-morosos/pdf")
    public Response solventesMorososPdf(
            @QueryParam("anio") @DefaultValue("0") int anio,
            @QueryParam("grado") Integer grado,
            @QueryParam("seccion") Integer seccion,
            @Context ContainerRequestContext req) {
        if (!esAdminOSecretaria(req)) {
            return prohibido();
        }
        try {
            byte[] pdf = servicio.solventesMorososPdf(anio, grado, seccion);
            return Response.ok(pdf)
                    .type("application/pdf")
                    .header("Content-Disposition", "inline; filename=\"solventes-morosos.pdf\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el PDF de solventes y morosos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("rendimiento")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rendimiento(
            @QueryParam("anio") @DefaultValue("0") int anio,
            @QueryParam("grado") Integer grado,
            @QueryParam("seccion") Integer seccion,
            @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            if (grado == null || seccion == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Grado y sección son obligatorios")).build();
            }
            return Response.ok(servicio.rendimiento(anio, grado, seccion)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el reporte de rendimiento");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("rendimiento/pdf")
    public Response rendimientoPdf(
            @QueryParam("anio") @DefaultValue("0") int anio,
            @QueryParam("grado") Integer grado,
            @QueryParam("seccion") Integer seccion,
            @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            if (grado == null || seccion == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(Map.of("error", "Grado y sección son obligatorios")).build();
            }
            byte[] pdf = servicio.rendimientoPdf(anio, grado, seccion);
            return Response.ok(pdf)
                    .type("application/pdf")
                    .header("Content-Disposition", "inline; filename=\"rendimiento.pdf\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el PDF de rendimiento");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("historial-pagos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response historialPagos(
            @QueryParam("idEstudiante") Integer idEstudiante,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @Context ContainerRequestContext req) {
        if (!esAdminOSecretaria(req)) {
            return prohibido();
        }
        try {
            if (idEstudiante == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El ID del estudiante es obligatorio")).build();
            }
            return Response.ok(servicio.historialPagos(idEstudiante, desde, hasta, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el reporte de historial de pagos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("historial-pagos/pdf")
    public Response historialPagosPdf(
            @QueryParam("idEstudiante") Integer idEstudiante,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @Context ContainerRequestContext req) {
        if (!esAdminOSecretaria(req)) {
            return prohibido();
        }
        try {
            if (idEstudiante == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(Map.of("error", "El ID del estudiante es obligatorio")).build();
            }
            byte[] pdf = servicio.historialPagosPdf(idEstudiante, desde, hasta);
            return Response.ok(pdf)
                    .type("application/pdf")
                    .header("Content-Disposition", "inline; filename=\"historial-pagos.pdf\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el PDF de historial de pagos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("ingresos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ingresos(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.ingresos(desde, hasta)).build();
        } catch (Exception e) {
            System.out.println("Error al generar el reporte de ingresos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("ingresos/pdf")
    public Response ingresosPdf(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @Context ContainerRequestContext req) {
        if (!esAdmin(req)) {
            return prohibido();
        }
        try {
            byte[] pdf = servicio.ingresosPdf(desde, hasta);
            return Response.ok(pdf)
                    .type("application/pdf")
                    .header("Content-Disposition", "inline; filename=\"ingresos.pdf\"")
                    .build();
        } catch (Exception e) {
            System.out.println("Error al generar el PDF de ingresos");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("prestamos-mora")
    @Produces(MediaType.APPLICATION_JSON)
    public Response prestamosEnMora(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("libro") Integer libro,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @Context ContainerRequestContext req) {
        if (!esAdminOBibliotecario(req)) {
            return prohibido();
        }
        try {
            return Response.ok(servicio.prestamosEnMora(desde, hasta, libro, page, size)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el reporte de préstamos en mora");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    @GET
    @Path("prestamos-mora/pdf")
    public Response prestamosEnMoraPdf(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("libro") Integer libro,
            @Context ContainerRequestContext req) {
        if (!esAdminOBibliotecario(req)) {
            return prohibido();
        }
        try {
            byte[] pdf = servicio.prestamosEnMoraPdf(desde, hasta, libro);
            return Response.ok(pdf)
                    .type("application/pdf")
                    .header("Content-Disposition", "inline; filename=\"prestamos-en-mora.pdf\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            System.out.println("Error al generar el PDF de préstamos en mora");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "Error interno")).build();
        }
    }

    private boolean esAdmin(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Admin".equals(rol) || "Super Admin".equals(rol);
    }

    private boolean esAdminOBibliotecario(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Admin".equals(rol) || "Super Admin".equals(rol) || "Bibliotecario".equals(rol);
    }

    private boolean esAdminOSecretaria(ContainerRequestContext req) {
        Object rol = req.getProperty("rol");
        return "Admin".equals(rol) || "Super Admin".equals(rol) || "Secretaria".equals(rol);
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "No autorizado para esta acción")).build();
    }
}