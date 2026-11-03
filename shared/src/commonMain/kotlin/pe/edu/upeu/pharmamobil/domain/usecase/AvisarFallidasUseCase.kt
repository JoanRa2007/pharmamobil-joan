package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import pe.edu.upeu.pharmamobil.domain.port.NotificadorSistema

/** Avisa con una notificacion del sistema que hay productos sin enviar, solo si el usuario lo permitio. */
class AvisarFallidasUseCase(
    private val permisos: GestorPermisos,
    private val notificador: NotificadorSistema,
) {
    /** Devuelve true si la notificacion se mostro. */
    suspend operator fun invoke(fallidas: Int): Boolean {
        if (fallidas <= 0) return false
        if (permisos.estado(Permiso.NOTIFICACIONES) != EstadoPermiso.Concedido) return false
        notificador.notificarFallidas(fallidas)
        return true
    }
}
