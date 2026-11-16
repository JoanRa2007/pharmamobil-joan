package pe.edu.upeu.pharmamobil.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.domain.model.Sesion

/**
 * Sesion en preferencias CIFRADAS (AES-256); la clave maestra vive en el Android Keystore.
 * En preferencias sin cifrar, quien acceda al archivo (root, respaldo) leeria el token.
 */
class AlmacenSeguroAndroid(context: Context) : AlmacenSeguro {

    @Suppress("DEPRECATION")
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "pharma_sesion_segura",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun guardarSesion(sesion: Sesion) = prefs.edit {
        putString(TOKEN, sesion.token)
        putString(USUARIO, sesion.usuario)
        putString(NOMBRE, sesion.nombre)
        putStringSet(ROLES, sesion.roles.toSet())
        putStringSet(PERMISOS, sesion.permisos)
        putLong(EXPIRA, sesion.expiraEn)
    }

    override fun leerSesion(): Sesion? {
        val token = prefs.getString(TOKEN, null) ?: return null
        return Sesion(
            token = token,
            usuario = prefs.getString(USUARIO, "").orEmpty(),
            nombre = prefs.getString(NOMBRE, "").orEmpty(),
            roles = prefs.getStringSet(ROLES, emptySet()).orEmpty().toList(),
            permisos = prefs.getStringSet(PERMISOS, emptySet()).orEmpty().toSet(),
            expiraEn = prefs.getLong(EXPIRA, 0),
        )
    }

    override fun leerToken(): String? = prefs.getString(TOKEN, null)

    override fun borrar() = prefs.edit { clear() }

    private companion object {
        const val TOKEN = "token"
        const val USUARIO = "usuario"
        const val NOMBRE = "nombre"
        const val ROLES = "roles"
        const val PERMISOS = "permisos"
        const val EXPIRA = "expira_en"
    }
}
