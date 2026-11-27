package com.mcmovilcare.atenciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AtencionEntrada(
    @NotBlank String equipoId,
    @NotBlank
        @Pattern(regexp = "Diagnóstico de falla|Mantenimiento preventivo|Consulta de garantía")
        String tipoServicio,
    @NotBlank @Size(min = 15, max = 1000) String descripcion,
    @NotBlank @Email @Size(max = 254) String correoContacto,
    @NotBlank @Pattern(regexp = "Recibida|Diagnóstico|Cotizada|En reparación|Finalizada")
        String estado,
    @NotNull @DecimalMin("0") @DecimalMax("100000000") Double importe,
    @NotBlank @Pattern(regexp = "Pendiente|Aprobada|Rechazada") String autorizacion,
    @NotBlank @Size(max = 2000) String informe,
    String garantiaHasta,
    String mantenimiento,
    String evidencia) {
  public AtencionEntrada {
    if (descripcion != null) descripcion = descripcion.trim();
    if (correoContacto != null) correoContacto = correoContacto.trim();
  }
}
