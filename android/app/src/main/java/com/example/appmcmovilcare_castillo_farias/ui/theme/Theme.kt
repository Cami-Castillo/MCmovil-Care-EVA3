package com.example.appmcmovilcare_castillo_farias.ui.theme

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Paleta para el modo oscuro.
private val DarkColorScheme =
    darkColorScheme(primary = Color(0xFFABC8FF), secondary = Color(0xFFBCC9DB), tertiary = Color(0xFF92D6CC), background = Color(0xFF101827), surface = Color(0xFF192537), surfaceVariant = Color(0xFF25354B), primaryContainer = Color(0xFF263F65), onPrimaryContainer = Color(0xFFDCE8FF))

// Paleta para el modo claro.
private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF2864C7), onPrimary = Color.White,
        primaryContainer = Color(0xFFDCE8FF), onPrimaryContainer = Color(0xFF153675),
        secondary = Color(0xFF475569), secondaryContainer = Color(0xFFE2E8F0),
        tertiary = Color(0xFF087E89), tertiaryContainer = Color(0xFFC9F2F2),
        background = Color(0xFFF5F7FA), onBackground = Color(0xFF17304F),
        surface = Color.White, surfaceContainer = Color.White, surfaceContainerLow = Color.White, surfaceContainerLowest = Color.White, surfaceContainerHigh = Color(0xFFEAF0F8), surfaceContainerHighest = Color(0xFFE3EBF6), onSurface = Color(0xFF17304F),
        surfaceVariant = Color(0xFFE2E8F0), onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFF8293AB), error = Color(0xFFB42318)
    )

@Composable
fun AppMCmovilCare_Castillo_FariasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Usa por defecto la paleta de MCmovil Care.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current

                if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = Shapes(small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp)), content = content)
}
