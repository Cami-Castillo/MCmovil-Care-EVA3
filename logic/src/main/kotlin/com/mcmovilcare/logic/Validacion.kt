package com.mcmovilcare.logic

import java.time.*

object Validacion {
    val servicios =
        listOf("Diagnóstico de falla", "Mantenimiento preventivo", "Consulta de garantía")
    val estadosEquipo = listOf("Recibido", "En revisión", "Listo")
    val estadosAtencion =
        listOf("Recibida", "Diagnóstico", "Cotizada", "En reparación", "Finalizada")
    val autorizaciones = listOf("Pendiente", "Aprobada", "Rechazada")
    private val email = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

    fun equipo(e: Equipo): Map<String, String> = buildMap {
        for ((clave, v) in
            listOf(
                "nombre" to e.nombre,
                "marca" to e.marca,
                "modelo" to e.modelo,
                "numeroSerie" to e.numeroSerie,
                "diagnostico" to e.diagnostico,
            )) if (v.isBlank() || v.trim().length > 200)
            put(clave, "Completa entre 1 y 200 caracteres")
        if (e.estado !in estadosEquipo) put("estado", "Estado inválido")
    }

    fun atencion(a: Atencion, ids: Set<String>): Map<String, String> = buildMap {
        if (a.equipoId !in ids) put("equipoId", "Selecciona un equipo disponible")
        if (a.tipoServicio !in servicios) put("tipoServicio", "Servicio inválido")
        if (a.descripcion.trim().length !in 15..1000)
            put("descripcion", "Escribe entre 15 y 1000 caracteres")
        if (!email.matches(a.correoContacto.trim()) || a.correoContacto.length > 254)
            put("correoContacto", "Correo inválido")
        if (a.estado !in estadosAtencion) put("estado", "Estado inválido")
        if (!a.importe.isFinite() || a.importe < 0 || a.importe > 100000000)
            put("importe", "Monto entre 0 y 100000000")
        if (a.autorizacion !in autorizaciones) put("autorizacion", "Autorización inválida")
        if (a.informe.isBlank() || a.informe.length > 2000)
            put("informe", "Informe obligatorio, máximo 2000 caracteres")
        for ((k, v) in
            listOf("garantiaHasta" to a.garantiaHasta, "mantenimiento" to a.mantenimiento)) if (
            v.isNotBlank() && runCatching { LocalDate.parse(v) }.isFailure
        )
            put(k, "Usa AAAA-MM-DD o deja vacío")
    }

    fun perfil(p: Perfil): Map<String, String> = buildMap {
        if (p.nombre.trim().length !in 3..100) put("nombre", "Nombre entre 3 y 100 caracteres")
        if (!email.matches(p.correo.trim()) || p.correo.length > 254)
            put("correo", "Correo inválido")
        if (p.direccion.trim().length !in 5..200)
            put("direccion", "Dirección entre 5 y 200 caracteres")
        if (!p.aceptaTerminos) put("terminos", "Debes aceptar los términos")
    }

    fun diaHabil(fecha: LocalDate, feriados: List<Feriado>): LocalDate {
        val excluidos = feriados.filter { it.global }.map { it.date }.toSet()
        var dia = fecha
        while (
            dia.dayOfWeek == DayOfWeek.SATURDAY ||
                dia.dayOfWeek == DayOfWeek.SUNDAY ||
                dia.toString() in excluidos
        ) dia = dia.plusDays(1)
        return dia
    }
}
