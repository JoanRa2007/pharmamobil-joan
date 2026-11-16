package pe.edu.upeu.pharmamobil.data

import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import pe.edu.upeu.pharmamobil.domain.port.NotificadorSistema

/**
 * Pendiente (sesion 14): implementar con UNUserNotificationCenter y declarar las claves de uso en Info.plist
 * (si falta la clave, iOS cierra la app al pedir el permiso). Hoy solo se evalua Android.
 */
class GestorPermisosIos : GestorPermisos {
    override suspend fun estado(permiso: Permiso): EstadoPermiso = EstadoPermiso.Denegado
    override suspend fun solicitar(permiso: Permiso): EstadoPermiso = EstadoPermiso.Denegado
    override fun abrirAjustes() = Unit
}

class NotificadorIos : NotificadorSistema {
    override fun notificarFallidas(cantidad: Int) = Unit
}
