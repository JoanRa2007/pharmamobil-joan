package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable

/** Envoltorio de paginacion de PharmaSoft (PaginaResponseDTO). */
@Serializable
data class PaginaDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean,
)

/** Formato de error comun de PharmaSoft (ErrorResponseDTO). */
@Serializable
data class ErrorDto(
    val timestamp: String? = null,
    val status: Int = 0,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null,
    val validationErrors: Map<String, String>? = null,
)

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String? = null,
    val expiraEnSegundos: Long,
    val username: String,
    val nombreCompleto: String? = null,
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList(),
)

@Serializable
data class UsuarioDto(
    val id: Long,
    val username: String,
    val empleadoNombre: String? = null,
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList(),
)

@Serializable
data class CategoriaDto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val estado: Boolean = true,
)

@Serializable
data class ProductoDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long,
    val categoriaNombre: String? = null,
)

@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean,
    val categoriaId: Long,
)
