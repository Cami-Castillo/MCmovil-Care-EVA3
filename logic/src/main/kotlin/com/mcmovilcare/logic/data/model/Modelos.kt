package com.mcmovilcare.logic.data.model

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

data class Equipo(
    val id: String = "",
    val nombre: String = "",
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val estado: String = "Recibido",
    val diagnostico: String = "Pendiente",
)

data class Atencion(
    val id: Long = 0,
    val equipoId: String = "",
    val tipoServicio: String = "Diagnóstico de falla",
    val descripcion: String = "",
    val correoContacto: String = "",
    val estado: String = "Recibida",
    val importe: Double = 0.0,
    val autorizacion: String = "Pendiente",
    val informe: String = "Pendiente",
    val garantiaHasta: String = "",
    val mantenimiento: String = "",
    val evidencia: String = "",
    val historial: List<String> = emptyList(),
)

data class Perfil(
    val nombre: String = "",
    val correo: String = "",
    val direccion: String = "",
    val aceptaTerminos: Boolean = false,
)

data class Feriado(val date: String, val localName: String, val global: Boolean = true)

data class EstadoAutorizacion(val autorizacion: String)

data class ArchivoSubido(val nombre: String)

data class Datos<T>(val items: List<T>, val cache: Boolean = false)

data class CareState(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val equipos: List<Equipo> = emptyList(),
    val atenciones: List<Atencion> = emptyList(),
    val feriados: List<Feriado> = emptyList(),
    val perfil: Perfil = Perfil(),
    val mostrarDiagnosticos: Boolean = true,
    val error: String? = null,
    val errorExterno: String? = null,
    val mensaje: String? = null,
    val offline: Boolean = false,
    val revision: Int = 0,
    val errores: Map<String, String> = emptyMap(),
)
