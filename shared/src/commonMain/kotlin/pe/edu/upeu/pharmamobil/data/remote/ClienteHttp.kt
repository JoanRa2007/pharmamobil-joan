package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.EstadoSesion

/** Tolerante a claves desconocidas (fechaCreacion, fechaModificacion...). */
val jsonPharma = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

/**
 * UNICA instancia de HttpClient (single en Koin).
 * La cabecera Authorization se agrega aqui para todas las peticiones; un 401 en una ruta
 * protegida borra el token y limpia la sesion, y la app vuelve al login.
 */
fun crearClienteHttp(
    motor: HttpClientEngine,
    almacen: AlmacenSeguro,
    estadoSesion: EstadoSesion,
    config: ConfigPlataforma,
): HttpClient = HttpClient(motor) {
    expectSuccess = false
    install(ContentNegotiation) { json(jsonPharma) }
    install(HttpTimeout) {
        connectTimeoutMillis = ApiConfig.TIEMPO_CONEXION_MS
        requestTimeoutMillis = ApiConfig.TIEMPO_PETICION_MS
        socketTimeoutMillis = ApiConfig.TIEMPO_PETICION_MS
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = if (config.depurar) LogLevel.INFO else LogLevel.NONE
        sanitizeHeader { it == HttpHeaders.Authorization }
    }
    defaultRequest { url(config.urlBase) }
    install(
        createClientPlugin("AutorizacionPharma") {
            onRequest { request, _ ->
                almacen.leerToken()?.let { request.headers[HttpHeaders.Authorization] = "Bearer $it" }
            }
            onResponse { response ->
                val esLogin = response.call.request.url.encodedPath.endsWith("auth/login")
                if (response.status == HttpStatusCode.Unauthorized && !esLogin) {
                    almacen.borrar()
                    estadoSesion.limpiar()
                }
            }
        },
    )
}
