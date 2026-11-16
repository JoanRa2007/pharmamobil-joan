package pe.edu.upeu.pharmamobil.domain.permiso

// commonMain/domain/permiso/GestorPermisos.kt
enum class Permiso { CAMARA, UBICACION, NOTIFICACIONES }

sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso
    data object Denegado : EstadoPermiso            // se puede volver a pedir
    data object DenegadoParaSiempre : EstadoPermiso // solo queda Ajustes
}

/**
 * Se declara en el codigo comun como interfaz: la implementacion de Android necesita la
 * Activity para lanzar el dialogo del sistema y el codigo comun no puede nombrarla.
 */
interface GestorPermisos {
    suspend fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
}
