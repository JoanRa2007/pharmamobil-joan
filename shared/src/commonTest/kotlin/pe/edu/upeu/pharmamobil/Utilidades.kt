package pe.edu.upeu.pharmamobil

import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.EstadoSesion
import pe.edu.upeu.pharmamobil.data.remote.ConfigPlataforma
import pe.edu.upeu.pharmamobil.data.remote.PharmaApi
import pe.edu.upeu.pharmamobil.data.remote.crearClienteHttp
import pe.edu.upeu.pharmamobil.data.repository.EnvioRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ProductoLocalRepositoryImpl
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositoryImpl
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarUseCase
import pe.edu.upeu.pharmamobil.db.PharmaDatabase
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.port.Telemetria

/** Controlador de base en memoria (lo aporta cada plataforma de prueba). */
expect fun crearDriverPrueba(): SqlDriver

/** Telemetria falsa: guarda lo registrado para poder verificarlo. */
class TelemetriaFalsa : Telemetria {
    val errores = mutableListOf<Pair<Throwable, Map<String, String>>>()
    val eventos = mutableListOf<Pair<String, Map<String, String>>>()
    override fun registrarError(e: Throwable, contexto: Map<String, String>) { errores += e to contexto }
    override fun registrarEvento(nombre: String, datos: Map<String, String>) { eventos += nombre to datos }
}

class AlmacenEnMemoria : AlmacenSeguro {
    var sesion: Sesion? = Sesion("token-prueba", "almacen", "Jorge Mendoza", listOf("ALMACENERO"),
        setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR"), Long.MAX_VALUE)
    override fun guardarSesion(sesion: Sesion) { this.sesion = sesion }
    override fun leerSesion() = sesion
    override fun leerToken() = sesion?.token
    override fun borrar() { sesion = null }
}

/** Arma la capa de datos con el motor HTTP simulado y la base en memoria. */
open class Entorno(manejador: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
    val peticiones = mutableListOf<String>()
    val almacen = AlmacenEnMemoria()
    val estado = EstadoSesion()
    val db = PharmaDatabase(crearDriverPrueba())
    private val motor = MockEngine { req ->
        peticiones += "${req.method.value} ${req.url.encodedPath}"
        manejador(req)
    }
    val api = PharmaApi(crearClienteHttp(motor, almacen, estado, ConfigPlataforma("http://servidor/api/v1/")))
    val productos = ProductoRepositoryImpl(db, api, Dispatchers.Unconfined)
    val local = ProductoLocalRepositoryImpl(db, Dispatchers.Unconfined)
    val remoto = EnvioRepositorioRest(api)
    val esperas = mutableListOf<Long>()

    /** Caso de uso con la espera registrada en vez de real: las pruebas no duermen. */
    val telemetria = TelemetriaFalsa()
    open fun sincronizar() = SincronizarUseCase(local, remoto, productos, telemetria, ahora = { 1L }, esperar = { esperas += it })
}

fun MockRequestHandleScope.json(cuerpo: String, estado: HttpStatusCode = HttpStatusCode.OK) =
    respond(cuerpo, estado, headersOf(HttpHeaders.ContentType, "application/json"))

fun errorJson(estado: Int, mensaje: String, campos: String = "") =
    """{"timestamp":"2026-11-03T09:14:22","status":$estado,"error":"x","message":"$mensaje","path":"/api/v1/productos"$campos}"""

const val CATEGORIAS_JSON = """[{"id":1,"nombre":"Analgesicos","descripcion":"Dolor","estado":true}]"""

fun productoJson(id: Long, nombre: String = "Paracetamol 500 mg") =
    """{"id":$id,"nombre":"$nombre","precio":12.5,"stock":80,"estado":true,"categoriaId":1,"categoriaNombre":"Analgesicos","fechaCreacion":"2026-11-03T10:00:00"}"""

fun pagina(vararg elementos: String) =
    """{"contenido":[${elementos.joinToString(",")}],"pagina":0,"tamanio":50,"totalElementos":${elementos.size},"totalPaginas":1,"ultima":true}"""
