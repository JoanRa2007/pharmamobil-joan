package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ProductoLocalRepository

/** Filas que aun se reintentaran solas. */
class ObservarPendientesUseCase(private val local: ProductoLocalRepository) {
    operator fun invoke() = local.observarPendientes()
}

/** Filas que agotaron los intentos o fueron rechazadas: requieren atencion del usuario. */
class ObservarFallidasUseCase(private val local: ProductoLocalRepository) {
    operator fun invoke() = local.observarFallidas()
}

/** «Reintentar ahora»: las filas fallidas vuelven a la cola de envio. */
class ReintentarFallidasUseCase(private val local: ProductoLocalRepository) {
    suspend operator fun invoke() = local.reintentarFallidas()
}
