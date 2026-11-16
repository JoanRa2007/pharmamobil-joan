package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto

/** Cola local de productos pendientes de envio (sesion 13). Solo contrato. */
interface ProductoLocalRepository {
    /** Filas pendientes que aun no agotaron [maxIntentos], en el orden en que se registraron. */
    suspend fun pendientes(maxIntentos: Int): List<Producto>

    /** El servidor acepto la fila: la provisional se sustituye por la definitiva en una transaccion. */
    suspend fun confirmar(idLocal: Long, definitivo: Producto)

    /** Fallo permanente: la fila queda fallida y visible con su motivo. */
    suspend fun marcarFallida(id: Long, motivo: String)

    /** Fallo transitorio: suma un intento y guarda el ultimo error. */
    suspend fun sumarIntento(id: Long, motivo: String)

    /** Accion del usuario: las filas fallidas vuelven a la cola. */
    suspend fun reintentarFallidas()

    fun observarPendientes(): Flow<Long>
    fun observarFallidas(): Flow<Long>
}
