package com.mcmovilcare.logic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.io.IOException
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LoginTest :
    StringSpec({
        "sin credenciales no se llama la API" {
            runTest {
                val api = mockk<SesionApi>()
                val vm = LoginController(api, this)
                vm.entrar(" ", "demo")
                vm.estado.value.error shouldBe "Completa usuario y contraseña"
                coVerify(exactly = 0) { api.entrar(any()) }
            }
        }

        "credenciales correctas permiten entrar una sola vez y salir revoca el token" {
            runTest {
                val api = mockk<SesionApi>()
                val acceso = Sesion("token-demo", "cliente", "CLIENTE")
                coEvery { api.entrar(EntradaSesion("cliente", "demo")) } returns acceso
                coEvery { api.salir("Bearer token-demo") } returns Unit
                val vm = LoginController(api, this)
                vm.entrar(" cliente ", "demo")
                vm.entrar("cliente", "demo")
                vm.estado.value.cargando shouldBe true
                runCurrent()
                vm.estado.value.sesion shouldBe acceso
                vm.salir("otra-sesion", true)
                vm.estado.value.sesion shouldBe acceso
                vm.salir(acceso.token)
                vm.estado.value.sesion shouldBe null
                runCurrent()
                coVerify(exactly = 1) { api.entrar(any()) }
                coVerify(exactly = 1) { api.salir("Bearer token-demo") }
            }
        }

        "si caduca la sesión se vuelve al login sin intentar revocar" {
            runTest {
                val api = mockk<SesionApi>()
                coEvery { api.entrar(any()) } returns Sesion("abc", "cliente", "CLIENTE")
                val vm = LoginController(api, this)
                vm.entrar("cliente", "demo")
                runCurrent()
                vm.salir("abc", true)
                vm.estado.value.sesion shouldBe null
                vm.estado.value.error shouldBe "La sesión terminó. Vuelve a ingresar."
                coVerify(exactly = 0) { api.salir(any()) }
            }
        }

        "401 se muestra como error de credenciales; caída como error de conexión" {
            runTest {
                val api = mockk<SesionApi>()
                coEvery { api.entrar(any()) } throws
                    HttpException(Response.error<Any>(401, "".toResponseBody()))
                val vm = LoginController(api, this)
                vm.entrar("cliente", "mala")
                runCurrent()
                vm.estado.value.sesion shouldBe null
                vm.estado.value.error shouldBe "Usuario o contraseña incorrectos"
                coEvery { api.entrar(any()) } throws IOException()
                vm.entrar("cliente", "demo")
                runCurrent()
                vm.estado.value.error shouldBe
                    "No se pudo conectar. Revisa los servicios y vuelve a intentar."
            }
        }
    })
