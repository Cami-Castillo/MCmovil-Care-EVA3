package com.mcmovilcare.logic.repository

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import com.google.gson.Gson
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteCareRepository(
    private val catalogo: CatalogoApi,
    private val atenciones: AtencionesApi,
    private val externa: FeriadosApi,
    private val cache: Cache,
) : CareRepository {
    private val gson = Gson()

    override suspend fun equipos(): Datos<Equipo> {
        try {
            val lista = catalogo.equipos()
            cache.guardar("equipos", gson.toJson(lista))
            return Datos(lista)
        } catch (e: IOException) {
            val json = cache.leer("equipos") ?: throw e
            return Datos(gson.fromJson(json, Array<Equipo>::class.java).toList(), true)
        }
    }

    override suspend fun atenciones(): Datos<Atencion> {
        try {
            val lista = atenciones.atenciones()
            cache.guardar("atenciones", gson.toJson(lista))
            return Datos(lista)
        } catch (e: IOException) {
            val json = cache.leer("atenciones") ?: throw e
            return Datos(gson.fromJson(json, Array<Atencion>::class.java).toList(), true)
        }
    }

    override suspend fun guardarEquipo(e: Equipo) {
        if (e.id.isBlank()) catalogo.crear(e) else catalogo.editar(e.id, e)
    }

    override suspend fun eliminarEquipo(id: String) = catalogo.eliminar(id)

    override suspend fun guardarAtencion(a: Atencion) {
        if (a.id == 0L) atenciones.crear(a) else atenciones.editar(a.id, a)
    }

    override suspend fun eliminarAtencion(id: Long) = atenciones.eliminar(id)

    override suspend fun autorizar(id: Long, estado: String) {
        atenciones.autorizar(id, EstadoAutorizacion(estado))
    }

    override suspend fun feriados(year: Int) = externa.feriados(year)

    override suspend fun perfil(): Perfil =
        cache.leer("perfil")?.let { gson.fromJson(it, Perfil::class.java) } ?: Perfil()

    override suspend fun guardarPerfil(p: Perfil) = cache.guardar("perfil", gson.toJson(p))

    override suspend fun preferencia(): Boolean = cache.leer("diagnosticos") != "false"

    override suspend fun preferencia(valor: Boolean) =
        cache.guardar("diagnosticos", valor.toString())

    override suspend fun subir(bytes: ByteArray): String {
        require(bytes.isNotEmpty() && bytes.size <= 5 * 1024 * 1024) {
            "Imagen vacía o mayor a 5 MiB"
        }
        return atenciones
            .subir(
                MultipartBody.Part.createFormData(
                    "archivo",
                    "evidencia.jpg",
                    bytes.toRequestBody("image/jpeg".toMediaType()),
                )
            )
            .nombre
    }
}
