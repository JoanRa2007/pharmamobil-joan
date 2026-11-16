package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.pharmamobil.data.remote.dto.CategoriaDto
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.UsuarioDto
import pe.edu.upeu.pharmamobil.domain.model.Resultado

/** Rutas de PharmaSoft bajo /api/v1/ consumidas por la app. */
class PharmaApi(private val http: HttpClient) {

    suspend fun login(req: LoginRequestDto): Resultado<LoginResponseDto> = llamar {
        http.post("auth/login") { contentType(ContentType.Application.Json); setBody(req) }
    }

    suspend fun usuarioActual(): Resultado<UsuarioDto> = llamar { http.get("auth/me") }

    suspend fun categorias(): Resultado<List<CategoriaDto>> = llamar { http.get("categorias") }

    suspend fun productos(pagina: Int, tamanio: Int = ApiConfig.TAMANIO_PAGINA): Resultado<PaginaDto<ProductoDto>> =
        llamar {
            http.get("productos") {
                parameter("pagina", pagina)
                parameter("tamanio", tamanio)
                parameter("ordenarPor", "nombre")
            }
        }

    suspend fun crearProducto(req: ProductoRequestDto): Resultado<ProductoDto> = llamar {
        http.post("productos") { contentType(ContentType.Application.Json); setBody(req) }
    }

    suspend fun actualizarProducto(id: Long, req: ProductoRequestDto): Resultado<ProductoDto> = llamar {
        http.put("productos/$id") { contentType(ContentType.Application.Json); setBody(req) }
    }

    suspend fun eliminarProducto(id: Long): Resultado<Unit> = llamar { http.delete("productos/$id") }
}
