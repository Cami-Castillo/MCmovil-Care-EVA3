package com.mcmovilcare.atenciones;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AtencionesController {
  private final AtencionesService s;
  private final EvidenciasService archivos;

  public AtencionesController(AtencionesService s, EvidenciasService a) {
    this.s = s;
    archivos = a;
  }

  @GetMapping("/api/salud")
  public Map<String, String> salud() {
    return Map.of("servicio", "atenciones", "estado", "OK");
  }

  @GetMapping("/api/atenciones")
  public List<AtencionEntidad> todos() {
    return s.todos();
  }

  @GetMapping("/api/atenciones/{id}")
  public AtencionEntidad uno(@PathVariable Long id) {
    return s.uno(id);
  }

  @PostMapping("/api/atenciones")
  public ResponseEntity<AtencionEntidad> crear(
      @Valid @RequestBody AtencionEntrada d, @RequestAttribute("rol") String rol) {
    if (!rol.equals("ADMIN")) {
      d =
          new AtencionEntrada(
              d.equipoId(),
              d.tipoServicio(),
              d.descripcion(),
              d.correoContacto(),
              "Recibida",
              0.0,
              "Pendiente",
              "Pendiente",
              "",
              "",
              d.evidencia());
    }
    var a = s.guardar(null, d);
    return ResponseEntity.created(URI.create("/api/atenciones/" + a.getId())).body(a);
  }

  @PutMapping("/api/atenciones/{id}")
  public AtencionEntidad editar(@PathVariable Long id, @Valid @RequestBody AtencionEntrada d) {
    return s.guardar(id, d);
  }

  @PatchMapping("/api/atenciones/{id}/autorizacion")
  public AtencionEntidad autorizar(
      @PathVariable Long id, @Valid @RequestBody AutorizacionEntrada d) {
    return s.autorizar(id, d);
  }

  @DeleteMapping("/api/atenciones/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    s.eliminar(id);
  }

  @GetMapping("/internal/equipos/{id}/en-uso")
  public boolean enUso(@PathVariable String id) {
    return s.enUso(id);
  }

  @PostMapping("/api/evidencias")
  public Map<String, String> subir(@RequestParam MultipartFile archivo) {
    return Map.of("nombre", archivos.subir(archivo));
  }

  @GetMapping("/api/evidencias/{nombre}")
  public ResponseEntity<byte[]> imagen(@PathVariable String nombre) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(archivos.leer(nombre));
  }
}
