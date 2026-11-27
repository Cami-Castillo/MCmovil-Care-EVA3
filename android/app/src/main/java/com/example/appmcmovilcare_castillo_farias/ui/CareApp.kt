package com.example.appmcmovilcare_castillo_farias.ui

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
import com.mcmovilcare.logic.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareApp(vm: CareViewModel, onSalir: () -> Unit) {
    val tecnico = vm.sesion.rol == "TECNICO"
    val cliente = vm.sesion.rol == "CLIENTE"
    val s by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val foto by vm.foto.collectAsStateWithLifecycle()
    val preparando by vm.ocupado.collectAsStateWithLifecycle()
    val errorFoto by vm.errorFoto.collectAsStateWithLifecycle()
    val actual by nav.currentBackStackEntryAsState()
    val ruta = actual?.destination?.route
    val disponible = !s.guardando && !s.cargando && !preparando
    var borrarEquipo by remember { mutableStateOf<Equipo?>(null) }
    var borrarAtencion by remember { mutableStateOf<Atencion?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MCmovil Care") },
                actions = {
                    TextButton(onClick = onSalir) { Text("Salir") }
                    IconButton(onClick = { vm.controller.cargar() }, enabled = disponible) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                    IconButton(onClick = { nav.navigate("perfil") }) {
                        Icon(Icons.Default.Person, "Datos de contacto")
                    }
                    IconButton(onClick = { nav.navigate("preferencias") }) {
                        Icon(Icons.Default.Settings, "Preferencias")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                listOf(
                        Triple("inicio", "Inicio", Icons.Default.Home),
                        Triple("equipos", "Equipos", Icons.Default.Build),
                        Triple("atenciones", "Atenciones", Icons.Default.List),
                        Triple("agenda", "Agenda", Icons.Default.DateRange),
                    )
                    .forEach { (destino, titulo, icono) ->
                        NavigationBarItem(
                            selected = ruta == destino,
                            onClick = {
                                nav.navigate(destino) {
                                    popUpTo("inicio") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icono, titulo) },
                            label = { Text(titulo) },
                        )
                    }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize()) {
            Text(
                "Sesión: ${vm.sesion.usuario} · ${vm.sesion.rol}",
                Modifier.padding(horizontal = 16.dp),
            )
            if (s.cargando || s.guardando || preparando)
                LinearProgressIndicator(Modifier.fillMaxWidth().testTag("carga"))
            if (s.offline)
                Text(
                    "Sin conexión: mostrando copia local, edición deshabilitada",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            s.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp).testTag("error_global"),
                )
            }
            NavHost(
                navController = nav,
                startDestination = "inicio",
                modifier =
                    Modifier.weight(1f).widthIn(max = 900.dp).align(Alignment.CenterHorizontally),
            ) {
                composable("inicio") {
                    Dashboard(
                        s,
                        { nav.navigate("equipos") },
                        { nav.navigate("atenciones") },
                        { vm.controller.cargar() },
                    )
                }
                composable("equipos") {
                    EquiposScreen(
                        s,
                        { nav.navigate("equipo/nuevo") },
                        { nav.navigate("equipo/${it.id}") },
                        { borrarEquipo = it },
                        puedeGestionar = !tecnico,
                    )
                }
                composable("equipo/{id}") { entry ->
                    val id = entry.arguments?.getString("id")
                    val e = if (id == "nuevo") Equipo() else s.equipos.find { it.id == id }
                    if (e != null) EquipoForm(s, e, vm.controller::equipo, { nav.popBackStack() })
                    else Text("Equipo no encontrado: actualiza la lista")
                }
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
                        { borrarAtencion = it },
                        vm.controller::autorizar,
                        cliente,
                        tecnico,
                        vm.imagenes,
                    )
                }
                composable("atencion/{id}") { entry ->
                    val id = entry.arguments?.getString("id")
                    val atencion =
                        if (id == "nueva") Atencion(correoContacto = s.perfil.correo)
                        else s.atenciones.find { it.id.toString() == id }
                    if (atencion != null)
                        AtencionForm(
                            s,
                            atencion,
                            {
                                vm.controller.atencion(
                                    it.copy(evidencia = vm.referenciaFoto.ifBlank { it.evidencia })
                                )
                            },
                            { nav.popBackStack() },
                        ) {
                            AdjuntoImagen(
                                foto,
                                preparando || s.guardando,
                                disponible && !s.offline,
                                errorFoto,
                                vm::adjuntar,
                                vm::quitarFoto,
                                vm::errorFoto,
                            )
                        }
                    else Text("Atención no encontrada")
                }
                composable("perfil") {
                    PerfilScreen(s, vm.controller::perfil) { nav.popBackStack() }
                }
                composable("preferencias") {
                    Column(Modifier.padding(20.dp)) {
                        Text("Preferencias", style = MaterialTheme.typography.headlineSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Mostrar diagnósticos", Modifier.weight(1f))
                            Switch(
                                checked = s.mostrarDiagnosticos,
                                onCheckedChange = vm.controller::preferencia,
                                enabled = disponible,
                            )
                        }
                        AnimatedVisibility(s.mensaje != null) { Text(s.mensaje ?: "") }
                        OutlinedButton(onClick = { nav.popBackStack() }) { Text("Volver") }
                    }
                }
                composable("agenda") { AgendaScreen(s, vm.controller::feriados) }
            }
        }
    }
    if (borrarEquipo != null || borrarAtencion != null)
        AlertDialog(
            onDismissRequest = {
                borrarEquipo = null
                borrarAtencion = null
            },
            title = { Text("Confirmar eliminación") },
            text = {
                Text(
                    "Se eliminará el registro del servidor. Los equipos con atenciones no se pueden eliminar."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = disponible,
                    onClick = {
                        borrarEquipo?.let { vm.controller.eliminarEquipo(it.id) }
                        borrarAtencion?.let { vm.controller.eliminarAtencion(it.id) }
                        borrarEquipo = null
                        borrarAtencion = null
                    },
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        borrarEquipo = null
                        borrarAtencion = null
                    }
                ) {
                    Text("Cancelar")
                }
            },
        )
}

@Composable
fun Dashboard(
    s: CareState,
    onEquipos: () -> Unit,
    onAtenciones: () -> Unit,
    onReintentar: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painterResource(R.drawable.logo_mcmovil),
            contentDescription = "Logo MCmovil Care",
            modifier = Modifier.size(88.dp),
        )
        Text("Tus equipos, siempre a mano", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Consulta estados, autorizaciones, informes y próximas mantenciones. Datos ficticios de una demostración académica."
        )
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("${s.equipos.size} equipos", style = MaterialTheme.typography.titleLarge)
                Text("${s.atenciones.size} atenciones registradas")
                Text(
                    "${s.atenciones.count{it.autorizacion=="Pendiente"&&it.estado=="Cotizada"}} autorizaciones pendientes",
                    modifier = Modifier.testTag("pendientes"),
                )
            }
        }
        Button(onClick = onEquipos, modifier = Modifier.fillMaxWidth()) {
            Text("Consultar equipos")
        }
        OutlinedButton(onClick = onAtenciones, modifier = Modifier.fillMaxWidth()) {
            Text("Solicitudes y seguimiento")
        }
        if (s.error != null) TextButton(onClick = onReintentar) { Text("Reintentar") }
        Text("MCmovil Care · Gestión de ciclo de vida", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EquiposScreen(
    s: CareState,
    nuevo: () -> Unit,
    editar: (Equipo) -> Unit,
    eliminar: (Equipo) -> Unit,
    puedeGestionar: Boolean,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Mis equipos", style = MaterialTheme.typography.headlineSmall)
            if (puedeGestionar)
                Button(onClick = nuevo, enabled = !s.offline && !s.cargando && !s.guardando) {
                    Text("Registrar equipo")
                }
        }
        if (s.equipos.isEmpty() && !s.cargando) item { Text("Aún no hay equipos.") }
        items(s.equipos, key = { it.id }) { e ->
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(16.dp)) {
                    Text(e.nombre, style = MaterialTheme.typography.titleLarge)
                    Text("${e.marca} · ${e.modelo}")
                    Text("Serie: ${e.numeroSerie}")
                    Text("Estado: ${e.estado}")
                    if (s.mostrarDiagnosticos) Text(e.diagnostico)
                    if (puedeGestionar)
                        Row {
                            TextButton(
                                enabled = !s.offline && !s.guardando,
                                onClick = { editar(e) },
                            ) {
                                Text("Editar")
                            }
                            TextButton(
                                enabled = !s.offline && !s.guardando,
                                onClick = { eliminar(e) },
                            ) {
                                Text("Eliminar")
                            }
                        }
                }
            }
        }
    }
}

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
            modifier = Modifier.testTag("guardar_equipo"),
        ) {
            Text("Guardar equipo")
        }
        TextButton(onClick = onVolver) { Text("Volver") }
    }
}

@Composable
private fun AtencionesScreen(
    s: CareState,
    nueva: () -> Unit,
    editar: (Atencion) -> Unit,
    eliminar: (Atencion) -> Unit,
    autorizar: (Long, String) -> Unit,
    cliente: Boolean,
    tecnico: Boolean,
    imagenes: coil.ImageLoader,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Solicitudes y seguimiento", style = MaterialTheme.typography.headlineSmall)
            Button(enabled = !s.offline && !s.guardando && !s.cargando, onClick = nueva) {
                Text("Solicitar atención")
            }
        }
        if (s.atenciones.isEmpty() && !s.cargando) item { Text("No hay solicitudes registradas") }
        items(s.atenciones, key = { it.id }) { a ->
            var expandido by rememberSaveable(a.id) { mutableStateOf(false) }
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Atención #${a.id} · ${a.estado}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(s.equipos.find { it.id == a.equipoId }?.nombre ?: a.equipoId)
                    Text(a.descripcion)
                    Text("Cotización ficticia: $${a.importe.toLong()} CLP · ${a.autorizacion}")
                    if (!tecnico && a.estado == "Cotizada" && a.autorizacion == "Pendiente")
                        Row {
                            Button(
                                enabled = !s.guardando && !s.offline,
                                onClick = { autorizar(a.id, "Aprobada") },
                            ) {
                                Text("Autorizar")
                            }
                            TextButton(
                                enabled = !s.guardando && !s.offline,
                                onClick = { autorizar(a.id, "Rechazada") },
                            ) {
                                Text("Rechazar")
                            }
                        }
                    TextButton(onClick = { expandido = !expandido }) {
                        Text(if (expandido) "Ocultar detalles" else "Historial, informe y garantía")
                    }
                    AnimatedVisibility(expandido) {
                        Column {
                            Text("Servicio: ${a.tipoServicio}")
                            Text("Contacto: ${a.correoContacto}")
                            Text("Informe: ${a.informe}")
                            Text("Garantía hasta: ${a.garantiaHasta.ifBlank{"Sin registrar"}}")
                            Text("Próxima mantención: ${a.mantenimiento.ifBlank{"Sin programar"}}")
                            if (a.evidencia.isNotBlank())
                                AsyncImage(
                                    model =
                                        BuildConfig.ATENCIONES_URL +
                                            "api/evidencias/" +
                                            a.evidencia,
                                    contentDescription = "Evidencia de atención",
                                    imageLoader = imagenes,
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                )
                            a.historial.forEach {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Row {
                        if (!cliente)
                            TextButton(
                                enabled = !s.guardando && !s.offline,
                                onClick = { editar(a) },
                            ) {
                                Text("Editar atención")
                            }
                        if (!tecnico)
                            TextButton(
                                enabled = !s.guardando && !s.offline,
                                onClick = { eliminar(a) },
                            ) {
                                Text("Eliminar")
                            }
                    }
                }
            }
        }
    }
}

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
            modifier = Modifier.testTag("guardar_atencion"),
        ) {
            Text("Guardar solicitud")
        }
        TextButton(onClick = onVolver) { Text("Volver") }
    }
}

@Composable
private fun PerfilScreen(s: CareState, guardar: (Perfil) -> Unit, volver: () -> Unit) {
    var nombre by rememberSaveable(s.perfil) { mutableStateOf(s.perfil.nombre) }
    var correo by rememberSaveable(s.perfil) { mutableStateOf(s.perfil.correo) }
    var direccion by rememberSaveable(s.perfil) { mutableStateOf(s.perfil.direccion) }
    var terminos by rememberSaveable(s.perfil) { mutableStateOf(s.perfil.aceptaTerminos) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Datos de contacto", style = MaterialTheme.typography.headlineSmall)
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

@Composable
fun AgendaScreen(s: CareState, cargar: (Int) -> Unit) {
    val year = LocalDate.now().year
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    LaunchedEffect(year) { cargar(year) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Agenda de mantenciones", style = MaterialTheme.typography.headlineSmall)
        Text(
            "API externa: Nager.Date · Feriados de Chile $year. No modifica equipos ni atenciones."
        )
        OutlinedButton(onClick = { cargar(year) }) { Text("Actualizar feriados") }
        s.errorExterno?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Campo("Fecha tentativa (AAAA-MM-DD)", fecha, { fecha = it }, null, "fecha_agenda")
        val dia = runCatching { LocalDate.parse(fecha) }.getOrNull()
        if (dia != null && dia.year == year) {
            Text("Siguiente día hábil de referencia: ${Validacion.diaHabil(dia,s.feriados)}")
            if (s.feriados.isEmpty())
                Text("Sin datos de feriados: solo se excluyen fines de semana.")
        } else Text("Ingresa una fecha válida del año $year")
        Text("Próximas mantenciones registradas", style = MaterialTheme.typography.titleMedium)
        s.atenciones
            .filter { it.mantenimiento.isNotBlank() }
            .forEach { Text("Atención #${it.id}: ${it.mantenimiento}") }
        s.feriados.forEach {
            Text("${it.date} · ${it.localName}${if(it.global)"" else " (regional)"}")
        }
    }
}

@Composable
private fun ErrorCampo(error: String?) {
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun Campo(
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
        supportingText = { ErrorCampo(error) },
        trailingIcon = { if (error != null) Icon(Icons.Default.Error, "Error en $label") },
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}

@Composable
private fun Selector(
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
