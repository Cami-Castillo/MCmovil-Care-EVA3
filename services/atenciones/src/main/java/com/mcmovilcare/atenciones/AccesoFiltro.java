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
    var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(2000);
    factory.setReadTimeout(2000);
    catalogo = RestClient.builder().baseUrl(url).requestFactory(factory).build();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    if (req.getRequestURI().equals(req.getContextPath() + "/api/salud")) {
      chain.doFilter(req, res);
      return;
    }
    res.setHeader("Cache-Control", "no-store");
    var credencial = req.getHeader("Authorization");
    if (credencial == null || !credencial.startsWith("Bearer ")) {
      res.setStatus(401);
      return;
    }
    Identidad identidad;
    try {
      identidad =
          catalogo
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
    if ((identidad.rol().equals("CLIENTE") && metodo.equals("PUT"))
        || (identidad.rol().equals("TECNICO")
            && (metodo.equals("PATCH") || metodo.equals("DELETE")))) {
      res.setStatus(403);
      return;
    }
    req.setAttribute("rol", identidad.rol());
    chain.doFilter(req, res);
  }
}
