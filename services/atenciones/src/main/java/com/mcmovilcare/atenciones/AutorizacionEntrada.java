package com.mcmovilcare.atenciones;

import jakarta.validation.constraints.*;

public record AutorizacionEntrada(
    @NotBlank @Pattern(regexp = "Pendiente|Aprobada|Rechazada") String autorizacion) {}
