package pe.edu.upeu.pharmamobil.data.remote

/** Direccion base centralizada y tiempos de espera del cliente HTTP. */
object ApiConfig {
    /** 10.0.2.2 = la PC vista desde el emulador. En celular por USB: "http://localhost:8080/api/v1/" + adb reverse. */
    const val URL_BASE = "http://10.0.2.2:8080/api/v1/"
    const val TIEMPO_CONEXION_MS = 10_000L
    const val TIEMPO_PETICION_MS = 20_000L
    const val TAMANIO_PAGINA = 50
}

data class ConfigPlataforma(
    val urlBase: String = ApiConfig.URL_BASE,
    val depurar: Boolean = false,
)
