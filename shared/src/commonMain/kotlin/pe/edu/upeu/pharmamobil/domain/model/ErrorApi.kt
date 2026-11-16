package pe.edu.upeu.pharmamobil.domain.model

/**
 * Jerarquia sellada de errores del dominio. La capa de datos traduce cada respuesta
 * del servicio (o falla de red) a uno de estos casos.
 */
sealed class ErrorApi(override val message: String) : Exception(message) {
    data class Validacion(val mensaje: String, val campos: Map<String, String>) : ErrorApi(mensaje)
    data class NoAutorizado(val mensaje: String = "Tu sesion vencio. Inicia sesion nuevamente") : ErrorApi(mensaje)
    data class SinPrivilegio(val mensaje: String = "No estas autorizado para esta operacion") : ErrorApi(mensaje)
    data class NoEncontrado(val mensaje: String = "El registro ya no existe") : ErrorApi(mensaje)
    data class Conflicto(val mensaje: String) : ErrorApi(mensaje)
    data class Servidor(val mensaje: String = "El servidor tuvo un problema. Intenta de nuevo") : ErrorApi(mensaje)
    data object SinConexion : ErrorApi("Sin conexion con el servidor")
    data object TiempoAgotado : ErrorApi("El servidor tardo demasiado en responder")
    data class Desconocido(val mensaje: String) : ErrorApi(mensaje)

    val esDeRed: Boolean get() = this is SinConexion || this is TiempoAgotado
}

sealed interface Resultado<out T> {
    data class Exito<T>(val dato: T) : Resultado<T>
    data class Fallo(val error: ErrorApi) : Resultado<Nothing>
}
