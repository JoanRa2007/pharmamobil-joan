package pe.edu.upeu.pharmamobil.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarSesionUseCase

class PerfilViewModel(
    observarSesion: ObservarSesionUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
    private val permisos: GestorPermisos,
) : ViewModel() {
    val sesion = observarSesion()

    private val _notificaciones = MutableStateFlow<EstadoPermiso?>(null)
    /** Estado del permiso de notificaciones (null mientras se consulta). */
    val notificaciones: StateFlow<EstadoPermiso?> = _notificaciones.asStateFlow()

    /** Se llama al mostrar la pantalla y al volver de Ajustes. */
    fun refrescarPermiso() {
        viewModelScope.launch { _notificaciones.value = permisos.estado(Permiso.NOTIFICACIONES) }
    }

    fun activarNotificaciones() {
        viewModelScope.launch { _notificaciones.value = permisos.solicitar(Permiso.NOTIFICACIONES) }
    }

    fun abrirAjustes() = permisos.abrirAjustes()

    /** Borra el token del almacenamiento seguro; la raiz detecta la sesion nula y muestra el login. */
    fun cerrarSesion() {
        viewModelScope.launch { cerrarSesionUseCase() }
    }
}
