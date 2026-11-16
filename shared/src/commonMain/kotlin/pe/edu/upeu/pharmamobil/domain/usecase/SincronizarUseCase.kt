package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import pe.edu.upeu.pharmamobil.domain.model.ColaEnvio
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.model.ResumenSincronizacion
import pe.edu.upeu.pharmamobil.domain.port.Telemetria
import pe.edu.upeu.pharmamobil.domain.repository.ProductoLocalRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRemotoRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Sincronizacion offline-first refinada (sesion 13).
 *  1. SUBE cada fila pendiente distinguiendo el fallo transitorio (suma intento y espera) del
 *     permanente (la fila queda fallida y visible); el maximo de intentos evita reintentar sin fin.
 *  2. Solo despues BAJA categorias y productos. Invertirlo borraria o pisaria lo pendiente.
 */
class SincronizarUseCase(
    private val local: ProductoLocalRepository,
    private val remoto: ProductoRemotoRepository,
    private val catalogo: ProductoRepository,
    private val telemetria: Telemetria,
    private val ahora: () -> Long,
    private val esperar: suspend (Long) -> Unit = { delay(it) },
) {
    suspend operator fun invoke(): Resultado<ResumenSincronizacion> {
        var enviadas = 0
        var fallidas = 0
        val motivos = mutableListOf<String>()

        for (fila in local.pendientes(MAX_INTENTOS)) {
            when (val r = remoto.enviar(fila)) {
                is ResultadoEnvio.Exito -> {
                    local.confirmar(fila.id, r.definitivo)
                    enviadas++
                }
                is ResultadoEnvio.Permanente -> {
                    local.marcarFallida(fila.id, r.motivo)
                    fallidas++
                    motivos += "${fila.nombre}: ${r.motivo}"
                    telemetria.registrarEvento(
                        "envio_rechazado",
                        mapOf("pantalla" to "productos", "operacion" to if (fila.id < 0) "crear_producto" else "corregir_producto"),
                    )
                }
                is ResultadoEnvio.Transitorio -> {
                    local.sumarIntento(fila.id, r.motivo)
                    if (fila.intentos + 1 >= MAX_INTENTOS) fallidas++
                    esperar(espera(fila.intentos))
                }
                ResultadoEnvio.SesionVencida -> return Resultado.Fallo(ErrorApi.NoAutorizado())
            }
        }

        // Solo despues de subir se baja: invertirlo borra lo pendiente.
        val recibidos = when (val r = catalogo.descargar()) {
            is Resultado.Exito -> r.dato
            is Resultado.Fallo -> {
                // La falta de red es normal; interesa registrar los fallos del servidor o inesperados.
                if (r.error is ErrorApi.Servidor || r.error is ErrorApi.Desconocido) {
                    telemetria.registrarError(r.error, mapOf("pantalla" to "productos", "operacion" to "descargar_catalogo"))
                }
                return r
            }
        }
        catalogo.registrarSincronizacion(ahora())

        return Resultado.Exito(
            ResumenSincronizacion(
                enviados = enviadas,
                fallidos = fallidas,
                recibidos = recibidos,
                pendientes = local.observarPendientes().first().toInt(),
                motivos = motivos,
            ),
        )
    }

    /** 1 s, 2 s, 4 s, 8 s, 16 s: una red intermitente no castiga la bateria. */
    private fun espera(intentos: Int): Long = 1_000L shl intentos.coerceAtMost(4)

    companion object {
        const val MAX_INTENTOS = ColaEnvio.MAX_INTENTOS
    }
}
