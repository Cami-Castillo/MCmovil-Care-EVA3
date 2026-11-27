package com.mcmovilcare.catalogo;

import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sesion")
public class SesionController {
  public record Identidad(String usuario, String rol) {}

  public record Entrada(String usuario, String clave) {}

  public record Acceso(String token, String usuario, String rol) {}

  private record Cuenta(Identidad identidad, byte[] sal, byte[] huella) {}

  private record Sesion(Identidad identidad, Instant vence) {}

  private final SecureRandom azar = new SecureRandom();
  private final Map<String, Sesion> activas = new ConcurrentHashMap<>();
  private final Map<String, Cuenta> cuentas;

  public SesionController(Environment env) {
    cuentas =
        Map.of(
            "cliente",
                cuenta(
                    "cliente",
                    "CLIENTE",
                    env.getProperty("MCCARE_CLAVE_CLIENTE", "ClienteDemo26!")),
            "tecnico",
                cuenta(
                    "tecnico",
                    "TECNICO",
                    env.getProperty("MCCARE_CLAVE_TECNICO", "TecnicoDemo26!")),
            "admin",
                cuenta("admin", "ADMIN", env.getProperty("MCCARE_CLAVE_ADMIN", "AdminDemo26!")));
  }

  private Cuenta cuenta(String usuario, String rol, String clave) {
    byte[] sal = new byte[16];
    azar.nextBytes(sal);
    return new Cuenta(new Identidad(usuario, rol), sal, huella(clave, sal));
  }

  private byte[] huella(String clave, byte[] sal) {
    var spec = new PBEKeySpec(clave.toCharArray(), sal, 120_000, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("No se pudo iniciar la autenticación", e);
    } finally {
      spec.clearPassword();
    }
  }

  @PostMapping
  public ResponseEntity<Acceso> entrar(@RequestBody Entrada datos) {
    if (datos.usuario() == null || datos.clave() == null || datos.clave().length() > 128)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    var cuenta = cuentas.get(datos.usuario());
    var comparar = cuenta != null ? cuenta : cuentas.get("cliente");
    boolean correcta =
        MessageDigest.isEqual(comparar.huella(), huella(datos.clave(), comparar.sal()));
    if (!correcta || cuenta == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    var ahora = Instant.now();
    activas.values().removeIf(s -> !ahora.isBefore(s.vence()));
    byte[] bytes = new byte[32];
    azar.nextBytes(bytes);
    var token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    var identidad = cuenta.identidad();
    activas.put(token, new Sesion(identidad, ahora.plusSeconds(28_800)));
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(new Acceso(token, identidad.usuario(), identidad.rol()));
  }

  @GetMapping
  public Identidad consultar(@RequestHeader(HttpHeaders.AUTHORIZATION) String cabecera) {
    var identidad = validar(cabecera);
    if (identidad == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return identidad;
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void salir(@RequestHeader(HttpHeaders.AUTHORIZATION) String cabecera) {
    activas.remove(cabecera.substring(7));
  }

  Identidad validar(String cabecera) {
    if (cabecera == null || !cabecera.startsWith("Bearer ")) return null;
    var token = cabecera.substring(7);
    var sesion = activas.get(token);
    if (sesion == null) return null;
    if (Instant.now().isBefore(sesion.vence())) return sesion.identidad();
    activas.remove(token, sesion);
    return null;
  }
}
