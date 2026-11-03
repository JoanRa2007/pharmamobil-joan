package pe.edu.upeu.pharmamobil.domain.port

// commonMain/domain/port/Telemetria.kt
/**
 * Lo que el dominio necesita para registrar fallos. No sabe que detras esta Firebase:
 * si cambia el proveedor, solo cambia una clase de la capa de datos.
 * Nunca se envian datos personales ni el token: solo contexto tecnico (pantalla, operacion, codigo).
 */
interface Telemetria {
    fun registrarError(e: Throwable, contexto: Map<String, String> = emptyMap())
    fun registrarEvento(nombre: String, datos: Map<String, String> = emptyMap())
}
