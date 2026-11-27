package com.mcmovilcare.atenciones;

import static org.springframework.http.HttpStatus.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EvidenciasService {
  private final Path root;

  public EvidenciasService(@Value("${evidencias.path:./datos/evidencias}") String path) {
    root = Path.of(path).toAbsolutePath();
  }

  public String subir(MultipartFile file) {
    if (file.isEmpty() || file.getSize() > 5L * 1024 * 1024)
      throw new ResponseStatusException(BAD_REQUEST, "Imagen vacía o mayor a 5 MiB");
    try {
      byte[] bytes = file.getBytes();
      try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
        var lectores = ImageIO.getImageReaders(input);
        if (!lectores.hasNext())
          throw new ResponseStatusException(BAD_REQUEST, "No es una imagen válida");
        var lector = lectores.next();
        try {
          lector.setInput(input);
          if ((long) lector.getWidth(0) * lector.getHeight(0) > 40000000)
            throw new ResponseStatusException(BAD_REQUEST, "Imagen demasiado grande");
        } finally {
          lector.dispose();
        }
      }
      String nombre = UUID.randomUUID() + ".img";
      Files.createDirectories(root);
      Files.write(root.resolve(nombre), bytes);
      return nombre;
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "No se pudo guardar la evidencia");
    }
  }

  public byte[] leer(String nombre) {
    if (nombre == null || !nombre.matches("[a-f0-9-]{36}[.]img"))
      throw new ResponseStatusException(BAD_REQUEST, "Referencia inválida");
    try {
      return Files.readAllBytes(root.resolve(nombre));
    } catch (IOException e) {
      throw new ResponseStatusException(NOT_FOUND, "Evidencia no encontrada");
    }
  }
}
