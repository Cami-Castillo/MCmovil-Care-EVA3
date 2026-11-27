package com.mcmovilcare.atenciones;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "atenciones")
public class AtencionEntidad {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String equipoId;
  private String tipoServicio;

  @Column(length = 2000)
  private String descripcion;

  private String correoContacto;
  private String estado;
  private Double importe;
  private String autorizacion;

  @Column(length = 2000)
  private String informe;

  private String garantiaHasta;
  private String mantenimiento;
  private String evidencia;

  @ElementCollection(fetch = FetchType.EAGER)
  @OrderColumn
  private List<String> historial = new ArrayList<>();

  public List<String> getHistorial() {
    return historial;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

  public String getEquipoId() {
    return equipoId;
  }

  public void setEquipoId(String v) {
    equipoId = v;
  }

  public String getTipoServicio() {
    return tipoServicio;
  }

  public void setTipoServicio(String v) {
    tipoServicio = v;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String v) {
    descripcion = v;
  }

  public String getCorreoContacto() {
    return correoContacto;
  }

  public void setCorreoContacto(String v) {
    correoContacto = v;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String v) {
    estado = v;
  }

  public Double getImporte() {
    return importe;
  }

  public void setImporte(Double v) {
    importe = v;
  }

  public String getAutorizacion() {
    return autorizacion;
  }

  public void setAutorizacion(String v) {
    autorizacion = v;
  }

  public String getInforme() {
    return informe;
  }

  public void setInforme(String v) {
    informe = v;
  }

  public String getGarantiaHasta() {
    return garantiaHasta;
  }

  public void setGarantiaHasta(String v) {
    garantiaHasta = v;
  }

  public String getMantenimiento() {
    return mantenimiento;
  }

  public void setMantenimiento(String v) {
    mantenimiento = v;
  }

  public String getEvidencia() {
    return evidencia;
  }

  public void setEvidencia(String v) {
    evidencia = v;
  }
}
