package pe.edu.upeu.pharmamobil.domain.model

// domain/model/ResultadoEnvio.kt
/** Resultado de enviar UNA fila pendiente. El caso de uso no conoce codigos HTTP. */
sealed interface ResultadoEnvio {
    data class Exito(val definitivo: Producto) : ResultadoEnvio {
        val idServidor: Long get() = definitivo.id
    }

    /** Sin red, tiempo agotado o servicio no disponible: se reintenta con espera creciente. */
    data class Transitorio(val motivo: String) : ResultadoEnvio

    /** Validacion, recurso inexistente, conflicto o falta de privilegio: reintentar no lo arregla. */
    data class Permanente(val motivo: String) : ResultadoEnvio

    /**
     * 401: la fila no tiene la culpa; se detiene la sincronizacion SIN marcarla fallida.
     * (Extension a la guia: marcarla Permanente haria perder filas correctas cuando vence el token.)
     */
    data object SesionVencida : ResultadoEnvio
}
