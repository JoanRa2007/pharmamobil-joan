package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Registra (id == null) o corrige un producto. Valida con las mismas reglas que PharmaSoft
 * (ProductoRequestDTO) y usa los mismos nombres de campo que sus validationErrors, para que
 * un error local y uno del servidor lleguen igual al campo del formulario.
 */
class RegistrarProductoUseCase(
    private val productoRepository: ProductoRepository,
) {
    suspend operator fun invoke(
        id: Long?,
        nombre: String,
        precio: String,
        stock: String,
        categoriaId: Long?,
        categoriaNombre: String = "",
    ): Resultado<Producto> {
        val campos = buildMap {
            validarNombre(nombre)?.let { put("nombre", it) }
            validarPrecio(precio)?.let { put("precio", it) }
            validarStock(stock)?.let { put("stock", it) }
            if (categoriaId == null) put("categoriaId", "Selecciona una categoria")
        }
        if (campos.isNotEmpty()) return Resultado.Fallo(ErrorApi.Validacion("Revisa los campos marcados", campos))

        val producto = Producto(
            id = id ?: 0L,
            nombre = nombre.trim(),
            precio = precio.replace(',', '.').toDouble(),
            stock = stock.toInt(),
            categoriaId = categoriaId!!,
            categoriaNombre = categoriaNombre,
        )
        return if (id == null) productoRepository.registrar(producto) else productoRepository.actualizar(producto)
    }

    private fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length !in 3..150 -> "El nombre debe tener entre 3 y 150 caracteres"
        else -> null
    }

    private fun validarPrecio(precio: String): String? {
        val valor = precio.replace(',', '.').toDoubleOrNull()
        return when {
            precio.isBlank() -> "El precio es obligatorio"
            valor == null || !valor.isFinite() -> "El precio debe ser un número válido"
            valor <= 0 -> "El precio debe ser mayor a 0"
            else -> null
        }
    }

    private fun validarStock(stock: String): String? {
        val valor = stock.toIntOrNull()
        return when {
            stock.isBlank() -> "El stock es obligatorio"
            valor == null -> "El stock debe ser un número entero"
            valor < 0 -> "El stock no puede ser negativo"
            else -> null
        }
    }
}
