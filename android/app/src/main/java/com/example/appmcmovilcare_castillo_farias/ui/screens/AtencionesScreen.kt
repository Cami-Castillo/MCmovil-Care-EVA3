
package com.example.appmcmovilcare_castillo_farias.ui.screens

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import coil.compose.AsyncImage
import com.example.appmcmovilcare_castillo_farias.BuildConfig
import com.example.appmcmovilcare_castillo_farias.ui.components.*

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

@Composable
internal fun AtencionesScreen(
    s: CareState,
    nueva: () -> Unit,
    editar: (Atencion) -> Unit,
    eliminar: (Atencion) -> Unit,
    autorizar: (Long, String) -> Unit,
    cliente: Boolean,
    tecnico: Boolean,
    imagenes: coil.ImageLoader,
) {

    val colores = MaterialTheme.colorScheme

    val accionesHabilitadas =
        !s.offline && !s.guardando && !s.cargando

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Encabezado

        item {

            Encabezado(
                "Solicitudes y seguimiento",
                "Consulta el progreso y las cotizaciones de servicio.",
                Icons.Default.List
            )

            if (!tecnico) {

                Spacer(Modifier.height(14.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    enabled = accionesHabilitadas,
                    onClick = nueva,
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Icon(Icons.Default.Add, null)

                    Spacer(Modifier.width(8.dp))

                    Text("Solicitar atención")
                }
            }
        }

        // Estado vacío

        if (s.atenciones.isEmpty() && !s.cargando) {

            item {

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = colores.primary
                        )

                        Text(
                            "No hay solicitudes registradas",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            "Las atenciones aparecerán aquí.",
                            color = colores.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Tarjetas de atenciones

        items(s.atenciones, key = { it.id }) { a ->

            var expandido by rememberSaveable(a.id) {
                mutableStateOf(false)
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colores.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    // Identificador y estado

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            "Atención #${a.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        EstadoVisual(a.estado)
                    }

                    // Equipo asociado

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colores.primaryContainer
                        ) {

                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(14.dp)
                                    .size(26.dp),
                                tint = colores.primary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {

                            Text(
                                s.equipos.find {
                                    it.id == a.equipoId
                                }?.nombre ?: a.equipoId,
                                style =
                                    MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                a.descripcion,
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color = colores.onSurfaceVariant
                            )
                        }
                    }

                    // Cotización

                    Surface(
                        color = colores.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                "Cotización ficticia",
                                style =
                                    MaterialTheme.typography.labelMedium,
                                color = colores.onPrimaryContainer
                            )

                            Text(
                                NumberFormat.getCurrencyInstance(
                                    Locale.forLanguageTag("es-CL")
                                ).format(a.importe) + " CLP",
                                style =
                                    MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = colores.onPrimaryContainer
                            )

                            EstadoVisual(a.autorizacion)
                        }
                    }

                    // Autorizaciones: cliente y administrador

                    if (
                        !tecnico &&
                        a.estado == "Cotizada" &&
                        a.autorizacion == "Pendiente"
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                enabled = accionesHabilitadas,
                                onClick = {
                                    autorizar(a.id, "Aprobada")
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {

                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    Modifier.size(18.dp)
                                )

                                Spacer(Modifier.width(6.dp))

                                Text("Autorizar")
                            }

                            OutlinedButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                enabled = accionesHabilitadas,
                                onClick = {
                                    autorizar(a.id, "Rechazada")
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {

                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    Modifier.size(18.dp)
                                )

                                Spacer(Modifier.width(6.dp))

                                Text("Rechazar")
                            }
                        }
                    }

                    // Botón para consultar detalles

                    TextButton(
                        onClick = {
                            expandido = !expandido
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 10.dp
                        )
                    ) {

                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            if (expandido)
                                "Ocultar detalles"
                            else
                                "Historial, informe y garantía",
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            if (expandido)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    // Detalles de la atención

                    AnimatedVisibility(expandido) {

                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(14.dp)
                        ) {

                            HorizontalDivider(
                                color = colores.outlineVariant
                            )

                            Text(
                                "Información del servicio",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            DatoAtencion(
                                "Tipo de servicio",
                                a.tipoServicio
                            )

                            DatoAtencion(
                                "Contacto",
                                a.correoContacto
                            )

                            // Informe técnico

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = colores.surfaceVariant.copy(
                                    alpha = 0.45f
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {

                                        Icon(
                                            Icons.Default.List,
                                            null,
                                            tint = colores.primary
                                        )

                                        Text(
                                            "Informe técnico",
                                            fontWeight =
                                                FontWeight.SemiBold
                                        )
                                    }

                                    Text(
                                        a.informe.ifBlank {
                                            "Pendiente"
                                        },
                                        style =
                                            MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            // Garantía y mantención

                            DatoAtencion(
                                "Garantía hasta",
                                fechaLegible(
                                    a.garantiaHasta,
                                    "Sin registrar"
                                )
                            )

                            DatoAtencion(
                                "Próxima mantención",
                                fechaLegible(
                                    a.mantenimiento,
                                    "Sin programar"
                                )
                            )

                            // Evidencia fotográfica

                            if (a.evidencia.isNotBlank()) {

                                Text(
                                    "Evidencia del equipo",
                                    style =
                                        MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Surface(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = colores.surfaceVariant
                                ) {

                                    AsyncImage(
                                        model =
                                            BuildConfig.ATENCIONES_URL +
                                                    "api/evidencias/" +
                                                    a.evidencia,
                                        contentDescription =
                                            "Evidencia de atención",
                                        imageLoader = imagenes,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .padding(8.dp)
                                            .clip(
                                                RoundedCornerShape(12.dp)
                                            )
                                    )
                                }
                            }

                            // Historial técnico

                            HorizontalDivider(
                                color = colores.outlineVariant
                            )

                            Text(
                                "Historial técnico",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (a.historial.isEmpty()) {

                                Text(
                                    "Sin movimientos registrados.",
                                    style =
                                        MaterialTheme.typography.bodyMedium
                                )
                            }

                            a.historial.forEach { registro ->

                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {

                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(20.dp),
                                        tint = colores.primary
                                    )

                                    Text(
                                        historialLegible(registro),
                                        style =
                                            MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Edición y eliminación según el rol

                    if (!cliente) {

                        HorizontalDivider(
                            color = colores.outlineVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                enabled = accionesHabilitadas,
                                onClick = { editar(a) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {

                                Icon(
                                    Icons.Default.Edit,
                                    null,
                                    Modifier.size(18.dp)
                                )

                                Spacer(Modifier.width(6.dp))

                                Text("Editar atención")
                            }

                            if (!tecnico) {

                                Button(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    enabled = accionesHabilitadas,
                                    onClick = { eliminar(a) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor =
                                            colores.errorContainer,
                                        contentColor =
                                            colores.onErrorContainer,
                                        disabledContainerColor =
                                            colores.onSurface.copy(
                                                alpha = 0.12f
                                            ),
                                        disabledContentColor =
                                            colores.onSurface.copy(
                                                alpha = 0.38f
                                            )
                                    )
                                ) {

                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        Modifier.size(18.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
        }
    }
}

// Componente para mostrar información

@Composable
private fun DatoAtencion(
    etiqueta: String,
    valor: String
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Formato de fechas de garantía y mantención

private fun fechaLegible(
    fecha: String,
    alternativa: String
): String {

    if (fecha.isBlank()) return alternativa

    return runCatching {

        LocalDate.parse(fecha).format(
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy",
                Locale.forLanguageTag("es-CL")
            )
        )

    }.getOrDefault(fecha)
}

// Formato de fechas del historial técnico

private fun historialLegible(
    registro: String
): String {

    val patron = Regex(
        """^\d{4}-\d{2}-\d{2}T\S+"""
    )

    val coincidencia =
        patron.find(registro) ?: return registro

    return runCatching {

        val fecha = Instant.parse(coincidencia.value)

        val fechaFormateada = fecha
            .atZone(ZoneId.of("America/Santiago"))
            .format(
                DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm",
                    Locale.forLanguageTag("es-CL")
                )
            )

        registro.replaceRange(
            coincidencia.range,
            fechaFormateada
        )

    }.getOrDefault(registro)
}
