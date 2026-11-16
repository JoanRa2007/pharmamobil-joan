package pe.edu.upeu.pharmamobil

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import pe.edu.upeu.pharmamobil.domain.port.NotificadorSistema
import pe.edu.upeu.pharmamobil.domain.usecase.AvisarFallidasUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** El gestor de permisos es una interfaz: en la prueba se reemplaza por una implementacion falsa. */
class PermisosTest {
    private class GestorFalso(var estadoActual: EstadoPermiso) : GestorPermisos {
        var solicitudes = 0
        var ajustesAbiertos = 0
        override suspend fun estado(permiso: Permiso) = estadoActual
        override suspend fun solicitar(permiso: Permiso): EstadoPermiso { solicitudes++; return estadoActual }
        override fun abrirAjustes() { ajustesAbiertos++ }
    }

    private class NotificadorFalso : NotificadorSistema {
        val avisos = mutableListOf<Int>()
        override fun notificarFallidas(cantidad: Int) { avisos += cantidad }
    }

    @Test
    fun conPermisoConcedidoSeNotificanLasFallidas() = runTest {
        val notificador = NotificadorFalso()
        val avisar = AvisarFallidasUseCase(GestorFalso(EstadoPermiso.Concedido), notificador)

        assertTrue(avisar(3))
        assertEquals(listOf(3), notificador.avisos)
    }

    @Test
    fun sinPermisoNoSeNotificaYNoSePideSolo() = runTest {
        val notificador = NotificadorFalso()
        for (estado in listOf(EstadoPermiso.Denegado, EstadoPermiso.DenegadoParaSiempre)) {
            val gestor = GestorFalso(estado)
            assertFalse(AvisarFallidasUseCase(gestor, notificador)(2))
            assertEquals(0, gestor.solicitudes, "el permiso se pide desde la interfaz, explicando antes")
        }
        assertTrue(notificador.avisos.isEmpty())
    }

    @Test
    fun sinFallidasNoSeConsultaNiNotifica() = runTest {
        val notificador = NotificadorFalso()
        assertFalse(AvisarFallidasUseCase(GestorFalso(EstadoPermiso.Concedido), notificador)(0))
        assertTrue(notificador.avisos.isEmpty())
    }
}
