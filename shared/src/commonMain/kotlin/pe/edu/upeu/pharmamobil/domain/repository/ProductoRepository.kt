package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Resultado

/** Catalogo offline-first: la interfaz SIEMPRE lee de la base local. */
interface ProductoRepository {
    fun observarProductos(texto: String?): Flow<List<Producto>>
    fun observarCategorias(): Flow<List<Categoria>>

    /** Con red se envia; sin red queda pendiente con id negativo. */
    suspend fun registrar(producto: Producto): Resultado<Producto>
    suspend fun actualizar(producto: Producto): Resultado<Producto>
    suspend fun eliminar(id: Long): Resultado<Unit>

    /** Baja categorias y productos del servidor sin pisar filas pendientes. Devuelve cuantos productos bajo. */
    suspend fun descargar(): Resultado<Int>
    suspend fun registrarSincronizacion(instante: Long)
    fun observarUltimaSincronizacion(): Flow<Long?>
}
