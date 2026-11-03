package pe.edu.upeu.pharmamobil

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.aDominio
import pe.edu.upeu.pharmamobil.data.local.aFila
import pe.edu.upeu.pharmamobil.domain.model.ColaEnvio
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.model.ResumenSincronizacion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// commonTest/SincronizacionTest.kt — sesion 13: cola de envio con intentos y espera creciente.
class SincronizacionTest {
    private val productoDePrueba =
        Producto(-1, "Vitamina C 1 g", 19.9, 30, categoriaId = 1, categoriaNombre = "Vitaminas", pendiente = true)

    private fun Entorno.insertarPendiente(p: Producto = productoDePrueba) = db.pharmaDbQueries.guardarProducto(p.aFila())
    private fun Entorno.porId(id: Long) = db.pharmaDbQueries.obtenerProducto(id).executeAsOne().aDominio()

    /** PharmaSoft responde [envio] al POST; las descargas responden vacias. */
    private fun armarCasoDeUso(envio: HttpStatusCode) = Entorno { req ->
        when {
            req.method == HttpMethod.Post -> json(errorJson(envio.value, "Motivo de PharmaSoft"), envio)
            req.url.encodedPath.endsWith("/categorias") -> json(CATEGORIAS_JSON)
            else -> json(pagina())
        }
    }

    @Test
    fun unFalloTransitorioSumaIntentoYNoMarcaFallida() = runTest {
        val e = armarCasoDeUso(HttpStatusCode.ServiceUnavailable)
        e.insertarPendiente()

        e.sincronizar()()

        val fila = e.porId(productoDePrueba.id)
        assertEquals(1, fila.intentos)
        assertTrue(fila.pendiente)
        assertFalse(fila.fallido)
        assertNotNull(fila.ultimoError)
        assertEquals(listOf(1_000L), e.esperas, "primera espera de 1 s")
    }

    @Test
    fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        val e = armarCasoDeUso(HttpStatusCode.Conflict)
        e.insertarPendiente()

        val resumen = assertIs<Resultado.Exito<ResumenSincronizacion>>(e.sincronizar()()).dato

        val fila = e.porId(productoDePrueba.id)
        assertTrue(fila.fallido, "la fila queda fallida y visible para el usuario")
        assertEquals("Motivo de PharmaSoft", fila.ultimoError)
        assertEquals(1, resumen.fallidos)
        assertEquals("envio_rechazado", e.telemetria.eventos.single().first)
        assertTrue(e.esperas.isEmpty(), "un fallo permanente no espera ni reintenta")

        e.peticiones.clear()
        e.sincronizar()()
        assertTrue(e.peticiones.none { it.startsWith("POST") }, "no se vuelve a enviar sola")
    }

    @Test
    fun laEsperaCreceYAlAgotarLosIntentosLaFilaQuedaFallida() = runTest {
        val e = armarCasoDeUso(HttpStatusCode.ServiceUnavailable)
        e.insertarPendiente()

        repeat(ColaEnvio.MAX_INTENTOS + 1) { e.sincronizar()() }

        assertEquals(listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L), e.esperas)
        assertTrue(e.porId(productoDePrueba.id).fallido)
        assertEquals(ColaEnvio.MAX_INTENTOS, e.peticiones.count { it.startsWith("POST") }, "no reintenta sin limite")
    }

    @Test
    fun laSesionVencidaDetieneSinMarcarLaFila() = runTest {
        val e = armarCasoDeUso(HttpStatusCode.Unauthorized)
        e.insertarPendiente()

        val r = e.sincronizar()()

        assertIs<ErrorApi.NoAutorizado>(assertIs<Resultado.Fallo>(r).error)
        assertEquals(0, e.porId(productoDePrueba.id).intentos, "la fila no tiene la culpa de que venza el token")
    }

    @Test
    fun reintentarFallidasLasDevuelveALaCola() = runTest {
        val e = armarCasoDeUso(HttpStatusCode.Conflict)
        e.insertarPendiente()
        e.sincronizar()()

        e.local.reintentarFallidas()

        assertFalse(e.porId(productoDePrueba.id).fallido)
        assertEquals(1, e.local.pendientes(ColaEnvio.MAX_INTENTOS).size)
    }
}
