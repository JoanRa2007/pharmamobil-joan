package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase

data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val errorUsuario: String? = null,
    val errorContrasena: String? = null,
    val mensaje: String? = null,
)

class LoginViewModel(private val iniciarSesion: IniciarSesionUseCase) : ViewModel() {
    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onUsuario(v: String) = _estado.update { it.copy(usuario = v, errorUsuario = null, mensaje = null) }
    fun onContrasena(v: String) = _estado.update { it.copy(contrasena = v, errorContrasena = null, mensaje = null) }

    fun ingresar() {
        val s = _estado.value
        if (s.cargando) return
        _estado.update { it.copy(cargando = true, mensaje = null) }
        viewModelScope.launch {
            when (val r = iniciarSesion(s.usuario, s.contrasena)) {
                is Resultado.Exito -> _estado.value = LoginUiState()
                is Resultado.Fallo -> _estado.update {
                    when (val e = r.error) {
                        is ErrorApi.Validacion -> it.copy(
                            cargando = false, errorUsuario = e.campos["usuario"], errorContrasena = e.campos["contrasena"],
                        )
                        // Mensajes DISTINTOS para credenciales incorrectas y para falta de conexion.
                        is ErrorApi.NoAutorizado -> it.copy(cargando = false, mensaje = "Usuario o contraseña incorrectos")
                        ErrorApi.SinConexion, ErrorApi.TiempoAgotado ->
                            it.copy(cargando = false, mensaje = "No hay conexión con PharmaSoft. Revisa tu red e intenta de nuevo")
                        else -> it.copy(cargando = false, mensaje = e.message)
                    }
                }
            }
        }
    }
}
