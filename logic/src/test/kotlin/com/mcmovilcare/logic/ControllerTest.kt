package com.mcmovilcare.logic

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ControllerTest :
    FunSpec({
        val e = Equipo("e1", "Notebook", "HP", "Pavilion", "123", "Recibido", "Pendiente")
        val a =
            Atencion(
                equipoId = "e1",
                descripcion = "No enciende al conectar el cargador",
                correoContacto = "cliente@example.cl",
            )
        val p = Perfil("Cliente", "prueba@example.cl", "Dirección ficticia", true)
        fun repo(): CareRepository = mockk {
            coEvery { equipos() } returns Datos(listOf(e))
            coEvery { atenciones() } returns Datos(listOf(a.copy(id = 1)))
            coEvery { perfil() } returns p
            coEvery { preferencia() } returns true
            coEvery { guardarEquipo(any()) } just Runs
            coEvery { guardarAtencion(any()) } just Runs
            coEvery { eliminarEquipo(any()) } just Runs
            coEvery { eliminarAtencion(any()) } just Runs
            coEvery { autorizar(any(), any()) } just Runs
            coEvery { guardarPerfil(any()) } just Runs
            coEvery { preferencia(any()) } just Runs
            coEvery { subir(any()) } returns "foto.img"
            coEvery { feriados(any()) } returns emptyList()
        }
        test("carga y bloquea cargas duplicadas") {
            runTest {
                val r = repo()
                val c = CareController(r, this)
                c.cargar()
                c.cargar()
                c.state.value.cargando shouldBe true
                advanceUntilIdle()
                c.state.value.equipos.size shouldBe 1
                c.state.value.perfil shouldBe p
                coVerify(exactly = 1) { r.equipos() }
            }
        }
        test("guardar equipo y atencion y eliminar y autorizar") {
            runTest {
                val r = repo()
                val c = CareController(r, this)
                c.cargar()
                advanceUntilIdle()
                c.equipo(e)
                advanceUntilIdle()
                c.atencion(a)
                advanceUntilIdle()
                c.eliminarAtencion(1)
                advanceUntilIdle()
                c.eliminarEquipo("e1")
                advanceUntilIdle()
                c.autorizar(1, "Aprobada")
                advanceUntilIdle()
                c.state.value.revision shouldBe 5
                coVerify {
                    r.guardarEquipo(any())
                    r.guardarAtencion(any())
                    r.eliminarAtencion(1)
                    r.eliminarEquipo("e1")
                    r.autorizar(1, "Aprobada")
                }
            }
        }
        test("invalidos no se envian") {
            runTest {
                val r = repo()
                val c = CareController(r, this)
                c.equipo(Equipo())
                c.atencion(Atencion())
                c.autorizar(1, "malo")
                c.perfil(Perfil())
                advanceUntilIdle()
                c.state.value.errores.isNotEmpty() shouldBe true
                coVerify(exactly = 0) {
                    r.guardarEquipo(any())
                    r.guardarAtencion(any())
                    r.autorizar(any(), any())
                    r.guardarPerfil(any())
                }
            }
        }
        test("perfil preferencia imagen y API externa") {
            runTest {
                val r = repo()
                val c = CareController(r, this)
                c.perfil(p)
                advanceUntilIdle()
                c.preferencia(false)
                advanceUntilIdle()
                var nombre = ""
                c.subir(byteArrayOf(1)) { nombre = it }
                advanceUntilIdle()
                c.feriados(2026)
                advanceUntilIdle()
                c.state.value.mostrarDiagnosticos shouldBe false
                c.state.value.perfil shouldBe p
                nombre shouldBe "foto.img"
                c.state.value.errorExterno shouldBe null
            }
        }
        test("externa puede fallar sin afectar equipos") {
            runTest {
                val r = repo()
                coEvery { r.feriados(any()) } throws IOException()
                val c = CareController(r, this)
                c.cargar()
                advanceUntilIdle()
                c.feriados(2026)
                advanceUntilIdle()
                c.state.value.errorExterno!!.isNotBlank() shouldBe true
                c.state.value.equipos.size shouldBe 1
            }
        }
        test("modo cache bloquea escritura pero permite preferencia") {
            runTest {
                val r = repo()
                coEvery { r.equipos() } returns Datos(listOf(e), true)
                val c = CareController(r, this)
                c.cargar()
                advanceUntilIdle()
                c.eliminarEquipo("e1")
                advanceUntilIdle()
                c.state.value.error!! shouldContain "Sin conexión"
                coVerify(exactly = 0) { r.eliminarEquipo(any()) }
                c.preferencia(false)
                advanceUntilIdle()
                c.state.value.mostrarDiagnosticos shouldBe false
            }
        }
        test("guardado exitoso con refresco fallido") {
            runTest {
                val r = repo()
                coEvery { r.equipos() } throws IOException()
                val c = CareController(r, this)
                c.equipo(e)
                advanceUntilIdle()
                c.state.value.revision shouldBe 1
                c.state.value.error!! shouldContain "Se guardó"
            }
        }
        test("evita doble envio") {
            runTest {
                val r = repo()
                coEvery { r.guardarEquipo(any()) } coAnswers { delay(100) }
                val c = CareController(r, this)
                c.equipo(e)
                c.equipo(e)
                advanceUntilIdle()
                coVerify(exactly = 1) { r.guardarEquipo(any()) }
            }
        }
        for (code in listOf(400, 404, 409, 503, 500)) test("error HTTP $code") {
            runTest {
                val r = repo()
                coEvery { r.equipos() } throws
                    HttpException(Response.error<Any>(code, "error".toResponseBody()))
                val c = CareController(r, this)
                c.cargar()
                advanceUntilIdle()
                c.state.value.error!!.isNotBlank() shouldBe true
                c.state.value.cargando shouldBe false
            }
        }
        test("IOException de carga") {
            runTest {
                val r = repo()
                coEvery { r.equipos() } throws IOException()
                val c = CareController(r, this)
                c.cargar()
                advanceUntilIdle()
                c.state.value.error!! shouldContain "conexión"
            }
        }
        test("excepcion inesperada escritura") {
            runTest {
                val r = repo()
                coEvery { r.guardarEquipo(any()) } throws IllegalStateException()
                val c = CareController(r, this)
                c.equipo(e)
                advanceUntilIdle()
                c.state.value.error!! shouldContain "completar"
                c.state.value.guardando shouldBe false
            }
        }
        test("cancelacion no se transforma en error de usuario") {
            runTest {
                val r = repo()
                coEvery { r.equipos() } throws CancellationException()
                val c = CareController(r, this)
                c.cargar()
                advanceUntilIdle()
                c.state.value.error shouldBe null
                c.state.value.cargando shouldBe false
            }
        }
        test("cancelacion de envio libera guardando") {
            runTest {
                val r = repo()
                coEvery { r.guardarEquipo(any()) } throws CancellationException()
                val c = CareController(r, this)
                c.equipo(e)
                advanceUntilIdle()
                c.state.value.error shouldBe null
                c.state.value.guardando shouldBe false
            }
        }
        test("cancelacion refresco mantiene operacion guardada") {
            runTest {
                val r = repo()
                coEvery { r.equipos() } throws CancellationException()
                val c = CareController(r, this)
                c.equipo(e)
                advanceUntilIdle()
                c.state.value.revision shouldBe 1
                c.state.value.error shouldBe null
            }
        }
        test("cancelacion externa no crea mensaje") {
            runTest {
                val r = repo()
                coEvery { r.feriados(any()) } throws CancellationException()
                val c = CareController(r, this)
                c.feriados(2026)
                advanceUntilIdle()
                c.state.value.errorExterno shouldBe null
            }
        }
    })
