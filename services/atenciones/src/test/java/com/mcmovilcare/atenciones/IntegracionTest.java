package com.mcmovilcare.atenciones;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:atenciones;DB_CLOSE_DELAY=-1",
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
            boolean sesion = x.getRequestURI().getPath().equals("/api/sesion");
            String auth = x.getRequestHeaders().getFirst("Authorization");
            String rol =
                "Bearer cliente-test".equals(auth)
                    ? "CLIENTE"
                    : "Bearer tecnico-test".equals(auth) ? "TECNICO" : "ADMIN";
            String body =
                sesion
                    ? new ObjectMapper().writeValueAsString(Map.of("usuario", "prueba", "rol", rol))
                    : "{}";
            x.getResponseHeaders().set("Content-Type", "application/json");
            x.sendResponseHeaders(
                sesion ? ("Bearer invalido".equals(auth) ? 401 : 200) : codigo.get(),
                body.length());
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

  @Autowired AtencionesRepo repo;

  org.springframework.test.web.servlet.ResultActions autorizado(
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder r)
      throws Exception {
    return mvc.perform(r.header("Authorization", token));
  }

  String token = "Bearer prueba";

  @BeforeEach
  void limpiar() {
    repo.deleteAll();
    codigo.set(200);
  }

  Map<String, Object> entrada() {
    return new HashMap<>(
        Map.ofEntries(
            Map.entry("equipoId", "e1"),
            Map.entry("tipoServicio", "Diagnóstico de falla"),
            Map.entry("descripcion", "El notebook no enciende al conectarlo"),
            Map.entry("correoContacto", "cliente@example.cl"),
            Map.entry("estado", "Recibida"),
            Map.entry("importe", 0),
            Map.entry("autorizacion", "Pendiente"),
            Map.entry("informe", "Pendiente"),
            Map.entry("garantiaHasta", ""),
            Map.entry("mantenimiento", ""),
            Map.entry("evidencia", "")));
  }

  long crear() throws Exception {
    String s =
        autorizado(
                post("/api/atenciones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(entrada())))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json.readTree(s).get("id").asLong();
  }

  @Test
  void salud() throws Exception {
    autorizado(get("/api/salud")).andExpect(status().isOk());
  }

  @Test
  void vacio() throws Exception {
    autorizado(get("/api/atenciones")).andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void crearLeerYEditar() throws Exception {
    long id = crear();
    autorizado(get("/api/atenciones/" + id)).andExpect(jsonPath("$.historial.length()").value(1));
    var d = entrada();
    d.put("informe", "Prueba finalizada");
    d.put("estado", "Finalizada");
    d.put("garantiaHasta", "2027-01-01");
    d.put("mantenimiento", "2027-06-01");
    autorizado(
            put("/api/atenciones/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(d)))
        .andExpect(jsonPath("$.historial.length()").value(2));
    autorizado(get("/api/atenciones")).andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void autorizado() throws Exception {
    long id = crear();
    autorizado(
            patch("/api/atenciones/" + id + "/autorizacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"autorizacion\":\"Aprobada\"}"))
        .andExpect(jsonPath("$.autorizacion").value("Aprobada"));
  }

  @Test
  void autorizadoInvalido() throws Exception {
    long id = crear();
    autorizado(
            patch("/api/atenciones/" + id + "/autorizacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"autorizacion\":\"Otro\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void eliminar() throws Exception {
    long id = crear();
    autorizado(delete("/api/atenciones/" + id)).andExpect(status().isNoContent());
    assertEquals(0, repo.count());
  }

  @Test
  void referencias() throws Exception {
    autorizado(get("/internal/equipos/e1/en-uso")).andExpect(content().string("false"));
    crear();
    autorizado(get("/internal/equipos/e1/en-uso")).andExpect(content().string("true"));
  }

  @Test
  void noExiste() throws Exception {
    autorizado(get("/api/atenciones/9999")).andExpect(status().isNotFound());
  }

  @Test
  void equipoNoExiste() throws Exception {
    codigo.set(404);
    autorizado(
            post("/api/atenciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(entrada())))
        .andExpect(status().isNotFound());
  }

  @Test
  void catalogoCaido() throws Exception {
    codigo.set(503);
    autorizado(
            post("/api/atenciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(entrada())))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void datosInvalidos() throws Exception {
    autorizado(post("/api/atenciones").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void correoInvalido() throws Exception {
    var d = entrada();
    d.put("correoContacto", "malo");
    autorizado(
            post("/api/atenciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(d)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void fechaInvalida() throws Exception {
    var d = entrada();
    d.put("mantenimiento", "2026-02-30");
    autorizado(
            post("/api/atenciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(d)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void jsonInvalido() throws Exception {
    autorizado(post("/api/atenciones").contentType(MediaType.APPLICATION_JSON).content("malo"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void imagenVacia() throws Exception {
    autorizado(
            multipart("/api/evidencias")
                .file(new MockMultipartFile("archivo", "a.jpg", "image/jpeg", new byte[0])))
        .andExpect(status().isBadRequest());
  }

  @Test
  void imagenGrande() throws Exception {
    autorizado(
            multipart("/api/evidencias")
                .file(
                    new MockMultipartFile(
                        "archivo", "a.jpg", "image/jpeg", new byte[5 * 1024 * 1024 + 1])))
        .andExpect(status().isBadRequest());
  }

  @Test
  void imagenInvalida() throws Exception {
    autorizado(
            multipart("/api/evidencias")
                .file(
                    new MockMultipartFile("archivo", "a.jpg", "image/jpeg", new byte[] {1, 2, 3})))
        .andExpect(status().isBadRequest());
  }

  @Test
  void imagenReal() throws Exception {
    var out = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(
        new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB),
        "png",
        out);
    String s =
        autorizado(
                multipart("/api/evidencias")
                    .file(
                        new MockMultipartFile("archivo", "a.png", "image/png", out.toByteArray())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String nombre = json.readTree(s).get("nombre").asText();
    autorizado(get("/api/evidencias/" + nombre)).andExpect(status().isOk());
    var d = entrada();
    d.put("evidencia", nombre);
    autorizado(
            post("/api/atenciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(d)))
        .andExpect(status().isCreated());
  }

  @Test
  void referenciaInvalida() throws Exception {
    autorizado(get("/api/evidencias/malo")).andExpect(status().isBadRequest());
  }

  @Test
  void evidenciaNoExiste() throws Exception {
    autorizado(get("/api/evidencias/00000000-0000-0000-0000-000000000000.img"))
        .andExpect(status().isNotFound());
  }

  @Test
  void bloqueaAtencionesYFotosSinSesion() throws Exception {
    mvc.perform(get("/api/atenciones")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/evidencias/foto")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/atenciones").header("Authorization", "Bearer invalido"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void clienteNoEditaDiagnosticosYTecnicoNoAutoriza() throws Exception {
    mvc.perform(put("/api/atenciones/1").header("Authorization", "Bearer cliente-test"))
        .andExpect(status().isForbidden());
    mvc.perform(
            patch("/api/atenciones/1/autorizacion").header("Authorization", "Bearer tecnico-test"))
        .andExpect(status().isForbidden());
  }

  @Test
  void clienteNoAdelantaEstadoAlCrearSolicitud() throws Exception {
    var datos = entrada();
    datos.put("estado", "Finalizada");
    datos.put("importe", 10000);
    mvc.perform(
            post("/api/atenciones")
                .header("Authorization", "Bearer cliente-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(datos)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.estado").value("Recibida"))
        .andExpect(jsonPath("$.importe").value(0));
  }

  @Test
  void tecnicoNoAutorizaCambiandoElCuerpoDeEdicion() throws Exception {
    long id = crear();
    var datos = entrada();
    datos.put("autorizacion", "Aprobada");
    mvc.perform(
            put("/api/atenciones/" + id)
                .header("Authorization", "Bearer tecnico-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(datos)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.autorizacion").value("Pendiente"));
  }
}
