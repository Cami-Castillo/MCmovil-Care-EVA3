package com.mcmovilcare.logic

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.io.IOException
import kotlinx.coroutines.test.runTest

class RepositoryTest :
    FunSpec({
        fun construir(): Triple<RemoteCareRepository, CatalogoApi, AtencionesApi> {
            val c = mockk<CatalogoApi>()
            val a = mockk<AtencionesApi>()
            val f = mockk<FeriadosApi>()
            val cache = mockk<Cache>()
            coEvery { cache.guardar(any(), any()) } just Runs
            return Triple(RemoteCareRepository(c, a, f, cache), c, a)
        }
        val e = Equipo(id = "e1")
        val a = Atencion(id = 1)
        test("GET equipos guarda cache") {
            runTest {
                val (r, c, _) = construir()
                coEvery { c.equipos() } returns listOf(e)
                r.equipos() shouldBe Datos(listOf(e))
            }
        }
        test("GET atenciones guarda cache") {
            runTest {
                val (r, _, api) = construir()
                coEvery { api.atenciones() } returns listOf(a)
                r.atenciones() shouldBe Datos(listOf(a))
            }
        }
        test("POST y PUT equipos") {
            runTest {
                val (r, c, _) = construir()
                coEvery { c.crear(any()) } returns e
                coEvery { c.editar(any(), any()) } returns e
                r.guardarEquipo(Equipo())
                r.guardarEquipo(e)
                coVerify {
                    c.crear(any())
                    c.editar("e1", e)
                }
            }
        }
        test("DELETE equipo") {
            runTest {
                val (r, c, _) = construir()
                coEvery { c.eliminar(any()) } just Runs
                r.eliminarEquipo("e1")
                coVerify { c.eliminar("e1") }
            }
        }
        test("POST y PUT atenciones") {
            runTest {
                val (r, _, api) = construir()
                coEvery { api.crear(any()) } returns a
                coEvery { api.editar(any(), any()) } returns a
                r.guardarAtencion(Atencion())
                r.guardarAtencion(a)
                coVerify {
                    api.crear(any())
                    api.editar(1, a)
                }
            }
        }
        test("DELETE y PATCH atencion") {
            runTest {
                val (r, _, api) = construir()
                coEvery { api.eliminar(any()) } just Runs
                coEvery { api.autorizar(any(), any()) } returns a
                r.eliminarAtencion(1)
                r.autorizar(1, "Aprobada")
                coVerify {
                    api.eliminar(1)
                    api.autorizar(1, EstadoAutorizacion("Aprobada"))
                }
            }
        }
        test("cache y configuracion reales con JSON") {
            runTest {
                val c = mockk<CatalogoApi>()
                val a = mockk<AtencionesApi>()
                val f = mockk<FeriadosApi>()
                val memoria = mutableMapOf<String, String>()
                val cache =
                    object : Cache {
                        override suspend fun leer(clave: String) = memoria[clave]

                        override suspend fun guardar(clave: String, valor: String) {
                            memoria[clave] = valor
                        }
                    }
                val r = RemoteCareRepository(c, a, f, cache)
                r.perfil() shouldBe Perfil()
                r.preferencia() shouldBe true
                r.preferencia(false)
                r.preferencia() shouldBe false
                val p = Perfil("Cliente", "a@b.cl", "Ficticia 123", true)
                r.guardarPerfil(p)
                r.perfil() shouldBe p
                coEvery { c.equipos() } returns listOf(e)
                coEvery { a.atenciones() } returns listOf(Atencion(id = 1))
                r.equipos()
                r.atenciones()
                coEvery { c.equipos() } throws IOException()
                coEvery { a.atenciones() } throws IOException()
                r.equipos().cache shouldBe true
                r.atenciones().cache shouldBe true
                memoria.clear()
                shouldThrow<IOException> { r.equipos() }
                shouldThrow<IOException> { r.atenciones() }
                coEvery { f.feriados(2026) } returns listOf(Feriado("2026-01-01", "Año nuevo"))
                r.feriados(2026).size shouldBe 1
                coEvery { a.subir(any()) } returns ArchivoSubido("foto.img")
                r.subir(byteArrayOf(1, 2)) shouldBe "foto.img"
                shouldThrow<IllegalArgumentException> { r.subir(byteArrayOf()) }
                shouldThrow<IllegalArgumentException> { r.subir(ByteArray(5 * 1024 * 1024 + 1)) }
            }
        }
    })
