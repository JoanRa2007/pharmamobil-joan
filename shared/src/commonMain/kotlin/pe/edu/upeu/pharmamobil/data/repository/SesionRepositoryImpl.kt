package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.StateFlow
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.EstadoSesion
import pe.edu.upeu.pharmamobil.data.remote.PharmaApi
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.aSesion
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.SesionRepository

class SesionRepositoryImpl(
    private val api: PharmaApi,
    private val almacen: AlmacenSeguro,
    private val estado: EstadoSesion,
    private val ahora: () -> Long,
) : SesionRepository {

    override val sesion: StateFlow<Sesion?> = estado.sesion

    override suspend fun restaurar(): Sesion? {
        val guardada = almacen.leerSesion() ?: return null
        if (guardada.expiraEn <= ahora()) {
            almacen.borrar() // token vencido: vuelve al login sin mensajes tecnicos
            return null
        }
        estado.establecer(guardada)
        return guardada
    }

    override suspend fun iniciarSesion(usuario: String, contrasena: String): Resultado<Sesion> =
        when (val r = api.login(LoginRequestDto(usuario, contrasena))) {
            is Resultado.Fallo -> r
            is Resultado.Exito -> {
                val sesion = r.dato.aSesion(ahora())
                almacen.guardarSesion(sesion)
                estado.establecer(sesion)
                Resultado.Exito(sesion)
            }
        }

    override suspend fun cerrarSesion() {
        almacen.borrar()
        estado.limpiar()
    }
}
