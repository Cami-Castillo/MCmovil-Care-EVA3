
package com.example.appmcmovilcare_castillo_farias.ui

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*

import com.example.appmcmovilcare_castillo_farias.ui.components.AdjuntoImagen
import com.example.appmcmovilcare_castillo_farias.viewmodel.CareViewModel
import com.example.appmcmovilcare_castillo_farias.ui.screens.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.appmcmovilcare_castillo_farias.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareApp(
    vm: CareViewModel,
    onSalir: () -> Unit
) {

    val tecnico = vm.sesion.rol == "TECNICO"
    val cliente = vm.sesion.rol == "CLIENTE"

    val s by vm.state.collectAsStateWithLifecycle()

    val nav = rememberNavController()

    val colores = MaterialTheme.colorScheme

    val nombreRol = when (vm.sesion.rol) {
        "ADMIN" -> "Administrador"
        "CLIENTE" -> "Cliente"
        "TECNICO" -> "Técnico"
        else -> vm.sesion.rol
    }

    // Navegación entre las pantallas principales

    fun navegarPrincipal(destino: String) {

        nav.navigate(destino) {

            popUpTo("inicio") {
                inclusive = false
            }

            launchSingleTop = true
            restoreState = false
        }
    }

    val foto by vm.foto.collectAsStateWithLifecycle()

    val preparando by vm.ocupado.collectAsStateWithLifecycle()

    val errorFoto by vm.errorFoto.collectAsStateWithLifecycle()

    val actual by nav.currentBackStackEntryAsState()

    val ruta = actual?.destination?.route

    val disponible =
        !s.guardando && !s.cargando && !preparando

    var borrarEquipo by remember {
        mutableStateOf<Equipo?>(null)
    }

    var borrarAtencion by remember {
        mutableStateOf<Atencion?>(null)
    }

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    Scaffold(

        containerColor = colores.background,

        // ==========================================
        // BARRA SUPERIOR
        // ==========================================

        topBar = {

            TopAppBar(

                title = {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        // Identidad visual

                        Image(
                            painter = painterResource(R.drawable.logo_mcmovil),
                            contentDescription = "Logo MCmovil Care",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )

                        // Nombre de la aplicación

                        Column {

                            Row {

                                Text(
                                    text = "MCmovil ",
                                    style =
                                        MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colores.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )

                                Text(
                                    text = "Care",
                                    style =
                                        MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = colores.primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Text(
                                text = "Centro de servicio",
                                style =
                                    MaterialTheme.typography.labelSmall,
                                color = colores.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colores.surface
                ),

                // Acciones principales

                actions = {

                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        // Actualizar información

                        IconButton(
                            onClick = {
                                vm.controller.cargar()
                            },
                            enabled = disponible,
                            modifier = Modifier.size(48.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar",
                                modifier = Modifier.size(24.dp),
                                tint = if (disponible)
                                    colores.primary
                                else
                                    colores.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        // Menú del usuario

                        Box {

                            FilledTonalIconButton(
                                onClick = {
                                    menuAbierto = true
                                },
                                modifier = Modifier.size(48.dp)
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Menú de usuario",
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = menuAbierto,
                                onDismissRequest = {
                                    menuAbierto = false
                                }
                            ) {

                                // Información del usuario

                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                ) {

                                    Text(
                                        text = "Mi cuenta",
                                        style =
                                            MaterialTheme.typography.labelMedium,
                                        color = colores.onSurfaceVariant
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = nombreRol,
                                        style =
                                            MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = vm.sesion.usuario,
                                        style =
                                            MaterialTheme.typography.bodySmall,
                                        color = colores.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider()

                                // Datos de contacto

                                DropdownMenuItem(

                                    text = {
                                        Text("Datos de contacto")
                                    },

                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null
                                        )
                                    },

                                    onClick = {

                                        menuAbierto = false

                                        nav.navigate("perfil") {
                                            launchSingleTop = true
                                        }
                                    }
                                )

                                // Preferencias

                                DropdownMenuItem(

                                    text = {
                                        Text("Preferencias")
                                    },

                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = null
                                        )
                                    },

                                    onClick = {

                                        menuAbierto = false

                                        nav.navigate("preferencias") {
                                            launchSingleTop = true
                                        }
                                    }
                                )

                                HorizontalDivider()

                                // Cerrar sesión

                                DropdownMenuItem(

                                    text = {
                                        Text(
                                            text = "Cerrar sesión",
                                            color = colores.error
                                        )
                                    },

                                    leadingIcon = {
                                        Icon(
                                            imageVector =
                                                Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            tint = colores.error
                                        )
                                    },

                                    onClick = {

                                        menuAbierto = false

                                        onSalir()
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },

        // ==========================================
        // BARRA DE NAVEGACIÓN INFERIOR
        // ==========================================

        bottomBar = {

            NavigationBar(
                containerColor = colores.surface
            ) {

                listOf(

                    Triple(
                        "inicio",
                        "Inicio",
                        Icons.Default.Home
                    ),

                    Triple(
                        "equipos",
                        "Equipos",
                        Icons.Default.Build
                    ),

                    Triple(
                        "atenciones",
                        "Atenciones",
                        Icons.Default.List
                    ),

                    Triple(
                        "agenda",
                        "Agenda",
                        Icons.Default.DateRange
                    )

                ).forEach { (destino, titulo, icono) ->

                    NavigationBarItem(

                        selected = ruta == destino,

                        onClick = {
                            navegarPrincipal(destino)
                        },

                        icon = {
                            Icon(
                                imageVector = icono,
                                contentDescription = titulo
                            )
                        },

                        label = {
                            Text(titulo)
                        },

                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = colores.primaryContainer,
                            selectedIconColor = colores.primary,
                            selectedTextColor = colores.primary
                        )
                    )
                }
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
        ) {

            // ==========================================
            // INDICADOR DE SESIÓN
            // ==========================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    color = colores.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colores.primary
                        )

                        Text(
                            text = nombreRol,
                            style =
                                MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = colores.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = vm.sesion.usuario,
                    style =
                        MaterialTheme.typography.labelMedium,
                    color = colores.onSurfaceVariant
                )
            }

            // ==========================================
            // INDICADORES DE CARGA Y CONEXIÓN
            // ==========================================

            if (s.cargando || s.guardando || preparando) {

                LinearProgressIndicator(
                    Modifier
                        .fillMaxWidth()
                        .testTag("carga")
                )
            }

            if (s.offline) {

                Text(
                    text = "Sin conexión: mostrando copia local, edición deshabilitada",
                    modifier = Modifier.padding(8.dp),
                    color = colores.error
                )
            }

            s.error?.let {

                Text(
                    text = it,
                    color = colores.error,
                    modifier = Modifier
                        .padding(8.dp)
                        .testTag("error_global")
                )
            }

            // ==========================================
            // NAVEGACIÓN DE PANTALLAS
            // ==========================================

            NavHost(

                navController = nav,

                startDestination = "inicio",

                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 900.dp)
                    .align(Alignment.CenterHorizontally)

            ) {

                // INICIO

                composable("inicio") {

                    Dashboard(

                        s,

                        {
                            navegarPrincipal("equipos")
                        },

                        {
                            navegarPrincipal("atenciones")
                        },

                        {
                            vm.controller.cargar()
                        },

                        rol = vm.sesion.rol
                    )
                }

                // EQUIPOS

                composable("equipos") {

                    EquiposScreen(

                        s,

                        {
                            nav.navigate("equipo/nuevo")
                        },

                        {
                            nav.navigate("equipo/${it.id}")
                        },

                        {
                            borrarEquipo = it
                        },

                        puedeGestionar =
                            vm.sesion.rol == "ADMIN"
                    )
                }

                // FORMULARIO DE EQUIPOS

                composable("equipo/{id}") { entry ->

                    val id =
                        entry.arguments?.getString("id")

                    val e = if (id == "nuevo") {

                        Equipo()

                    } else {

                        s.equipos.find {
                            it.id == id
                        }
                    }

                    if (e != null) {

                        EquipoForm(
                            s,
                            e,
                            vm.controller::equipo,
                            {
                                nav.popBackStack()
                            }
                        )

                    } else {

                        Text(
                            "Equipo no encontrado: actualiza la lista"
                        )
                    }
                }

                // ATENCIONES

                composable("atenciones") {

                    AtencionesScreen(

                        s,

                        {
                            vm.quitarFoto()

                            nav.navigate("atencion/nueva")
                        },

                        {
                            vm.quitarFoto()

                            nav.navigate("atencion/${it.id}")
                        },

                        {
                            borrarAtencion = it
                        },

                        vm.controller::autorizar,

                        cliente,

                        tecnico,

                        vm.imagenes
                    )
                }

                // FORMULARIO DE ATENCIONES

                composable("atencion/{id}") { entry ->

                    val id =
                        entry.arguments?.getString("id")

                    val atencion =

                        if (id == "nueva") {

                            Atencion(
                                correoContacto = s.perfil.correo
                            )

                        } else {

                            s.atenciones.find {
                                it.id.toString() == id
                            }
                        }

                    if (atencion != null) {

                        AtencionForm(

                            s,

                            atencion,

                            {

                                vm.controller.atencion(

                                    it.copy(

                                        evidencia =
                                            vm.referenciaFoto.ifBlank {
                                                it.evidencia
                                            }
                                    )
                                )
                            },

                            {
                                nav.popBackStack()
                            }

                        ) {

                            AdjuntoImagen(

                                foto,

                                preparando || s.guardando,

                                disponible && !s.offline,

                                errorFoto,

                                vm::adjuntar,

                                vm::quitarFoto,

                                vm::errorFoto
                            )
                        }

                    } else {

                        Text("Atención no encontrada")
                    }
                }

                // PERFIL DEL USUARIO

                composable("perfil") {

                    PerfilScreen(
                        s,
                        vm.controller::perfil
                    ) {
                        nav.popBackStack()
                    }
                }

                // PREFERENCIAS

                composable("preferencias") {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {

                        Text(
                            text = "Preferencias",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Personaliza la información que deseas visualizar.",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color = colores.onSurfaceVariant
                        )

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {

                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement =
                                        Arrangement.spacedBy(4.dp)
                                ) {

                                    Text(
                                        text = "Mostrar diagnósticos",
                                        style =
                                            MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "Visualiza el diagnóstico en las tarjetas de equipos.",
                                        style =
                                            MaterialTheme.typography.bodySmall,
                                        color = colores.onSurfaceVariant
                                    )
                                }

                                Switch(

                                    checked =
                                        s.mostrarDiagnosticos,

                                    onCheckedChange =
                                        vm.controller::preferencia,

                                    enabled = disponible
                                )
                            }
                        }

                        AnimatedVisibility(
                            s.mensaje != null
                        ) {

                            Text(s.mensaje ?: "")
                        }

                        OutlinedButton(

                            onClick = {
                                nav.popBackStack()
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),

                            shape = RoundedCornerShape(12.dp)

                        ) {

                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null
                            )

                            Spacer(Modifier.width(8.dp))

                            Text("Volver")
                        }
                    }
                }

                // AGENDA

                composable("agenda") {

                    AgendaScreen(
                        s,
                        vm.controller::feriados
                    )
                }
            }
        }
    }

    // ==========================================
    // CONFIRMACIÓN DE ELIMINACIÓN
    // ==========================================

    if (borrarEquipo != null || borrarAtencion != null) {

        AlertDialog(

            onDismissRequest = {

                borrarEquipo = null

                borrarAtencion = null
            },

            icon = {

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = colores.error
                )
            },

            title = {

                Text("Confirmar eliminación")
            },

            text = {

                Text(
                    if (borrarEquipo != null)
                        "Se eliminará el equipo del servidor. Los equipos con atenciones asociadas no se pueden eliminar."
                    else
                        "Se eliminará la atención del servidor. ¿Deseas continuar?"
                )
            },

            confirmButton = {

                Button(

                    enabled = disponible && !s.offline,

                    onClick = {

                        borrarEquipo?.let {

                            vm.controller.eliminarEquipo(it.id)
                        }

                        borrarAtencion?.let {

                            vm.controller.eliminarAtencion(it.id)
                        }

                        borrarEquipo = null

                        borrarAtencion = null
                    },

                    colors = ButtonDefaults.buttonColors(
                        containerColor = colores.error,
                        contentColor = colores.onError
                    )

                ) {

                    Text("Eliminar")
                }
            },

            dismissButton = {

                OutlinedButton(

                    onClick = {

                        borrarEquipo = null

                        borrarAtencion = null
                    }

                ) {

                    Text("Cancelar")
                }
            }
        )
    }
}
