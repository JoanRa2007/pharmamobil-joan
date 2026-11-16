package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio

/** Lado remoto de la cola de envio. */
interface ProductoRemotoRepository {
    /** Crea (id negativo) o corrige (id positivo) el producto en PharmaSoft. */
    suspend fun enviar(fila: Producto): ResultadoEnvio
}
