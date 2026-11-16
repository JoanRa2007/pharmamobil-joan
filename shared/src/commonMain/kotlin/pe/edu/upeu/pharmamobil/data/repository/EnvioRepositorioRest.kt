package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.data.remote.PharmaApi
import pe.edu.upeu.pharmamobil.data.remote.dto.aDominio
import pe.edu.upeu.pharmamobil.data.remote.dto.aRequest
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRemotoRepository

/** Envio por REST. Aqui, en la capa de datos, cada ErrorApi se traduce a ResultadoEnvio. */
class EnvioRepositorioRest(private val api: PharmaApi) : ProductoRemotoRepository {

    override suspend fun enviar(fila: Producto): ResultadoEnvio {
        val r = if (fila.id < 0) api.crearProducto(fila.aRequest()) else api.actualizarProducto(fila.id, fila.aRequest())
        return when (r) {
            is Resultado.Exito -> ResultadoEnvio.Exito(r.dato.aDominio())
            is Resultado.Fallo -> r.error.aResultado()
        }
    }

    private fun ErrorApi.aResultado(): ResultadoEnvio = when (this) {
        is ErrorApi.SinConexion, is ErrorApi.TiempoAgotado,
        is ErrorApi.Servidor, is ErrorApi.Desconocido -> ResultadoEnvio.Transitorio(message)
        is ErrorApi.Validacion -> ResultadoEnvio.Permanente(
            campos.entries.joinToString("; ") { "${it.key}: ${it.value}" }.ifBlank { mensaje },
        )
        is ErrorApi.NoEncontrado, is ErrorApi.Conflicto, is ErrorApi.SinPrivilegio -> ResultadoEnvio.Permanente(message)
        is ErrorApi.NoAutorizado -> ResultadoEnvio.SesionVencida
    }
}
