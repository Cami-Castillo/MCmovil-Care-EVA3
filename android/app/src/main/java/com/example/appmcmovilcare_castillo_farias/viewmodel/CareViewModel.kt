package com.example.appmcmovilcare_castillo_farias.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmcmovilcare_castillo_farias.BuildConfig
import com.example.appmcmovilcare_castillo_farias.data.LocalCache
import com.mcmovilcare.logic.*
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CareViewModel(app: Application, val sesion: Sesion, onSesionVencida: () -> Unit) :
    AndroidViewModel(app) {
    private val client =
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .addInterceptor { chain ->
                val solicitud = chain.request()
                val url = solicitud.url
                val interno =
                    listOf(BuildConfig.CATALOGO_URL, BuildConfig.ATENCIONES_URL)
                        .map { it.toHttpUrl() }
                        .any {
                            it.scheme == url.scheme && it.host == url.host && it.port == url.port
                        }
                val respuesta =
                    chain.proceed(
                        if (interno)
                            solicitud
                                .newBuilder()
                                .header("Authorization", "Bearer ${sesion.token}")
                                .build()
                        else solicitud
                    )
                if (interno && respuesta.code == 401) {
                    viewModelScope.launch(Dispatchers.Main) { onSesionVencida() }
                }
                respuesta
            }
            .build()

    private fun retrofit(url: String) =
        Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val controller =
        CareController(
            RemoteCareRepository(
                retrofit(BuildConfig.CATALOGO_URL).create(CatalogoApi::class.java),
                retrofit(BuildConfig.ATENCIONES_URL).create(AtencionesApi::class.java),
                retrofit("https://date.nager.at/").create(FeriadosApi::class.java),
                LocalCache(app, sesion.usuario),
            ),
            viewModelScope,
        )
    val state = controller.state
    val imagenes =
        coil.ImageLoader.Builder(app)
            .okHttpClient(client)
            .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
            .diskCachePolicy(coil.request.CachePolicy.DISABLED)
            .build()

    private val _foto = MutableStateFlow<String?>(null)
    val foto = _foto.asStateFlow()
    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()
    private val _errorFoto = MutableStateFlow<String?>(null)
    val errorFoto = _errorFoto.asStateFlow()
    var referenciaFoto: String = ""
        private set

    init {
        controller.cargar()
    }

    fun quitarFoto() {
        _foto.value = null
        referenciaFoto = ""
        _errorFoto.value = null
    }

    fun adjuntar(uri: Uri) {
        if (_ocupado.value || state.value.cargando || state.value.guardando) return
        _ocupado.value = true
        _errorFoto.value = null
        viewModelScope.launch {
            try {
                val bytes =
                    withContext(Dispatchers.IO) {
                        getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                            input ->
                            val out = java.io.ByteArrayOutputStream()
                            val buf = ByteArray(8192)
                            var n = input.read(buf)
                            while (n != -1) {
                                require(out.size() + n <= 5 * 1024 * 1024) { "Máximo 5 MiB" }
                                out.write(buf, 0, n)
                                n = input.read(buf)
                            }
                            out.toByteArray()
                        } ?: error("No se pudo leer")
                    }
                require(bytes.size in 1..5 * 1024 * 1024) { "Imagen vacía o mayor a 5 MiB" }
                val app = getApplication<Application>()
                val nombre =
                    withContext(Dispatchers.IO) {
                        val carpeta = File(app.filesDir, "adjuntos").apply { mkdirs() }
                        File.createTempFile("foto_", ".img", carpeta)
                            .apply { writeBytes(bytes) }
                            .name
                    }
                controller.subir(bytes) {
                    referenciaFoto = it
                    _foto.value = nombre
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorFoto.value = e.message ?: "No se pudo adjuntar la imagen"
            } finally {
                _ocupado.value = false
            }
        }
    }

    fun errorFoto(m: String) {
        _errorFoto.value = m
    }
}
