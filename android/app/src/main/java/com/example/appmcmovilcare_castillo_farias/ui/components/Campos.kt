package com.example.appmcmovilcare_castillo_farias.ui.components

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
internal fun ErrorCampo(error: String?) {
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
}

@Composable
internal fun Campo(
    label: String,
    value: String,
    change: (String) -> Unit,
    error: String?,
    tag: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = change,
        label = { Text(label) },
        isError = error != null,
        shape = MaterialTheme.shapes.medium,
        supportingText = { ErrorCampo(error) },
        trailingIcon = { if (error != null) Icon(Icons.Default.Error, "Error en $label") },
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}

@Composable
internal fun Selector(
    label: String,
    value: String,
    opciones: List<Pair<String, String>>,
    change: (String) -> Unit,
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${opciones.find{it.first==value}?.second?:"Seleccionar"}")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { (id, titulo) ->
                DropdownMenuItem(
                    text = { Text(titulo) },
                    onClick = {
                        change(id)
                        abierto = false
                    },
                )
            }
        }
    }
}
