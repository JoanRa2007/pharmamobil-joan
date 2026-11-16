package pe.edu.upeu.pharmamobil.presentation.comun

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi

/** Mensaje breve; con [accion] se muestra un boton (p. ej. «Reintentar» ante un 500). */
data class Aviso(val texto: String, val accion: String? = null, val alAccionar: (() -> Unit)? = null)

/** Canal unico de avisos (snackbar) que usan los ViewModel. */
class Avisos {
    private val _flujo = MutableSharedFlow<Aviso>(extraBufferCapacity = 8)
    val flujo: SharedFlow<Aviso> = _flujo.asSharedFlow()

    fun mostrar(aviso: Aviso) { _flujo.tryEmit(aviso) }
    fun mostrar(texto: String) = mostrar(Aviso(texto))

    /** 401 lo resuelve el cliente HTTP (vuelve al login); 500 ofrece reintentar; el resto se informa. */
    fun informar(error: ErrorApi, reintentar: (() -> Unit)? = null) {
        when (error) {
            is ErrorApi.NoAutorizado -> Unit
            is ErrorApi.Servidor -> mostrar(Aviso(error.mensaje, "Reintentar", reintentar))
            is ErrorApi.SinPrivilegio -> mostrar("Operacion no autorizada: ${error.mensaje}")
            else -> mostrar(error.message)
        }
    }
}
