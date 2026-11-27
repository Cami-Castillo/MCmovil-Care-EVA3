package com.mcmovilcare.catalogo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EquipoEntrada(
    @NotBlank @Size(max = 200) String nombre,
    @NotBlank @Size(max = 200) String marca,
    @NotBlank @Size(max = 200) String modelo,
    @NotBlank @Size(max = 200) String numeroSerie,
    @NotBlank @Pattern(regexp = "Recibido|En revisión|Listo") String estado,
    @NotBlank @Size(max = 200) String diagnostico) {}
