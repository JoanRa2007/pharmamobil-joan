package pe.edu.upeu.pharmamobil.presentation.sincronizacion

// presentation/sincronizacion/SincronizacionUiState.kt
/** Lo que el usuario necesita saber sin abrir ningun menu: red, cambios por enviar y antiguedad de los datos. */
data class SincronizacionUiState(
    val enCurso: Boolean = false,
    val conectado: Boolean = true,
    val pendientes: Int = 0,
    val fallidas: Int = 0,
    val ultimaSync: Long? = null, // epoch en milisegundos
) {
    /** «Sin conexión · 3 cambios por enviar» dice más que un ícono rojo. */
    fun resumen(): String = buildString {
        append(if (conectado) "En línea" else "Sin conexión")
        append(" · ")
        append(
            when (pendientes) {
                0 -> "Todo enviado"
                1 -> "1 cambio por enviar"
                else -> "$pendientes cambios por enviar"
            },
        )
    }

    /** «Actualizado hace 2 h» dice más que una fecha completa. */
    fun antiguedad(ahora: Long): String {
        val ultima = ultimaSync ?: return "Datos nunca sincronizados"
        val minutos = (ahora - ultima).coerceAtLeast(0) / 60_000
        return when {
            minutos < 1 -> "Actualizado hace un momento"
            minutos < 60 -> "Actualizado hace $minutos min"
            minutos < 60 * 24 -> "Actualizado hace ${minutos / 60} h"
            else -> "Actualizado hace ${minutos / (60 * 24)} d"
        }
    }
}
