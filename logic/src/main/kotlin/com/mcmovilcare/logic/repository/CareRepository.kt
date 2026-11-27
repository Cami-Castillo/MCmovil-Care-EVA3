package com.mcmovilcare.logic.repository

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

interface Cache {
    suspend fun leer(clave: String): String?

    suspend fun guardar(clave: String, valor: String)
}

interface CareRepository {
    suspend fun equipos(): Datos<Equipo>

    suspend fun atenciones(): Datos<Atencion>

    suspend fun guardarEquipo(e: Equipo)

    suspend fun eliminarEquipo(id: String)

    suspend fun guardarAtencion(a: Atencion)

    suspend fun eliminarAtencion(id: Long)

    suspend fun autorizar(id: Long, estado: String)

    suspend fun feriados(year: Int): List<Feriado>

    suspend fun perfil(): Perfil

    suspend fun guardarPerfil(p: Perfil)

    suspend fun preferencia(): Boolean

    suspend fun preferencia(valor: Boolean)

    suspend fun subir(bytes: ByteArray): String
}

