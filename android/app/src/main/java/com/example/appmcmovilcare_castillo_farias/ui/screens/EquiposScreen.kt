
package com.example.appmcmovilcare_castillo_farias.ui.screens

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.appmcmovilcare_castillo_farias.ui.components.*

@Composable
internal fun EquiposScreen(
    s: CareState,
    nuevo: () -> Unit,
    editar: (Equipo) -> Unit,
    eliminar: (Equipo) -> Unit,
    puedeGestionar: Boolean,
) {

    val colores = MaterialTheme.colorScheme

    val accionesHabilitadas =
        !s.offline && !s.cargando && !s.guardando

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // Encabezado de la pantalla

        item {

            Encabezado(
                "Equipos registrados",
                "Consulta el inventario y el estado de los equipos.",
                Icons.Default.Build
            )

            Spacer(Modifier.height(14.dp))

            if (puedeGestionar) {

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    onClick = nuevo,
                    enabled = accionesHabilitadas,
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Registrar equipo",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Mensaje cuando no existen equipos

        if (s.equipos.isEmpty() && !s.cargando) {

            item {

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = colores.surface
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Surface(
                            color = colores.primaryContainer,
                            shape = RoundedCornerShape(18.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(18.dp)
                                    .size(32.dp),
                                tint = colores.primary
                            )
                        }

                        Text(
                            text = "Aún no hay equipos.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (puedeGestionar)
                                "Registra un equipo para comenzar."
                            else
                                "Los equipos registrados aparecerán aquí.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colores.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Listado de equipos registrados

        items(
            items = s.equipos,
            key = { it.id }
        ) { e ->

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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Identificación del equipo

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colores.primaryContainer
                        ) {

                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(14.dp)
                                    .size(28.dp),
                                tint = colores.primary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {

                            Text(
                                text = e.nombre,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${e.marca} · ${e.modelo}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colores.onSurfaceVariant
                            )

                            EstadoVisual(e.estado)
                        }
                    }

                    HorizontalDivider(
                        color = colores.outlineVariant
                    )

                    // Número de serie

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colores.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {

                            Text(
                                text = "Número de serie",
                                style = MaterialTheme.typography.labelMedium,
                                color = colores.onSurfaceVariant
                            )

                            Text(
                                text = e.numeroSerie,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Diagnóstico del equipo

                    if (s.mostrarDiagnosticos) {

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = colores.surfaceVariant.copy(
                                alpha = 0.45f
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        tint = colores.primary,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Text(
                                        text = "Diagnóstico",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = e.diagnostico.ifBlank {
                                        "Sin diagnóstico registrado."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colores.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Acciones exclusivas del administrador

                    if (puedeGestionar) {

                        HorizontalDivider(
                            color = colores.outlineVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                enabled = accionesHabilitadas,
                                onClick = { editar(e) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp
                                )
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(Modifier.width(6.dp))

                                Text("Editar")
                            }

                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                enabled = accionesHabilitadas,
                                onClick = { eliminar(e) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colores.errorContainer,
                                    contentColor = colores.onErrorContainer,
                                    disabledContainerColor =
                                        colores.onSurface.copy(alpha = 0.12f),
                                    disabledContentColor =
                                        colores.onSurface.copy(alpha = 0.38f)
                                )
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(Modifier.width(6.dp))

                                Text("Eliminar")
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
