package com.mcmovilcare.catalogo;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class CatalogoController {
  private final CatalogoService s;

  public CatalogoController(CatalogoService s) {
    this.s = s;
  }

  @GetMapping("/api/salud")
  public Map<String, String> salud() {
    return Map.of("servicio", "catalogo", "estado", "OK");
  }

  @GetMapping("/api/equipos")
  public List<EquipoEntidad> todos() {
    return s.todos();
  }

  @GetMapping("/api/equipos/{id}")
  public EquipoEntidad uno(@PathVariable String id) {
    return s.uno(id);
  }

  @PostMapping("/api/equipos")
  public ResponseEntity<EquipoEntidad> crear(@Valid @RequestBody EquipoEntrada d) {
    var e = s.guardar(null, d);
    return ResponseEntity.created(URI.create("/api/equipos/" + e.getId())).body(e);
  }

  @PutMapping("/api/equipos/{id}")
  public EquipoEntidad editar(@PathVariable String id, @Valid @RequestBody EquipoEntrada d) {
    return s.guardar(id, d);
  }

  @DeleteMapping("/api/equipos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable String id) {
    s.eliminar(id);
  }
}
