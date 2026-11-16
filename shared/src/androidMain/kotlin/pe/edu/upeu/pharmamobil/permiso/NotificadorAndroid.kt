package pe.edu.upeu.pharmamobil.permiso

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import pe.edu.upeu.pharmamobil.domain.port.NotificadorSistema

/** Notificacion «N productos no se pudieron enviar» en el canal de sincronizacion. */
class NotificadorAndroid(private val contexto: Context) : NotificadorSistema {

    init {
        val canal = NotificationChannel(CANAL, "Sincronización", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Avisos de productos que no se pudieron enviar a PharmaSoft" }
        contexto.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    override fun notificarFallidas(cantidad: Int) {
        val permitido = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!permitido) return
        val abrir = contexto.packageManager.getLaunchIntentForPackage(contexto.packageName)?.let {
            PendingIntent.getActivity(contexto, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val texto = if (cantidad == 1) "1 producto no se pudo enviar" else "$cantidad productos no se pudieron enviar"
        val notificacion = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("PharmaMobil")
            .setContentText("$texto. Toca para revisar el motivo.")
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(contexto).notify(ID, notificacion)
    }

    private companion object {
        const val CANAL = "sincronizacion"
        const val ID = 1301
    }
}
