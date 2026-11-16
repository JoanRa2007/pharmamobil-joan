package pe.edu.upeu.pharmamobil

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.aDominio
import pe.edu.upeu.pharmamobil.data.remote.jsonPharma
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

class ConversionYErroresTest {
    private val req = ProductoRequestDto("Paracetamol 500 mg", 12.5, 10, true, 1)
    private fun fallo(r: Resultado<*>): ErrorApi = assertIs<Resultado.Fallo>(r).error

    @Test
    fun elListadoLlegaEnElEnvoltorioDePaginacionYToleraClavesDesconocidas() {
        val pagina = jsonPharma.decodeFromString<PaginaDto<ProductoDto>>(pagina(productoJson(7), productoJson(8, "Ibuprofeno 400 mg")))
        val productos = pagina.contenido.map { it.aDominio() }
        assertEquals(listOf(7L, 8L), productos.map { it.id })
        assertEquals("Analgesicos", productos.first().categoriaNombre)
        assertFalse(productos.first().pendiente, "lo que llega del servidor nunca esta pendiente")
    }

    @Test
    fun cada400y409SeTraduceAUnErrorTipadoConSuMotivo() = runTest {
        val e400 = Entorno { json(errorJson(400, "Datos invalidos", ""","validationErrors":{"precio":"El precio debe ser mayor que cero"}"""), HttpStatusCode.BadRequest) }
        assertEquals("El precio debe ser mayor que cero", assertIs<ErrorApi.Validacion>(fallo(e400.api.crearProducto(req))).campos["precio"])

        val e409 = Entorno { json(errorJson(409, "Ya existe un producto con ese nombre"), HttpStatusCode.Conflict) }
        assertEquals("Ya existe un producto con ese nombre", assertIs<ErrorApi.Conflicto>(fallo(e409.api.crearProducto(req))).mensaje)
    }

    @Test
    fun el403MantieneLaSesionYEl401LaBorra() = runTest {
        val e403 = Entorno { json(errorJson(403, "Acceso denegado"), HttpStatusCode.Forbidden) }
        assertIs<ErrorApi.SinPrivilegio>(fallo(e403.api.eliminarProducto(5)))
        assertEquals("token-prueba", e403.almacen.leerToken())

        val e401 = Entorno { json(errorJson(401, "Token vencido"), HttpStatusCode.Unauthorized) }
        assertIs<ErrorApi.NoAutorizado>(fallo(e401.api.productos(0)))
        assertNull(e401.almacen.leerToken(), "un 401 borra el token")
    }

    @Test
    fun el204NoConvierteElCuerpoYLaFaltaDeRedEsUnErrorPropio() = runTest {
        val e204 = Entorno { respond("", HttpStatusCode.NoContent) }
        assertIs<Resultado.Exito<Unit>>(e204.api.eliminarProducto(3))

        val sinRed = Entorno { throw kotlinx.io.IOException("sin senal") }
        assertEquals(ErrorApi.SinConexion, fallo(sinRed.api.productos(0)))
    }
}
