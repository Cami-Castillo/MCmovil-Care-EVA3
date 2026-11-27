package com.example.appmcmovilcare_castillo_farias.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun Encabezado(titulo: String, texto: String, icono: ImageVector) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(titulo, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(texto, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Icon(icono, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun EstadoVisual(estado: String) {
    val fondo = when(estado) {
        "Finalizada", "Aprobada", "Operativo" -> Color(0xFFE0F3E9)
        "Pendiente", "En revisión", "En reparación" -> Color(0xFFFFF0D6)
        "Rechazada" -> Color(0xFFFCE5E5)
        else -> Color(0xFFE3EDFC)
    }
    val tinta = when(estado) {
        "Finalizada", "Aprobada", "Operativo" -> Color(0xFF17633D)
        "Pendiente", "En revisión", "En reparación" -> Color(0xFF805400)
        "Rechazada" -> Color(0xFF9B2828)
        else -> Color(0xFF17458E)
    }
    Surface(color = fondo, contentColor = tinta, shape = RoundedCornerShape(50)) {
        Text(estado, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
    }
}
