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
fun AtencionForm(
    s: CareState,
    a: Atencion,
    onGuardar: (Atencion) -> Unit,
    onVolver: () -> Unit,
    foto: @Composable () -> Unit = {},
) {
    var equipoId by rememberSaveable(a.id) { mutableStateOf(a.equipoId) }
    var servicio by rememberSaveable(a.id) { mutableStateOf(a.tipoServicio) }
    var descripcion by rememberSaveable(a.id) { mutableStateOf(a.descripcion) }
    var correo by rememberSaveable(a.id) { mutableStateOf(a.correoContacto) }
    var estado by rememberSaveable(a.id) { mutableStateOf(a.estado) }
    var importe by rememberSaveable(a.id) { mutableStateOf(a.importe.toString()) }
    var informe by rememberSaveable(a.id) { mutableStateOf(a.informe) }
    var garantia by rememberSaveable(a.id) { mutableStateOf(a.garantiaHasta) }
    var mantenimiento by rememberSaveable(a.id) { mutableStateOf(a.mantenimiento) }
    var pendiente by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(s.revision, s.guardando) {
        if (pendiente != null && s.revision > pendiente!! && !s.guardando) {
            onVolver()
            pendiente = null
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            if (a.id == 0L) "Nueva solicitud" else "Editar atención",
            style = MaterialTheme.typography.headlineSmall,
        )
        Selector("Equipo", equipoId, s.equipos.map { it.id to it.nombre }, { equipoId = it })
        ErrorCampo(s.errores["equipoId"])
        Selector("Servicio", servicio, Validacion.servicios.map { it to it }, { servicio = it })
        Campo(
            "Descripción",
            descripcion,
            { descripcion = it },
            s.errores["descripcion"],
            "descripcion",
        )
        Campo("Correo de contacto", correo, { correo = it }, s.errores["correoContacto"], "correo")
        if (a.id != 0L) {
            Text("Actualización técnica", style = MaterialTheme.typography.titleMedium)
            Selector("Estado", estado, Validacion.estadosAtencion.map { it to it }, { estado = it })
            Campo("Cotización CLP", importe, { importe = it }, s.errores["importe"], "importe")
            Campo("Informe", informe, { informe = it }, s.errores["informe"], "informe")
            Campo(
                "Garantía hasta (AAAA-MM-DD)",
                garantia,
                { garantia = it },
                s.errores["garantiaHasta"],
                "garantia",
            )
            Campo(
                "Mantención (AAAA-MM-DD)",
                mantenimiento,
                { mantenimiento = it },
                s.errores["mantenimiento"],
                "mantenimiento",
            )
        }
        foto()
        Button(
            enabled = !s.guardando && !s.cargando && !s.offline,
            onClick = {
                pendiente = s.revision
                onGuardar(
                    a.copy(
                        equipoId = equipoId,
                        tipoServicio = servicio,
                        descripcion = descripcion,
                        correoContacto = correo,
                        estado = estado,
                        importe = importe.replace(',', '.').toDoubleOrNull() ?: -1.0,
                        informe = informe,
                        garantiaHasta = garantia,
                        mantenimiento = mantenimiento,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("guardar_atencion"),
        ) {
            Text("Guardar solicitud")
        }
        TextButton(onClick = onVolver) { Text("Volver") }
    }
}

