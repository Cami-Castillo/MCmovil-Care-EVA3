package com.example.appmcmovilcare_castillo_farias.ui.screens

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.appmcmovilcare_castillo_farias.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appmcmovilcare_castillo_farias.viewmodel.*

@Composable
fun LoginScreen(estado: LoginEstado, onEntrar: (String, String) -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var intento by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                Modifier.widthIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.logo_mcmovil), "Logo MCmovil Care", Modifier.size(88.dp))
                    }
                }
                Text("PORTAL DE SERVICIO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("MCmovil Care", style = MaterialTheme.typography.headlineMedium)
                Text("Inicia sesión para acceder a tus equipos y atenciones.")
                OutlinedTextField(
                    usuario,
                    { usuario = it.take(50) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Usuario") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    enabled = !estado.cargando,
                    isError = intento && usuario.isBlank(),
                    supportingText = {
                        if (intento && usuario.isBlank()) Text("Ingresa tu usuario")
                    },
                )
                OutlinedTextField(
                    clave,
                    { clave = it.take(128) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = { IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (visible) "Ocultar contraseña" else "Mostrar contraseña") } },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    enabled = !estado.cargando,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = intento && clave.isBlank(),
                    supportingText = {
                        if (intento && clave.isBlank()) Text("Ingresa tu contraseña")
                    },
                )
                if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    enabled = !estado.cargando,
                    onClick = {
                        intento = true
                        if (usuario.isNotBlank() && clave.isNotBlank()) onEntrar(usuario, clave)
                    },
                ) {
                    Text("Iniciar sesión")
                }
            }
        }
    }
}
