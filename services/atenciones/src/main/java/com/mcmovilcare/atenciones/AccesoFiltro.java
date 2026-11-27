
package com.mcmovilcare.atenciones;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AccesoFiltro extends OncePerRequestFilter {

    public record Identidad(String usuario, String rol) {}

    private final RestClient catalogo;

    public AccesoFiltro(@Value("${otro.url}") String url) {
        var factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(2000);
        factory.setReadTimeout(2000);

        catalogo = RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws ServletException, IOException {

        // Endpoint público para verificar el servicio.
        if (req.getRequestURI().equals(
                req.getContextPath() + "/api/salud")) {
            chain.doFilter(req, res);
            return;
        }

        res.setHeader("Cache-Control", "no-store");

        // Comprobar la existencia del token.
        var credencial = req.getHeader("Authorization");

        if (credencial == null || !credencial.startsWith("Bearer ")) {
            res.setStatus(401);
            return;
        }

        Identidad identidad;

        // Validar la sesión mediante el microservicio Catálogo.
        try {
            identidad = catalogo
                    .get()
                    .uri("/api/sesion")
                    .header("Authorization", credencial)
                    .retrieve()
                    .body(Identidad.class);

        } catch (HttpClientErrorException.Unauthorized e) {
            res.setStatus(401);
            return;

        } catch (RestClientException e) {
            res.setStatus(503);
            return;
        }

        if (identidad == null || identidad.rol() == null) {
            res.setStatus(503);
            return;
        }

            
        var metodo = req.getMethod();
        var rol = identidad.rol();
        var ruta = req.getRequestURI()
                .substring(req.getContextPath().length());

        // Validar que el rol sea reconocido.
        if (!rol.equals("ADMIN")
                && !rol.equals("CLIENTE")
                && !rol.equals("TECNICO")) {
            res.setStatus(403);
            return;
        }

        // Restricciones del cliente.
        if (rol.equals("CLIENTE")
                && (metodo.equals("PUT")
                || metodo.equals("DELETE"))) {
            res.setStatus(403);
            return;
        }

        // Restricciones del técnico.
        if (rol.equals("TECNICO")
                && (metodo.equals("PATCH")
                || metodo.equals("DELETE")
                || (metodo.equals("POST")
                && ruta.equals("/api/atenciones")))) {
            res.setStatus(403);
            return;
        }


        req.setAttribute("rol", rol);

        chain.doFilter(req, res);
    }
}
