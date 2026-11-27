package com.example.appmcmovilcare_castillo_farias

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import androidx.compose.ui.test.*
import com.example.appmcmovilcare_castillo_farias.ui.*
import com.example.appmcmovilcare_castillo_farias.ui.screens.*
import com.example.appmcmovilcare_castillo_farias.ui.theme.AppMCmovilCare_Castillo_FariasTheme

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Rule

@OptIn(ExperimentalTestApi::class)
class InterfazTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun inicioMuestraContadoresReales(): Unit =
        compose.run {
            setContent {
                AppMCmovilCare_Castillo_FariasTheme {
                    Dashboard(CareState(equipos = listOf(Equipo(id = "1"))), {}, {}, {})
                }
            }
            onNodeWithText("1 equipos").performScrollTo().assertIsDisplayed()
        }

    @Test
    fun botonEquiposComunicaAccion(): Unit =
        compose.run {
            var clicks = 0
            setContent {
                AppMCmovilCare_Castillo_FariasTheme { Dashboard(CareState(), { clicks++ }, {}, {}) }
            }
            onNodeWithText("Consultar equipos").performScrollTo().performClick()
            runOnIdle { assertEquals(1, clicks) }
        }

    @Test
    fun inicioPermiteReintentar(): Unit =
        compose.run {
            var clicks = 0
            setContent {
                AppMCmovilCare_Castillo_FariasTheme {
                    Dashboard(CareState(error = "Sin conexión"), {}, {}, { clicks++ })
                }
            }
            onNodeWithText("Reintentar").performScrollTo().performClick()
            runOnIdle { assertEquals(1, clicks) }
        }

    @Test
    fun edicionEquipoEntregaDatosIngresados(): Unit =
        compose.run {
            var enviado: Equipo? = null
            setContent {
                AppMCmovilCare_Castillo_FariasTheme {
                    EquipoForm(CareState(), Equipo(), { enviado = it }, {})
                }
            }
            listOf(
                    "equipo_nombre" to "Notebook",
                    "equipo_marca" to "HP",
                    "equipo_modelo" to "Pavilion",
                    "equipo_serie" to "001",
                )
                .forEach { (tag, texto) ->
                    onNodeWithTag(tag).performScrollTo().performTextInput(texto)
                }
            onNodeWithTag("guardar_equipo").performScrollTo().performClick()
            runOnIdle { assertEquals("Notebook", enviado?.nombre) }
        }

    @Test
    fun campoMuestraErrorDelEstado(): Unit =
        compose.run {
            setContent {
                AppMCmovilCare_Castillo_FariasTheme {
                    EquipoForm(
                        CareState(errores = mapOf("nombre" to "Completa el nombre")),
                        Equipo(),
                        {},
                        {},
                    )
                }
            }
            onNodeWithText("Completa el nombre").assertIsDisplayed()
        }

    @Test
    fun guardarDeshabilitadoSinConexion(): Unit =
        compose.run {
            setContent {
                AppMCmovilCare_Castillo_FariasTheme {
                    EquipoForm(CareState(offline = true), Equipo(), {}, {})
                }
            }
            onNodeWithTag("guardar_equipo").performScrollTo().assertIsNotEnabled()
        }

    @Test
    fun guardarDeshabilitadoDuranteEnvio(): Unit =
        compose.run {
            setContent {
                AppMCmovilCare_Castillo_FariasTheme {
                    AtencionForm(CareState(guardando = true), Atencion(), {}, {})
                }
            }
            onNodeWithTag("guardar_atencion").performScrollTo().assertIsNotEnabled()
        }

    @Test
    fun agendaIdentificaApiExterna(): Unit =
        compose.run {
            setContent { AppMCmovilCare_Castillo_FariasTheme { AgendaScreen(CareState(), {}) } }
            onNodeWithText("Agenda de mantenciones").assertIsDisplayed()
        }

    @Test
    fun agendaAdvierteFeriadoNacional(): Unit = compose.run {
        val hoy = java.time.LocalDate.now().toString()
        setContent {
            AppMCmovilCare_Castillo_FariasTheme {
                AgendaScreen(CareState(feriados = listOf(Feriado(hoy, "Feriado de prueba"))), {})
            }
        }
        onNodeWithText("Feriado de prueba · Feriado nacional").performScrollTo().assertIsDisplayed()
        onNodeWithText("Día hábil de referencia:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun aperturaMuestraSoloLogin(): Unit =
        compose.run {
            setContent { AppMCmovilCare_Castillo_FariasTheme { AccesoApp() } }
            onNodeWithText("Iniciar sesión").performScrollTo().assertIsDisplayed()
            onNodeWithText("Consultar equipos").assertDoesNotExist()
            onNodeWithText("Agenda").assertDoesNotExist()
            onNodeWithText("Iniciar sesión").performScrollTo().performClick()
            onNodeWithText("Ingresa tu usuario").performScrollTo().assertIsDisplayed()
            onNodeWithText("Ingresa tu contraseña").performScrollTo().assertIsDisplayed()
        }
}
