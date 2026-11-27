package com.mcmovilcare.logic.data.remote

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

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

interface SesionApi {
    @POST("api/sesion") suspend fun entrar(@Body datos: EntradaSesion): Sesion

    @DELETE("api/sesion") suspend fun salir(@Header("Authorization") cabecera: String)
}
