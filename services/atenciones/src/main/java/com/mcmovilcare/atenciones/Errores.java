package com.mcmovilcare.atenciones;

import java.util.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class Errores {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String, Object>> negocio(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode()).body(Map.of("mensaje", e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
    var m = new TreeMap<String, String>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> m.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest().body(Map.of("mensaje", "Revisa los campos", "errores", m));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<Map<String, Object>> json(Exception e) {
    return ResponseEntity.badRequest().body(Map.of("mensaje", "JSON inválido"));
  }
}
