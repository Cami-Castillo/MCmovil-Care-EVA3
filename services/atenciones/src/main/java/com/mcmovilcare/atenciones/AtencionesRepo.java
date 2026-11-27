package com.mcmovilcare.atenciones;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AtencionesRepo extends JpaRepository<AtencionEntidad, Long> {
  boolean existsByEquipoId(String equipoId);
}
