package com.example.appmcmovilcare_castillo_farias.ui

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.mcmovilcare.logic.LoginEstado

@Composable
fun AccesoApp(login: LoginViewModel = viewModel()) {
    val estado by login.estado.collectAsStateWithLifecycle()
    val sesion = estado.sesion
    if (sesion == null) {
        LoginScreen(estado, login::entrar)
    } else {
        // El árbol de navegación solo existe mientras hay una sesión válida.
        key(sesion.token) {
            val app = LocalContext.current.applicationContext as Application
            val owner =
                remember(login) {
                    object : ViewModelStoreOwner {
                        override val viewModelStore = login.pantallas
                    }
                }
            val vm: CareViewModel =
                viewModel(
                    viewModelStoreOwner = owner,
                    factory =
                        viewModelFactory {
                            initializer {
                                CareViewModel(app, sesion) { login.salir(sesion.token, true) }
                            }
                        },
                )
            CareApp(vm, onSalir = { login.salir(sesion.token) })
        }
    }
}

@Composable
fun LoginScreen(estado: LoginEstado, onEntrar: (String, String) -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var intento by remember { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize()) {
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
                Text("MCmovil Care", style = MaterialTheme.typography.headlineMedium)
                Text("Inicia sesión para acceder a tus equipos y atenciones.")
                OutlinedTextField(
                    usuario,
                    { usuario = it.take(50) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Usuario") },
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
                    singleLine = true,
                    enabled = !estado.cargando,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = intento && clave.isBlank(),
                    supportingText = {
                        if (intento && clave.isBlank()) Text("Ingresa tu contraseña")
                    },
                )
                if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    modifier = Modifier.fillMaxWidth(),
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
