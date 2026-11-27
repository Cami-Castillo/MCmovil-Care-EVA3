package com.mcmovilcare.logic

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class ValidacionTest :
    FunSpec({
        val e =
            Equipo(
                nombre = "Notebook",
                marca = "HP",
                modelo = "Pavilion",
                numeroSerie = "123",
                diagnostico = "Pendiente",
            )
        val a =
            Atencion(
                equipoId = "e1",
                descripcion = "No enciende al conectar el cargador",
                correoContacto = "cliente@example.cl",
            )
        val p = Perfil("Alejandrina", "prueba@example.cl", "Dirección ficticia 123", true)
        test("equipo correcto") { Validacion.equipo(e).isEmpty() shouldBe true }
        for ((campo, valor) in
            listOf(
                "nombre" to "",
                "marca" to " ",
                "modelo" to "",
                "numeroSerie" to "",
                "diagnostico" to "",
            )) test("equipo obligatorio $campo") {
            val malo =
                when (campo) {
                    "nombre" -> e.copy(nombre = valor)
                    "marca" -> e.copy(marca = valor)
                    "modelo" -> e.copy(modelo = valor)
                    "numeroSerie" -> e.copy(numeroSerie = valor)
                    else -> e.copy(diagnostico = valor)
                }
            Validacion.equipo(malo).containsKey(campo) shouldBe true
        }
        test("equipo demasiado largo") {
            Validacion.equipo(e.copy(nombre = "x".repeat(201))).containsKey("nombre") shouldBe true
        }
        test("estado equipo incorrecto") {
            Validacion.equipo(e.copy(estado = "Otro")).containsKey("estado") shouldBe true
        }
        test("atencion correcta") { Validacion.atencion(a, setOf("e1")).isEmpty() shouldBe true }
        test("equipo inexistente") {
            Validacion.atencion(a, emptySet()).containsKey("equipoId") shouldBe true
        }
        test("servicio incorrecto") {
            Validacion.atencion(a.copy(tipoServicio = "Otro"), setOf("e1"))
                .containsKey("tipoServicio") shouldBe true
        }
        for (n in listOf(0, 14, 15, 1000, 1001)) test("longitud descripcion $n") {
            Validacion.atencion(a.copy(descripcion = "x".repeat(n)), setOf("e1"))
                .containsKey("descripcion") shouldBe (n !in 15..1000)
        }
        for (email in listOf("malo", "a b@c.cl", "x".repeat(255) + "@a.cl")) test(
            "correo malo $email"
        ) {
            Validacion.atencion(a.copy(correoContacto = email), setOf("e1"))
                .containsKey("correoContacto") shouldBe true
        }
        test("estado atencion invalido") {
            Validacion.atencion(a.copy(estado = "Otro"), setOf("e1")).containsKey("estado") shouldBe
                true
        }
        for (monto in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY, 100000001.0)) test(
            "monto invalido $monto"
        ) {
            Validacion.atencion(a.copy(importe = monto), setOf("e1"))
                .containsKey("importe") shouldBe true
        }
        test("autorizacion invalida") {
            Validacion.atencion(a.copy(autorizacion = "Otro"), setOf("e1"))
                .containsKey("autorizacion") shouldBe true
        }
        test("informe vacio") {
            Validacion.atencion(a.copy(informe = ""), setOf("e1")).containsKey("informe") shouldBe
                true
        }
        test("informe largo") {
            Validacion.atencion(a.copy(informe = "x".repeat(2001)), setOf("e1"))
                .containsKey("informe") shouldBe true
        }
        test("fechas correctas") {
            Validacion.atencion(
                    a.copy(garantiaHasta = "2027-03-01", mantenimiento = "2027-04-01"),
                    setOf("e1"),
                )
                .isEmpty() shouldBe true
        }
        test("fechas invalidas") {
            Validacion.atencion(
                    a.copy(garantiaHasta = "ayer", mantenimiento = "2026-02-30"),
                    setOf("e1"),
                )
                .keys
                .containsAll(listOf("garantiaHasta", "mantenimiento")) shouldBe true
        }
        test("perfil correcto") { Validacion.perfil(p).isEmpty() shouldBe true }
        test("perfil errores por campo") {
            Validacion.perfil(Perfil()).keys shouldBe
                setOf("nombre", "correo", "direccion", "terminos")
        }
        test("perfil largos") {
            Validacion.perfil(
                    p.copy(
                        nombre = "x".repeat(101),
                        correo = "x".repeat(255) + "@a.cl",
                        direccion = "x".repeat(201),
                    )
                )
                .size shouldBe 3
        }
        test("dia laboral sin cambios") {
            Validacion.diaHabil(LocalDate.parse("2026-11-20"), emptyList()).toString() shouldBe
                "2026-11-20"
        }
        test("fin de semana y feriado nacional") {
            Validacion.diaHabil(
                    LocalDate.parse("2026-11-21"),
                    listOf(Feriado("2026-11-23", "Prueba")),
                )
                .toString() shouldBe "2026-11-24"
        }
        test("feriado regional no bloquea agenda nacional") {
            Validacion.diaHabil(
                    LocalDate.parse("2026-11-23"),
                    listOf(Feriado("2026-11-23", "Regional", false)),
                )
                .toString() shouldBe "2026-11-23"
        }
    })
