package com.mcmovilcare.catalogo;

import jakarta.persistence.*;

@Entity
@Table(name = "equipos")
public class EquipoEntidad {
  @Id private String id;
  private String nombre;
  private String marca;
  private String modelo;
  private String numeroSerie;
  private String estado;
  private String diagnostico;

  public String getId() {
    return id;
  }

  public void setId(String v) {
    id = v;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String v) {
    nombre = v;
  }

  public String getMarca() {
    return marca;
  }

  public void setMarca(String v) {
    marca = v;
  }

  public String getModelo() {
    return modelo;
  }

  public void setModelo(String v) {
    modelo = v;
  }

  public String getNumeroSerie() {
    return numeroSerie;
  }

  public void setNumeroSerie(String v) {
    numeroSerie = v;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String v) {
    estado = v;
  }

  public String getDiagnostico() {
    return diagnostico;
  }

  public void setDiagnostico(String v) {
    diagnostico = v;
  }
}
