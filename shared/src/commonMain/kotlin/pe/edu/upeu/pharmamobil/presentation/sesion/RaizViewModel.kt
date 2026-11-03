package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RestaurarSesionUseCase

sealed interface EstadoRaiz {
    data object Cargando : EstadoRaiz
    data object SinSesion : EstadoRaiz
    data class ConSesion(val sesion: Sesion) : EstadoRaiz
}

/** Decide entre login y area protegida. Si el token guardado sigue vigente se entra directo. */
class RaizViewModel(
    restaurarSesion: RestaurarSesionUseCase,
    observarSesion: ObservarSesionUseCase,
) : ViewModel() {
    private val restaurada = MutableStateFlow(false)

    val estado: StateFlow<EstadoRaiz> = combine(restaurada, observarSesion()) { lista, sesion ->
        when {
            !lista -> EstadoRaiz.Cargando
            sesion == null -> EstadoRaiz.SinSesion
            else -> EstadoRaiz.ConSesion(sesion)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EstadoRaiz.Cargando)

    init {
        viewModelScope.launch {
            restaurarSesion()
            restaurada.value = true
        }
    }
}
