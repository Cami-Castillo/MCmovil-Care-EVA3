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
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    var ruta = req.getRequestURI().substring(req.getContextPath().length());
    var metodo = req.getMethod();
    if (ruta.equals("/api/salud") || (ruta.equals("/api/sesion") && metodo.equals("POST"))) {
      chain.doFilter(req, res);
      return;
    }
    var identidad = sesiones.validar(req.getHeader("Authorization"));
    res.setHeader("Cache-Control", "no-store");
    if (identidad == null) {
      res.setStatus(401);
      return;
    }
    if (identidad.rol().equals("TECNICO") && !metodo.equals("GET") && !ruta.equals("/api/sesion")) {
      res.setStatus(403);
      return;
    }
    chain.doFilter(req, res);
  }
}
