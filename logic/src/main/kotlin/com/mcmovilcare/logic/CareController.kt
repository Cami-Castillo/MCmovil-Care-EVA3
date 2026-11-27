package com.mcmovilcare.logic

import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException

class CareController(private val repo: CareRepository, private val scope: CoroutineScope) {
    private val _state = MutableStateFlow(CareState())
    val state = _state.asStateFlow()

    fun cargar() {
        if (state.value.cargando || state.value.guardando) return
        _state.update { it.copy(cargando = true, error = null) }
        scope.launch {
            try {
                refrescar()
                val p = repo.perfil()
                val d = repo.preferencia()
                _state.update { it.copy(perfil = p, mostrarDiagnosticos = d) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fallo(e)
            } finally {
                _state.update { it.copy(cargando = false) }
            }
        }
    }

    private suspend fun refrescar() {
        val equipos = repo.equipos()
        val atenciones = repo.atenciones()
        _state.update {
            it.copy(
                equipos = equipos.items,
                atenciones = atenciones.items,
                offline = equipos.cache || atenciones.cache,
            )
        }
    }

    fun equipo(e: Equipo) {
        val errores = Validacion.equipo(e)
        if (invalido(errores)) return
        escribir {
            repo.guardarEquipo(
                e.copy(
                    nombre = e.nombre.trim(),
                    marca = e.marca.trim(),
                    modelo = e.modelo.trim(),
                    numeroSerie = e.numeroSerie.trim(),
                    diagnostico = e.diagnostico.trim(),
                )
            )
        }
    }

    fun atencion(a: Atencion) {
        val errores = Validacion.atencion(a, state.value.equipos.map { it.id }.toSet())
        if (invalido(errores)) return
        escribir {
            repo.guardarAtencion(
                a.copy(descripcion = a.descripcion.trim(), correoContacto = a.correoContacto.trim())
            )
        }
    }

    fun eliminarEquipo(id: String) = escribir { repo.eliminarEquipo(id) }

    fun eliminarAtencion(id: Long) = escribir { repo.eliminarAtencion(id) }

    fun autorizar(id: Long, valor: String) {
        if (
            invalido(
                if (valor in Validacion.autorizaciones) emptyMap()
                else mapOf("autorizacion" to "Valor inválido")
            )
        )
            return
        escribir { repo.autorizar(id, valor) }
    }

    private fun invalido(errores: Map<String, String>): Boolean {
        _state.update { it.copy(errores = errores) }
        return errores.isNotEmpty()
    }

    fun perfil(p: Perfil) {
        if (invalido(Validacion.perfil(p))) return
        escribir(false) {
            repo.guardarPerfil(p)
            _state.update { it.copy(perfil = p) }
        }
    }

    fun preferencia(valor: Boolean) =
        escribir(false) {
            repo.preferencia(valor)
            _state.update { it.copy(mostrarDiagnosticos = valor) }
        }

    fun subir(bytes: ByteArray, resultado: (String) -> Unit) =
        escribir(false) { resultado(repo.subir(bytes)) }

    fun feriados(year: Int) {
        scope.launch {
            try {
                val lista = repo.feriados(year)
                _state.update { it.copy(feriados = lista, errorExterno = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorExterno = "No se pudo consultar la API externa. Reintenta.")
                }
            }
        }
    }

    private fun escribir(refrescar: Boolean = true, accion: suspend () -> Unit) {
        if (state.value.guardando || state.value.cargando) return
        if (refrescar && state.value.offline) {
            _state.update {
                it.copy(error = "Sin conexión: consulta disponible, cambios deshabilitados")
            }
            return
        }
        _state.update { it.copy(guardando = true, error = null, mensaje = null) }
        scope.launch {
            try {
                accion()
                _state.update {
                    it.copy(revision = it.revision + 1, mensaje = "Operación guardada")
                }
                if (refrescar)
                    try {
                        refrescar()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error =
                                    "Se guardó, pero no se pudo refrescar. Actualiza antes de repetir."
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fallo(e)
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private fun fallo(e: Exception) {
        val mensaje =
            when (e) {
                is HttpException ->
                    when (e.code()) {
                        400 -> "Revisa los datos enviados"
                        404 -> "El registro ya no existe"
                        409 -> "No puedes eliminar un equipo con atenciones"
                        503 -> "El otro microservicio no está disponible"
                        else -> "Error del servidor HTTP ${e.code()}"
                    }
                is IOException -> "No hay conexión. Actualiza antes de repetir un envío."
                else -> "No se pudo completar la operación"
            }
        _state.update { it.copy(error = mensaje) }
    }
}
