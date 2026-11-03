package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ObservarProductosUseCase(private val repo: ProductoRepository) {
    operator fun invoke(texto: String? = null) = repo.observarProductos(texto)
}

class ObservarCategoriasUseCase(private val repo: ProductoRepository) {
    operator fun invoke() = repo.observarCategorias()
}

class ObservarUltimaSincronizacionUseCase(private val repo: ProductoRepository) {
    operator fun invoke() = repo.observarUltimaSincronizacion()
}

class EliminarProductoUseCase(private val repo: ProductoRepository) {
    suspend operator fun invoke(id: Long): Resultado<Unit> = repo.eliminar(id)
}
