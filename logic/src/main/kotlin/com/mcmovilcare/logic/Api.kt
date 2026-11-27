package com.mcmovilcare.logic

import okhttp3.MultipartBody
import retrofit2.http.*

interface CatalogoApi {
    @GET("api/equipos") suspend fun equipos(): List<Equipo>

    @POST("api/equipos") suspend fun crear(@Body e: Equipo): Equipo

    @PUT("api/equipos/{id}") suspend fun editar(@Path("id") id: String, @Body e: Equipo): Equipo

    @DELETE("api/equipos/{id}") suspend fun eliminar(@Path("id") id: String)
}

interface AtencionesApi {
    @GET("api/atenciones") suspend fun atenciones(): List<Atencion>

    @POST("api/atenciones") suspend fun crear(@Body a: Atencion): Atencion

    @PUT("api/atenciones/{id}")
    suspend fun editar(@Path("id") id: Long, @Body a: Atencion): Atencion

    @PATCH("api/atenciones/{id}/autorizacion")
    suspend fun autorizar(@Path("id") id: Long, @Body estado: EstadoAutorizacion): Atencion

    @DELETE("api/atenciones/{id}") suspend fun eliminar(@Path("id") id: Long)

    @Multipart
    @POST("api/evidencias")
    suspend fun subir(@Part archivo: MultipartBody.Part): ArchivoSubido
}

interface FeriadosApi {
    @GET("api/v3/PublicHolidays/{year}/CL")
    suspend fun feriados(@Path("year") year: Int): List<Feriado>
}

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

data class EntradaSesion(val usuario: String, val clave: String)

data class Sesion(val token: String, val usuario: String, val rol: String)

interface SesionApi {
    @POST("api/sesion") suspend fun entrar(@Body datos: EntradaSesion): Sesion

    @DELETE("api/sesion") suspend fun salir(@Header("Authorization") cabecera: String)
}
