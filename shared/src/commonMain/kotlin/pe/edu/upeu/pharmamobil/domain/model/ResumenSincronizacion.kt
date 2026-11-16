package pe.edu.upeu.pharmamobil.domain.model

data class ResumenSincronizacion(
    val enviados: Int = 0,
    val fallidos: Int = 0,
    val recibidos: Int = 0,
    val pendientes: Int = 0,
    val motivos: List<String> = emptyList(),
) {
    fun texto(): String = buildString {
        append("Enviados: $enviados · Recibidos: $recibidos productos")
        if (pendientes > 0) append(" · Quedan $pendientes por enviar")
        if (motivos.isNotEmpty()) append("\nNo enviados ($fallidos): ${motivos.joinToString("; ")}")
    }
}
