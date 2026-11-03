package pe.edu.upeu.pharmamobil.presentation.producto

import kotlin.math.roundToLong
import pe.edu.upeu.pharmamobil.domain.model.Producto


data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String,
    val stock: String,
    val categoria: String,
    val requiereReposicion: Boolean,
    val pendiente: Boolean,
    val fallido: Boolean,
    val intentos: Int,
    val ultimoError: String?,
    /** Dominio original, para precargar el formulario al corregir. */
    val producto: Producto,
)

fun Producto.aUi(): ProductoUi = ProductoUi(
    id = id,
    nombre = nombre,
    precio = precio.enSoles(),
    stock = "$stock u.",
    categoria = categoriaNombre,
    requiereReposicion = requiereReposicion,
    pendiente = pendiente,
    fallido = fallido,
    intentos = intentos,
    ultimoError = ultimoError,
    producto = this,
)

/** Kotlin comun no trae String.format, asi que armamos los dos decimales a mano. */
private fun Double.enSoles(): String {

    val centavos = (this * 100).roundToLong()

    val enteros = centavos / 100
    val decimales = (centavos % 100).toString().padStart(2, '0')

    return "S/ $enteros.$decimales"
}
