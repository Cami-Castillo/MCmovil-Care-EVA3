package com.example.appmcmovilcare_castillo_farias.viewmodel

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import com.example.appmcmovilcare_castillo_farias.BuildConfig


import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginViewModel : ViewModel() {
    val pantallas = ViewModelStore()
    private val api =
        Retrofit.Builder()
            .baseUrl(BuildConfig.CATALOGO_URL)
            .client(
                OkHttpClient.Builder()
                    .callTimeout(12, TimeUnit.SECONDS)
                    .followRedirects(false)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SesionApi::class.java)

    private val controller = LoginController(api, viewModelScope)
    val estado = controller.estado

    fun entrar(usuario: String, clave: String) = controller.entrar(usuario, clave)

    fun salir(token: String, vencida: Boolean = false) {
        if (estado.value.sesion?.token != token) return
        controller.salir(token, vencida)
        pantallas.clear()
    }

    override fun onCleared() {
        pantallas.clear()
        super.onCleared()
    }
}
