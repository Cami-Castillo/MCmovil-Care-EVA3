
package com.mcmovilcare.catalogo;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AccesoFiltro extends OncePerRequestFilter {

    private final SesionController sesiones;

    public AccesoFiltro(SesionController sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws ServletException, IOException {

        var ruta = req.getRequestURI()
                .substring(req.getContextPath().length());

        var metodo = req.getMethod();

        // Endpoints públicos: estado del servicio e inicio de sesión.
        if (ruta.equals("/api/salud") ||
                (ruta.equals("/api/sesion") && metodo.equals("POST"))) {

            chain.doFilter(req, res);
            return;
        }

        // Validar el token de autenticación.
        var identidad = sesiones.validar(
                req.getHeader("Authorization"));

        res.setHeader("Cache-Control", "no-store");

        // Rechazar solicitudes sin una sesión válida.
        if (identidad == null) {
            res.setStatus(401);
            return;
        }

        // Solo el administrador puede realizar operaciones
        // distintas de GET, excepto las relacionadas con la sesión.
        if (!identidad.rol().equals("ADMIN")
                && !metodo.equals("GET")
                && !ruta.equals("/api/sesion")) {

            res.setStatus(403);
            return;
        }

        chain.doFilter(req, res);
    }
}
