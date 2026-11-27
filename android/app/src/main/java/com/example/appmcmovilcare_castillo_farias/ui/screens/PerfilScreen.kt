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
internal fun PerfilScreen(s: CareState, guardar: (Perfil) -> Unit, volver: () -> Unit) {
    var nombre by rememberSaveable(s.perfil) { mutableStateOf(s.perfil.nombre) }
    var correo by rememberSaveable(s.perfil) { mutableStateOf(s.perfil.correo) }
    var direccion by rememberSaveable(s.perfil) { mutableStateOf(s.perfil.direccion) }
    var terminos by rememberSaveable(s.perfil) { mutableStateOf(s.perfil.aceptaTerminos) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Encabezado("Datos de contacto", "Actualiza tu información de contacto de demostración.", Icons.Default.Person)
        Text("Perfil local de demostración. No es una cuenta autenticada.")
        Campo("Nombre", nombre, { nombre = it }, s.errores["nombre"], "perfil_nombre")
        Campo("Correo", correo, { correo = it }, s.errores["correo"], "perfil_correo")
        Campo(
            "Dirección",
            direccion,
            { direccion = it },
            s.errores["direccion"],
            "perfil_direccion",
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = terminos, onCheckedChange = { terminos = it })
            Text("Acepto los términos de la demostración")
        }
        ErrorCampo(s.errores["terminos"])
        Button(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            enabled = !s.guardando,
            onClick = { guardar(Perfil(nombre, correo, direccion, terminos)) },
        ) {
            Text("Guardar datos")
        }
        s.mensaje?.let { Text(it) }
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Resumen guardado")
                Text(s.perfil.nombre)
                Text(s.perfil.correo)
                Text(s.perfil.direccion)
            }
        }
        TextButton(onClick = volver) { Text("Volver") }
    }
}

