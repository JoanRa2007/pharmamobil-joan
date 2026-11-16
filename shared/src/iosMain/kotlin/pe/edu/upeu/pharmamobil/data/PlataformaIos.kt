package pe.edu.upeu.pharmamobil.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSUserDefaults
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.BaseLocal
import pe.edu.upeu.pharmamobil.db.PharmaDatabase
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.MonitorConexion

class BaseLocalIos : BaseLocal {
    override fun crearDriver(): SqlDriver = NativeSqliteDriver(PharmaDatabase.Schema, "pharmamobil.db")
}

/**
 * Pendiente (sesion 14): en iOS el token debe ir al Keychain. Mientras tanto se usa NSUserDefaults,
 * que NO es almacenamiento seguro; esta plataforma no se evalua en esta sesion.
 */
class AlmacenSesionIos : AlmacenSeguro {
    private val prefs = NSUserDefaults.standardUserDefaults

    override fun guardarSesion(sesion: Sesion) {
        prefs.setObject(sesion.token, "token")
        prefs.setObject(sesion.usuario, "usuario")
        prefs.setObject(sesion.nombre, "nombre")
        prefs.setObject(sesion.roles.joinToString(","), "roles")
        prefs.setObject(sesion.permisos.joinToString(","), "permisos")
        prefs.setObject(sesion.expiraEn.toString(), "expira")
    }

    override fun leerSesion(): Sesion? {
        val token = prefs.stringForKey("token") ?: return null
        return Sesion(
            token = token,
            usuario = prefs.stringForKey("usuario").orEmpty(),
            nombre = prefs.stringForKey("nombre").orEmpty(),
            roles = prefs.stringForKey("roles").orEmpty().split(",").filter { it.isNotBlank() },
            permisos = prefs.stringForKey("permisos").orEmpty().split(",").filter { it.isNotBlank() }.toSet(),
            expiraEn = prefs.stringForKey("expira")?.toLongOrNull() ?: 0,
        )
    }

    override fun leerToken(): String? = prefs.stringForKey("token")

    override fun borrar() {
        listOf("token", "usuario", "nombre", "roles", "permisos", "expira").forEach { prefs.removeObjectForKey(it) }
    }
}

/** Pendiente (sesion 14): observar la red con NWPathMonitor. Se asume conexion. */
class MonitorConexionIos : MonitorConexion {
    override val conectado: StateFlow<Boolean> = MutableStateFlow(true)
}
