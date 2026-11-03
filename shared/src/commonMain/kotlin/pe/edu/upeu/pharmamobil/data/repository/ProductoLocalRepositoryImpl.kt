package pe.edu.upeu.pharmamobil.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import pe.edu.upeu.pharmamobil.data.local.aDominio
import pe.edu.upeu.pharmamobil.data.local.aFila
import pe.edu.upeu.pharmamobil.db.PharmaDatabase
import pe.edu.upeu.pharmamobil.domain.model.ColaEnvio
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoLocalRepository

/** Cola de envio sobre SQLDelight: cada fila recuerda sus intentos y su ultimo error. */
class ProductoLocalRepositoryImpl(
    private val db: PharmaDatabase,
    private val io: CoroutineDispatcher,
) : ProductoLocalRepository {
    private val q get() = db.pharmaDbQueries
    private val max = ColaEnvio.MAX_INTENTOS.toLong()

    override suspend fun pendientes(maxIntentos: Int): List<Producto> = withContext(io) {
        q.pendientesPorEnviar(maxIntentos.toLong()).executeAsList().map { it.aDominio() }
    }

    override suspend fun confirmar(idLocal: Long, definitivo: Producto): Unit = withContext(io) {
        db.transaction {
            q.eliminarProducto(idLocal)
            q.guardarProducto(definitivo.copy(pendiente = false, intentos = 0, ultimoError = null).aFila())
        }
    }

    override suspend fun marcarFallida(id: Long, motivo: String): Unit = withContext(io) {
        q.marcarFallida(maxIntentos = max, motivo = motivo, id = id)
    }

    override suspend fun sumarIntento(id: Long, motivo: String): Unit = withContext(io) {
        q.sumarIntento(motivo, id)
    }

    override suspend fun reintentarFallidas(): Unit = withContext(io) {
        q.reintentarFallidas(max)
    }

    override fun observarPendientes(): Flow<Long> = q.contarPendientes(max).asFlow().mapToOne(io)
    override fun observarFallidas(): Flow<Long> = q.contarFallidas(max).asFlow().mapToOne(io)
}
