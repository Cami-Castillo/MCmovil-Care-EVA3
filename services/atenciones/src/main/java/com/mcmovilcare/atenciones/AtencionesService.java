package com.mcmovilcare.atenciones;

import static org.springframework.http.HttpStatus.*;

import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AtencionesService {
  private final AtencionesRepo repo;
  private final RestClient catalogo;
  private final EvidenciasService archivos;

  public AtencionesService(
      AtencionesRepo r, @Value("${otro.url}") String url, EvidenciasService a) {
    repo = r;
    var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(5000);
    catalogo = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    archivos = a;
  }

  public List<AtencionEntidad> todos() {
    return repo.findAll(
        org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "id"));
  }

  public AtencionEntidad uno(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Atención no encontrada"));
  }

  public boolean enUso(String id) {
    return repo.existsByEquipoId(id);
  }

  public AtencionEntidad guardar(Long id, AtencionEntrada d) {
    try {
      catalogo
          .get()
          .uri("/api/equipos/{id}", d.equipoId())
          .header("Authorization", credencial())
          .retrieve()
          .toBodilessEntity();
    } catch (HttpClientErrorException.NotFound e) {
      throw new ResponseStatusException(NOT_FOUND, "Equipo inexistente");
    } catch (RestClientException e) {
      throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Catálogo no disponible");
    }
    fecha(d.garantiaHasta());
    fecha(d.mantenimiento());
    if (d.evidencia() != null && !d.evidencia().isBlank()) archivos.leer(d.evidencia());
    var a = id == null ? new AtencionEntidad() : uno(id);
    a.setEquipoId(d.equipoId());
    a.setTipoServicio(d.tipoServicio());
    a.setDescripcion(d.descripcion());
    a.setCorreoContacto(d.correoContacto());
    a.setEstado(d.estado());
    a.setImporte(d.importe());
    if (id == null) a.setAutorizacion(d.autorizacion());
    a.setInforme(d.informe());
    a.setGarantiaHasta(d.garantiaHasta());
    a.setMantenimiento(d.mantenimiento());
    a.setEvidencia(d.evidencia());
    a.getHistorial()
        .add(Instant.now() + " · " + (id == null ? "Creada" : "Actualizada") + " · " + d.estado());
    return repo.save(a);
  }

  private void fecha(String valor) {
    if (valor != null && !valor.isBlank())
      try {
        LocalDate.parse(valor);
      } catch (DateTimeException e) {
        throw new ResponseStatusException(BAD_REQUEST, "Fecha inválida: usa AAAA-MM-DD");
      }
  }

  public AtencionEntidad autorizar(Long id, AutorizacionEntrada d) {
    var a = uno(id);
    a.setAutorizacion(d.autorizacion());
    a.getHistorial().add(Instant.now() + " · Autorización " + d.autorizacion());
    return repo.save(a);
  }

  public void eliminar(Long id) {
    repo.delete(uno(id));
  }

  private String credencial() {
    var contexto =
        (org.springframework.web.context.request.ServletRequestAttributes)
            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
    return contexto.getRequest().getHeader("Authorization");
  }
}
