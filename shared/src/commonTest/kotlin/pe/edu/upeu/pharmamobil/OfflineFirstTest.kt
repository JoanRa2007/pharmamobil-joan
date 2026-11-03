package pe.edu.upeu.pharmamobil

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.model.ResumenSincronizacion
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OfflineFirstTest {
    private val nuevo = Producto(0, "Vitamina C 1 g", 19.9, 30, categoriaId = 1, categoriaNombre = "Vitaminas")

    @Test
    fun laValidacionLocalUsaLosMismosCamposQuePharmaSoft() = runTest {
        val e = Entorno { error("no debe llamar a la red") }
        val r = RegistrarProductoUseCase(e.productos)(null, "ab", "-1", "x", null)
        val campos = assertIs<ErrorApi.Validacion>(assertIs<Resultado.Fallo>(r).error).campos
        assertEquals(setOf("nombre", "precio", "stock", "categoriaId"), campos.keys)
        assertTrue(e.peticiones.isEmpty())
    }

    @Test
    fun sinRedElProductoQuedaPendienteConIdProvisionalNegativo() = runTest {
        val e = Entorno { throw kotlinx.io.IOException("modo avion") }
        val uno = assertIs<Resultado.Exito<Producto>>(e.productos.registrar(nuevo)).dato
        val dos = assertIs<Resultado.Exito<Producto>>(e.productos.registrar(nuevo.copy(nombre = "Complejo B"))).dato
        assertEquals(listOf(-1L, -2L), listOf(uno.id, dos.id))
        assertTrue(uno.pendiente && dos.pendiente)
    }

    @Test
    fun primeroSubeLoPendienteYDespuesBajaSinDuplicar() = runTest {
        var hayRed = false
        val e = Entorno { req ->
            if (!hayRed) throw kotlinx.io.IOException("sin senal")
            when {
                req.method == HttpMethod.Post -> json(productoJson(50, "Vitamina C 1 g"), HttpStatusCode.Created)
                req.url.encodedPath.endsWith("/categorias") -> json(CATEGORIAS_JSON)
                else -> json(pagina(productoJson(50, "Vitamina C 1 g"), productoJson(7)))
            }
        }
        e.productos.registrar(nuevo)
        hayRed = true
        e.peticiones.clear()

        val resumen = assertIs<Resultado.Exito<ResumenSincronizacion>>(e.sincronizar()()).dato

        assertEquals("POST /api/v1/productos", e.peticiones.first(), "primero se sube lo pendiente")
        assertEquals(1, resumen.enviados)
        val ids = e.db.pharmaDbQueries.observarProductos(null).executeAsList().map { it.id }.sorted()
        assertEquals(listOf(7L, 50L), ids, "la provisional -1 fue sustituida por la definitiva 50")
    }
}
