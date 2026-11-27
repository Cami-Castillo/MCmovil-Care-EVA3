package com.mcmovilcare.logic

import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException

data class LoginEstado(
    val sesion: Sesion? = null,
    val cargando: Boolean = false,
    val error: String? = null,
)

class LoginController(private val api: SesionApi, private val scope: CoroutineScope) {
    private val _estado = MutableStateFlow(LoginEstado())
    val estado = _estado.asStateFlow()

    fun entrar(usuario: String, clave: String) {
        if (_estado.value.cargando || _estado.value.sesion != null) return
        if (usuario.isBlank() || clave.isBlank()) {
            _estado.value = LoginEstado(error = "Completa usuario y contraseña")
            return
        }
        _estado.value = LoginEstado(cargando = true)
        scope.launch {
            try {
                _estado.value =
                    LoginEstado(sesion = api.entrar(EntradaSesion(usuario.trim(), clave)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                _estado.value =
                    LoginEstado(
                        error =
                            if (e.code() == 401) "Usuario o contraseña incorrectos"
                            else "El servicio de acceso no está disponible"
                    )
            } catch (e: IOException) {
                _estado.value =
                    LoginEstado(
                        error = "No se pudo conectar. Revisa los servicios y vuelve a intentar."
                    )
            } catch (e: Exception) {
                _estado.value = LoginEstado(error = "No se pudo iniciar sesión.")
            }
        }
    }

    fun salir(token: String, vencida: Boolean = false) {
        if (_estado.value.sesion?.token != token) return
        _estado.value =
            LoginEstado(error = if (vencida) "La sesión terminó. Vuelve a ingresar." else null)
        if (!vencida)
            scope.launch {
                try {
                    api.salir("Bearer $token")
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // El cierre local ya se efectuó; el servidor caduca la sesión si no recibió
                    // DELETE.
                }
            }
    }
}
