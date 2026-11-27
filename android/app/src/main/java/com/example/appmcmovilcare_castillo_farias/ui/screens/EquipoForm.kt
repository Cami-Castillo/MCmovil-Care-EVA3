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
fun EquipoForm(s: CareState, e: Equipo, onGuardar: (Equipo) -> Unit, onVolver: () -> Unit) {
    var nombre by rememberSaveable(e.id) { mutableStateOf(e.nombre) }
    var marca by rememberSaveable(e.id) { mutableStateOf(e.marca) }
    var modelo by rememberSaveable(e.id) { mutableStateOf(e.modelo) }
    var serie by rememberSaveable(e.id) { mutableStateOf(e.numeroSerie) }
    var diagnostico by rememberSaveable(e.id) { mutableStateOf(e.diagnostico) }
    var estado by rememberSaveable(e.id) { mutableStateOf(e.estado) }
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
            if (e.id.isBlank()) "Registrar equipo" else "Editar equipo",
            style = MaterialTheme.typography.headlineSmall,
        )
        Campo("Nombre", nombre, { nombre = it }, s.errores["nombre"], "equipo_nombre")
        Campo("Marca", marca, { marca = it }, s.errores["marca"], "equipo_marca")
        Campo("Modelo", modelo, { modelo = it }, s.errores["modelo"], "equipo_modelo")
        Campo("Número de serie", serie, { serie = it }, s.errores["numeroSerie"], "equipo_serie")
        Selector("Estado", estado, Validacion.estadosEquipo.map { it to it }, { estado = it })
        Campo(
            "Diagnóstico",
            diagnostico,
            { diagnostico = it },
            s.errores["diagnostico"],
            "equipo_diagnostico",
        )
        Button(
            enabled = !s.guardando && !s.cargando && !s.offline,
            onClick = {
                pendiente = s.revision
                onGuardar(
                    e.copy(
                        nombre = nombre,
                        marca = marca,
                        modelo = modelo,
                        numeroSerie = serie,
                        estado = estado,
                        diagnostico = diagnostico,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("guardar_equipo"),
        ) {
            Text("Guardar equipo")
        }
        TextButton(onClick = onVolver) { Text("Volver") }
    }
}

