package pe.edu.upeu.pharmamobil.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobil.domain.model.Sesion

/** Almacen seguro de la sesion. Android: preferencias cifradas con clave en el Android Keystore. */
interface AlmacenSeguro {
    fun guardarSesion(sesion: Sesion)
    fun leerSesion(): Sesion?
    fun leerToken(): String?
    fun borrar()
}

/** Sesion en memoria observada por la interfaz (null = mostrar login). */
class EstadoSesion {
    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    fun establecer(sesion: Sesion) { _sesion.value = sesion }
    fun limpiar() { _sesion.value = null }
}
