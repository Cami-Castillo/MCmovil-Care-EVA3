package com.mcmovilcare.catalogo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:catalogo;DB_CLOSE_DELAY=-1",
      "spring.jpa.hibernate.ddl-auto=create-drop"
    })
@AutoConfigureMockMvc
class IntegracionTest {
  static HttpServer stub;
  static AtomicInteger codigo = new AtomicInteger(200);
  static AtomicBoolean usado = new AtomicBoolean(false);

  static {
    try {
      stub = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      stub.createContext(
          "/",
          x -> {
            String body = usado.get() ? "true" : "false";
            x.getResponseHeaders().set("Content-Type", "application/json");
            x.sendResponseHeaders(codigo.get(), body.length());
            x.getResponseBody().write(body.getBytes());
            x.close();
          });
      stub.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("otro.url", () -> "http://127.0.0.1:" + stub.getAddress().getPort());
    r.add("evidencias.path", () -> "target/evidencias-test");
  }

  @AfterAll
  static void cerrar() {
    stub.stop(0);
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @Autowired EquiposRepo repo;

  org.springframework.test.web.servlet.ResultActions autorizado(
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder r)
      throws Exception {
    return mvc.perform(r.header("Authorization", token));
  }

  String token = "Bearer prueba";

  @BeforeEach
  void limpiar() throws Exception {
    token = ingresar("admin", "AdminDemo26!");

    repo.deleteAll();
    codigo.set(200);
    usado.set(false);
  }

  Map<String, Object> entrada() {
    return new HashMap<>(
        Map.of(
            "nombre",
            "Notebook",
            "marca",
            "HP",
            "modelo",
            "Pavilion",
            "numeroSerie",
            "FICTICIO-001",
            "estado",
            "Recibido",
            "diagnostico",
            "Pendiente"));
  }

  String crear() throws Exception {
    String s =
        autorizado(
                post("/api/equipos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(entrada())))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json.readTree(s).get("id").asText();
  }

  @Test
  void salud() throws Exception {
    autorizado(get("/api/salud")).andExpect(status().isOk());
  }

  @Test
  void vacio() throws Exception {
    autorizado(get("/api/equipos")).andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void crearYLeer() throws Exception {
    String id = crear();
    autorizado(get("/api/equipos/" + id)).andExpect(jsonPath("$.nombre").value("Notebook"));
    autorizado(get("/api/equipos")).andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void editar() throws Exception {
    String id = crear();
    var d = entrada();
    d.put("nombre", "Editado");
    autorizado(
            put("/api/equipos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(d)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Editado"));
  }

  @Test
  void noExiste() throws Exception {
    autorizado(get("/api/equipos/no")).andExpect(status().isNotFound());
  }

  @Test
  void editarNoExiste() throws Exception {
    autorizado(
            put("/api/equipos/no")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(entrada())))
        .andExpect(status().isNotFound());
  }

  @Test
  void borrar() throws Exception {
    String id = crear();
    autorizado(delete("/api/equipos/" + id)).andExpect(status().isNoContent());
    assertEquals(0, repo.count());
  }

  @Test
  void bloquearReferencias() throws Exception {
    String id = crear();
    usado.set(true);
    autorizado(delete("/api/equipos/" + id)).andExpect(status().isConflict());
    assertEquals(1, repo.count());
  }

  @Test
  void bloquearSiOtroServicioFalla() throws Exception {
    String id = crear();
    codigo.set(503);
    autorizado(delete("/api/equipos/" + id)).andExpect(status().isServiceUnavailable());
    assertEquals(1, repo.count());
  }

  @Test
  void datosInvalidos() throws Exception {
    autorizado(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errores.nombre").exists());
  }

  @Test
  void jsonInvalido() throws Exception {
    autorizado(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content("no-json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void estadoInvalido() throws Exception {
    var d = entrada();
    d.put("estado", "otro");
    autorizado(
            post("/api/equipos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(d)))
        .andExpect(status().isBadRequest());
  }

  private String ingresar(String usuario, String clave) throws Exception {
    var respuesta =
        mvc.perform(
                post("/api/sesion")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("usuario", usuario, "clave", clave))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return "Bearer " + json.readTree(respuesta).get("token").asText();
  }

  @Test
  void noEntregaDatosSinSesionYValidaClaves() throws Exception {
    mvc.perform(get("/api/equipos")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/equipos").header("Authorization", "Bearer inventado"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/sesion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(Map.of("usuario", "cliente", "clave", "equivocada"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void rolYRevocacionSeAplicanEnServidor() throws Exception {
    var cliente = ingresar("cliente", "ClienteDemo26!");
    mvc.perform(get("/api/sesion").header("Authorization", cliente))
        .andExpect(jsonPath("$.rol").value("CLIENTE"));
    var tecnico = ingresar("tecnico", "TecnicoDemo26!");
    mvc.perform(delete("/api/equipos/cualquiera").header("Authorization", tecnico))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/sesion").header("Authorization", tecnico))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/equipos").header("Authorization", tecnico))
        .andExpect(status().isUnauthorized());
  }
}
