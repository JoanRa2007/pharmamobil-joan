package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.ResumenSincronizacion
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/** Sincronizacion basica: primero SUBE lo pendiente y despues BAJA lo actualizado. */
class SincronizarUseCase(
    private val productos: ProductoRepository,
    private val ahora: () -> Long,
) {
    suspend operator fun invoke(): Resultado<ResumenSincronizacion> {
        val (enviados, rechazos) = when (val r = productos.enviarPendientes()) {
            is Resultado.Exito -> r.dato
            is Resultado.Fallo -> return r
        }
        val recibidos = when (val r = productos.descargar()) {
            is Resultado.Exito -> r.dato
            is Resultado.Fallo -> return r
        }
        productos.registrarSincronizacion(ahora())
        return Resultado.Exito(ResumenSincronizacion(enviados, rechazos.size, recibidos, 0, rechazos))
    }
}
