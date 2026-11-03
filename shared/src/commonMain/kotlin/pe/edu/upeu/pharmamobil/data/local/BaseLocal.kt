package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.db.SqlDriver
import pe.edu.upeu.pharmamobil.db.PharmaDatabase

/** Cada plataforma aporta su controlador de SQLDelight. */
interface BaseLocal {
    fun crearDriver(): SqlDriver
}

fun BaseLocal.abrir(): PharmaDatabase = PharmaDatabase(crearDriver())

/** Conversiones fila local <-> dominio (INTEGER 0/1 -> Boolean). */
internal fun pe.edu.upeu.pharmamobil.Producto.aDominio() = pe.edu.upeu.pharmamobil.domain.model.Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock.toInt(),
    categoriaId = categoriaId,
    categoriaNombre = categoriaNombre,
    estado = estado == 1L,
    pendiente = pendiente == 1L,
    intentos = intentos.toInt(),
    ultimoError = ultimoError,
)

internal fun pe.edu.upeu.pharmamobil.domain.model.Producto.aFila() = pe.edu.upeu.pharmamobil.Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock.toLong(),
    estado = if (estado) 1L else 0L,
    categoriaId = categoriaId,
    categoriaNombre = categoriaNombre,
    pendiente = if (pendiente) 1L else 0L,
    intentos = intentos.toLong(),
    ultimoError = ultimoError,
)

internal fun pe.edu.upeu.pharmamobil.Categoria.aDominio() =
    pe.edu.upeu.pharmamobil.domain.model.Categoria(id = id, nombre = nombre)
