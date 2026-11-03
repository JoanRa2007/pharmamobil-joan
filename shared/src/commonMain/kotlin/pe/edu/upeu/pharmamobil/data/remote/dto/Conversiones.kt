package pe.edu.upeu.pharmamobil.data.remote.dto

import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Sesion

/** Conversion DTO <-> dominio. El dominio no conoce anotaciones de serializacion. */
fun ProductoDto.aDominio() = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock,
    categoriaId = categoriaId,
    categoriaNombre = categoriaNombre.orEmpty(),
    estado = estado,
    pendiente = false,
)

fun Producto.aRequest() = ProductoRequestDto(
    nombre = nombre,
    precio = precio,
    stock = stock,
    estado = estado,
    categoriaId = categoriaId,
)

fun CategoriaDto.aDominio() = Categoria(id = id, nombre = nombre)

fun LoginResponseDto.aSesion(ahora: Long) = Sesion(
    token = token,
    usuario = username,
    nombre = nombreCompleto ?: username,
    roles = roles,
    permisos = permisos.toSet(),
    expiraEn = ahora + expiraEnSegundos * 1_000,
)
