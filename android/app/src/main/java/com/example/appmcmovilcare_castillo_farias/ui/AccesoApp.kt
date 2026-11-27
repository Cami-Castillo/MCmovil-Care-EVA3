package com.example.appmcmovilcare_castillo_farias.ui

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

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

import com.example.appmcmovilcare_castillo_farias.ui.screens.LoginScreen

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

