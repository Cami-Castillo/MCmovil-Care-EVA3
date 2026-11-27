package com.example.appmcmovilcare_castillo_farias.ui.screens

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.appmcmovilcare_castillo_farias.BuildConfig
import com.example.appmcmovilcare_castillo_farias.R
import com.example.appmcmovilcare_castillo_farias.ui.components.AdjuntoImagen
import com.example.appmcmovilcare_castillo_farias.viewmodel.CareViewModel

import java.time.LocalDate

import com.example.appmcmovilcare_castillo_farias.ui.components.*

@Composable
fun AgendaScreen(s: CareState, cargar: (Int) -> Unit) {
    var mesTexto by rememberSaveable { mutableStateOf(java.time.YearMonth.now().toString()) }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val mes = java.time.YearMonth.parse(mesTexto)
    val year = mes.year
    val locale = java.util.Locale.forLanguageTag("es-CL")
    val formato = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    val feriados = s.feriados.filter { it.date.startsWith("$year-") }
    val nacionales = feriados.filter { it.global }.map { it.date }.toSet()
    val mantenciones = s.atenciones.mapNotNull { a ->
        runCatching { LocalDate.parse(a.mantenimiento) }.getOrNull()?.let { it to a }
    }.sortedBy { it.first }
    val dia = runCatching { LocalDate.parse(fecha) }.getOrNull()
    LaunchedEffect(year) { cargar(year) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Encabezado("Agenda de mantenciones", "Organiza los servicios y revisa si coinciden con días no hábiles.", Icons.Default.DateRange)
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { mesTexto = mes.minusMonths(1).toString(); fecha = mes.minusMonths(1).atDay(1).toString() }) { Icon(Icons.Default.KeyboardArrowLeft, "Mes anterior") }
                    Text(mes.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", locale)).replaceFirstChar { it.uppercase() }, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    IconButton(onClick = { mesTexto = mes.plusMonths(1).toString(); fecha = mes.plusMonths(1).atDay(1).toString() }) { Icon(Icons.Default.KeyboardArrowRight, "Mes siguiente") }
                }
                Row(Modifier.fillMaxWidth()) {
                    listOf("L", "M", "M", "J", "V", "S", "D").forEach { letra ->
                        Text(letra, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                val inicio = mes.atDay(1).dayOfWeek.value - 1
                val filas = (inicio + mes.lengthOfMonth() + 6) / 7
                repeat(filas) { fila ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { col ->
                            val numero = fila * 7 + col - inicio + 1
                            Box(Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                                if (numero in 1..mes.lengthOfMonth()) {
                                    val d = mes.atDay(numero)
                                    val festivo = d.toString() in nacionales
                                    val servicio = mantenciones.any { it.first == d }
                                    val elegido = d == dia
                                    val fondo = when {
                                        elegido -> MaterialTheme.colorScheme.primary
                                        servicio -> MaterialTheme.colorScheme.primaryContainer
                                        festivo -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                    val tinta = when {
                                        elegido -> MaterialTheme.colorScheme.onPrimary
                                        festivo -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    Surface(onClick = { fecha = d.toString() }, shape = MaterialTheme.shapes.small, color = fondo, modifier = Modifier.fillMaxSize().padding(1.dp)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                            Text(numero.toString(), color = tinta)
                                            if (servicio || festivo) Text(if (servicio && festivo) "M F" else if (servicio) "M" else "F", color = tinta, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Text("M = mantención · F = feriado nacional", style = MaterialTheme.typography.bodySmall)
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Planificación de servicios", style = MaterialTheme.typography.titleLarge)
                Text("Fecha seleccionada: ${dia?.format(formato) ?: fecha}")
                if (dia != null) {
                    feriados.filter { it.date == dia.toString() }.forEach { Text(it.localName + if (it.global) " · Feriado nacional" else " · Feriado regional", color = MaterialTheme.colorScheme.error) }
                    val siguiente = Validacion.diaHabil(dia, feriados)
                    if (siguiente != dia) Text("Día hábil de referencia: ${siguiente.format(formato)}", color = MaterialTheme.colorScheme.primary)
                    else if (feriados.isNotEmpty() && s.errorExterno == null) Text("Sin conflicto nacional conocido en esta fecha.")
                    if (feriados.isEmpty() || siguiente.year != year || s.errorExterno != null) Text("Verificación parcial: confirma los feriados antes de programar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val delDia = mantenciones.filter { it.first == dia }
                    if (delDia.isEmpty()) Text("No hay mantenciones registradas para este día.")
                    delDia.forEach { (_, a) -> Text("Atención #${a.id} · ${s.equipos.find { it.id == a.equipoId }?.nombre ?: a.equipoId}") }
                }
                Text("Fuente de feriados: Nager.Date · Chile", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { cargar(year) }, modifier = Modifier.fillMaxWidth()) { Text("Actualizar feriados") }
                s.errorExterno?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Text("Referencia orientativa: no cambia automáticamente las fechas ni considera horarios del servicio.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Próximas mantenciones", style = MaterialTheme.typography.titleLarge)
        val futuras = mantenciones.filter { !it.first.isBefore(LocalDate.now()) }
        if (futuras.isEmpty()) Text("No hay próximas mantenciones registradas.")
        futuras.forEach { (d, a) ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(s.equipos.find { it.id == a.equipoId }?.nombre ?: a.equipoId, style = MaterialTheme.typography.titleMedium)
                        Text("${d.format(formato)} · Atención #${a.id}")
                        if (d.toString() in nacionales) Text("Coincide con feriado nacional", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { mesTexto = java.time.YearMonth.from(d).toString(); fecha = d.toString() }) { Text("Ver") }
                }
            }
        }
    }
}
