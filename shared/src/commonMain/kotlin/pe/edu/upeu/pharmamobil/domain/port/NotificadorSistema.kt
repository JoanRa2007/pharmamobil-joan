package pe.edu.upeu.pharmamobil.domain.port

/** Notificaciones del sistema operativo. Cada plataforma aporta la suya. */
interface NotificadorSistema {
    fun notificarFallidas(cantidad: Int)
}
