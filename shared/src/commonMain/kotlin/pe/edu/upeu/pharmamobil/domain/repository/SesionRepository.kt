package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.model.Sesion

interface SesionRepository {
    val sesion: StateFlow<Sesion?>
    /** Recupera la sesion guardada si el token sigue vigente. */
    suspend fun restaurar(): Sesion?
    suspend fun iniciarSesion(usuario: String, contrasena: String): Resultado<Sesion>
    suspend fun cerrarSesion()
}

/** Estado de la red del dispositivo (lo implementa cada plataforma). */
interface MonitorConexion {
    val conectado: StateFlow<Boolean>
}
