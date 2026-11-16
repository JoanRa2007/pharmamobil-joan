package pe.edu.upeu.pharmamobil.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pe.edu.upeu.pharmamobil.data.local.aDominio
import pe.edu.upeu.pharmamobil.data.local.aFila
import pe.edu.upeu.pharmamobil.data.remote.PharmaApi
import pe.edu.upeu.pharmamobil.data.remote.dto.aDominio
import pe.edu.upeu.pharmamobil.data.remote.dto.aRequest
import pe.edu.upeu.pharmamobil.db.PharmaDatabase
import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

private const val CLAVE_ULTIMA_SINCRONIZACION = "ultima_sincronizacion"

/**
 * Catalogo offline-first.
 * - Con red: se envia a PharmaSoft y se guarda la fila definitiva.
 * - Sin red: se guarda con id NEGATIVO y pendiente = 1; se envia al sincronizar.
 */
class ProductoRepositoryImpl(
    private val db: PharmaDatabase,
    private val api: PharmaApi,
    private val io: CoroutineDispatcher,
) : ProductoRepository {
    private val q get() = db.pharmaDbQueries

    override fun observarProductos(texto: String?): Flow<List<Producto>> =
        q.observarProductos(texto?.trim()?.takeIf { it.isNotEmpty() }).asFlow().mapToList(io)
            .map { filas -> filas.map { it.aDominio() } }

    override fun observarCategorias(): Flow<List<Categoria>> =
        q.observarCategorias().asFlow().mapToList(io).map { filas -> filas.map { it.aDominio() } }

    override suspend fun registrar(producto: Producto): Resultado<Producto> =
        when (val r = api.crearProducto(producto.aRequest())) {
            is Resultado.Exito -> {
                val definitivo = r.dato.aDominio()
                withContext(io) { q.guardarProducto(definitivo.aFila()) }
                Resultado.Exito(definitivo)
            }
            is Resultado.Fallo -> if (r.error.esDeRed) Resultado.Exito(guardarProvisional(producto)) else r
        }

    /** Id de un rango que el servidor nunca emite (negativo), asignado en una transaccion. */
    private suspend fun guardarProvisional(producto: Producto): Producto = withContext(io) {
        db.transactionWithResult {
            val minimo: Long = q.siguienteIdLocal().executeAsOneOrNull() ?: 0L
            val provisional = producto.copy(id = minOf(minimo, 0L) - 1, pendiente = true)
            q.guardarProducto(provisional.aFila())
            provisional
        }
    }

    override suspend fun actualizar(producto: Producto): Resultado<Producto> {
        if (producto.id < 0) {
            val local = producto.copy(pendiente = true)
            withContext(io) { q.guardarProducto(local.aFila()) }
            return Resultado.Exito(local)
        }
        return when (val r = api.actualizarProducto(producto.id, producto.aRequest())) {
            is Resultado.Exito -> {
                val definitivo = r.dato.aDominio()
                withContext(io) { q.guardarProducto(definitivo.aFila()) }
                Resultado.Exito(definitivo)
            }
            is Resultado.Fallo -> when {
                r.error.esDeRed -> {
                    val local = producto.copy(pendiente = true)
                    withContext(io) { q.guardarProducto(local.aFila()) }
                    Resultado.Exito(local)
                }
                r.error is ErrorApi.NoEncontrado -> {
                    withContext(io) { q.eliminarProducto(producto.id) }
                    r
                }
                else -> r
            }
        }
    }

    override suspend fun eliminar(id: Long): Resultado<Unit> {
        if (id < 0) {
            withContext(io) { q.eliminarProducto(id) } // nunca llego al servidor
            return Resultado.Exito(Unit)
        }
        val r = api.eliminarProducto(id)
        if (r is Resultado.Exito || (r is Resultado.Fallo && r.error is ErrorApi.NoEncontrado)) {
            withContext(io) { q.eliminarProducto(id) }
        }
        return r
    }

    override suspend fun descargar(): Resultado<Int> {
        val categorias = when (val r = api.categorias()) {
            is Resultado.Exito -> r.dato
            is Resultado.Fallo -> return r
        }
        val productos = mutableListOf<Producto>()
        var pagina = 0
        do {
            val r = api.productos(pagina)
            if (r is Resultado.Fallo) return r
            val datos = (r as Resultado.Exito).dato
            productos += datos.contenido.map { it.aDominio() }
            pagina++
        } while (!datos.ultima)
        withContext(io) {
            db.transaction {
                q.eliminarCategorias()
                categorias.forEach { q.guardarCategoria(it.id, it.nombre) }
                q.eliminarSincronizados()
                // INSERT OR IGNORE: no pisa filas con cambios locales pendientes.
                productos.forEach { q.guardarProductoSiNoPendiente(it.aFila()) }
            }
        }
        return Resultado.Exito(productos.size)
    }

    override suspend fun registrarSincronizacion(instante: Long) {
        withContext(io) { q.guardarMetadato(CLAVE_ULTIMA_SINCRONIZACION, instante.toString()) }
    }

    override fun observarUltimaSincronizacion(): Flow<Long?> =
        q.observarMetadato(CLAVE_ULTIMA_SINCRONIZACION).asFlow().mapToOneOrNull(io).map { it?.toLongOrNull() }
}
