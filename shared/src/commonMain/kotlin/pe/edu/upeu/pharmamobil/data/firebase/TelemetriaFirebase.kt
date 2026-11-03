package pe.edu.upeu.pharmamobil.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobil.domain.port.Telemetria

// commonMain/data/firebase/TelemetriaFirebase.kt
/**
 * Unico lugar donde aparece el SDK de Firebase (GitLive, Crashlytics).
 * Sin google-services.json Firebase no se inicializa y la telemetria se omite sin afectar a la app.
 */
class TelemetriaFirebase : Telemetria {

    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        runCatching {
            val crashlytics = Firebase.crashlytics
            contexto.forEach { (clave, valor) -> crashlytics.setCustomKey(clave, valor) }
            crashlytics.recordException(e)
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        runCatching {
            // Crashlytics no tiene eventos propios: quedan como migas en el registro del proximo informe.
            val detalle = datos.entries.joinToString(", ") { "${it.key}=${it.value}" }
            Firebase.crashlytics.log("$nombre {$detalle}")
        }
    }
}
