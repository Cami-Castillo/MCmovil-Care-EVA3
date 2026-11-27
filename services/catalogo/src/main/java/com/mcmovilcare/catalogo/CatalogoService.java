package com.mcmovilcare.catalogo;

import static org.springframework.http.HttpStatus.*;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogoService {
  private final EquiposRepo repo;
  private final RestClient otro;

  public CatalogoService(EquiposRepo r, @Value("${otro.url}") String url) {
    repo = r;
    var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(5000);
    otro = RestClient.builder().baseUrl(url).requestFactory(factory).build();
  }

  public List<EquipoEntidad> todos() {
    return repo.findAll(org.springframework.data.domain.Sort.by("nombre"));
  }

  public EquipoEntidad uno(String id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Equipo no encontrado"));
  }

  public EquipoEntidad guardar(String id, EquipoEntrada d) {
    var e = id == null ? new EquipoEntidad() : uno(id);
    if (id == null) e.setId(UUID.randomUUID().toString());
    e.setNombre(d.nombre().trim());
    e.setMarca(d.marca().trim());
    e.setModelo(d.modelo().trim());
    e.setNumeroSerie(d.numeroSerie().trim());
    e.setEstado(d.estado());
    e.setDiagnostico(d.diagnostico().trim());
    return repo.save(e);
  }

  public void eliminar(String id) {
    var e = uno(id);
    boolean usado;
    try {
      usado =
          Boolean.TRUE.equals(
              otro.get()
                  .uri("/internal/equipos/{id}/en-uso", id)
                  .header("Authorization", credencial())
                  .retrieve()
                  .body(Boolean.class));
    } catch (RestClientException ex) {
      throw new ResponseStatusException(
          SERVICE_UNAVAILABLE, "No se pudo verificar el servicio de atenciones");
    }
    if (usado) throw new ResponseStatusException(CONFLICT, "El equipo tiene atenciones");
    repo.delete(e);
  }

  private String credencial() {
    var contexto =
        (org.springframework.web.context.request.ServletRequestAttributes)
            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
    return contexto.getRequest().getHeader("Authorization");
  }
}
