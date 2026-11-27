
package com.example.appmcmovilcare_castillo_farias.ui.screens

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Dashboard(
    s: CareState,
    onEquipos: () -> Unit,
    onAtenciones: () -> Unit,
    onReintentar: () -> Unit,
    rol: String = "ADMIN"
) {
    val equipos = s.equipos.size
    val atenciones = s.atenciones.size

    val pendientes = s.atenciones.count {
        it.autorizacion == "Pendiente" &&
                it.estado == "Cotizada"
    }

    val noFinalizadas = s.atenciones.count {
        it.estado != "Finalizada"
    }

    val colores = MaterialTheme.colorScheme

    val titulo = when (rol) {
        "TECNICO" -> "Tu jornada técnica"
        "CLIENTE" -> "Tus equipos, siempre a mano"
        else -> "Tu centro de servicio"
    }

    val descripcion = when (rol) {
        "TECNICO" ->
            "Consulta tus trabajos y actualiza su seguimiento."
        "CLIENTE" ->
            "Consulta solicitudes, cotizaciones y mantenciones."
        else ->
            "Gestiona equipos y supervisa las atenciones desde un solo lugar."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // Encabezado principal

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                colores.primaryContainer,
                                colores.secondaryContainer
                            )
                        )
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CENTRO DE SERVICIO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colores.surface.copy(alpha = 0.65f)
                    ) {
                        Icon(
                            imageVector = when (rol) {
                                "TECNICO" -> Icons.Default.Build
                                "CLIENTE" -> Icons.Default.Person
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(26.dp),
                            tint = colores.primary
                        )
                    }
                }

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colores.onPrimaryContainer
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.onPrimaryContainer
                )
            }
        }

        // Resumen general

        Text(
            text = "Resumen general",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Indicador(
                cantidad = equipos,
                etiqueta = if (equipos == 1)
                    "Equipo registrado"
                else
                    "Equipos registrados",
                icon = Icons.Default.Build,
                modifier = Modifier.weight(1f)
            )

            Indicador(
                cantidad = atenciones,
                etiqueta = if (atenciones == 1)
                    "Atención registrada"
                else
                    "Atenciones registradas",
                icon = Icons.Default.List,
                modifier = Modifier.weight(1f)
            )
        }

        // Autorizaciones pendientes

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pendientes"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = colores.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colores.primaryContainer
                ) {
                    Icon(
                        imageVector = if (pendientes == 0)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp),
                        tint = colores.primary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (pendientes == 1)
                            "1 autorización pendiente"
                        else
                            "$pendientes autorizaciones pendientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (pendientes > 0)
                            "Revisa las cotizaciones en Atenciones."
                        else
                            "Estás al día con las cotizaciones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onSurfaceVariant
                    )
                }
            }
        }

        // Información adicional para técnicos

        if (rol == "TECNICO") {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = colores.primary
                    )

                    Column {
                        Text(
                            text = "Trabajos activos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (noFinalizadas == 1)
                                "1 atención no finalizada"
                            else
                                "$noFinalizadas atenciones no finalizadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colores.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Accesos rápidos

        Text(
            text = "Accesos rápidos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        AccesoRapido(
            titulo = "Consultar equipos",
            descripcion = "Inventario y estado de equipos",
            icon = Icons.Default.Build,
            onClick = onEquipos
        )

        AccesoRapido(
            titulo = "Solicitudes y seguimiento",
            descripcion = when (rol) {
                "TECNICO" -> "Consulta y actualiza las atenciones"
                "CLIENTE" -> "Consulta solicitudes y cotizaciones"
                else -> "Gestiona las atenciones técnicas"
            },
            icon = Icons.Default.List,
            onClick = onAtenciones
        )

        // Recuperación de conexión

        if (s.error != null) {
            OutlinedButton(
                onClick = onReintentar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Reintentar")
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Indicador(
    cantidad: Int,
    etiqueta: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme

    OutlinedCard(
        modifier = modifier.height(154.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = colores.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colores.primaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp),
                    tint = colores.primary
                )
            }

            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = colores.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccesoRapido(
    titulo: String,
    descripcion: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = colores.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colores.primaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(26.dp),
                    tint = colores.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colores.primary
            )
        }
    }
}
