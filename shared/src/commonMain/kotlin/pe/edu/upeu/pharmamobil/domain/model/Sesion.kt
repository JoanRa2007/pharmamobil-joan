package pe.edu.upeu.pharmamobil.domain.model

/** Sesion del usuario autenticado contra PharmaSoft. */
data class Sesion(
    val token: String,
    val usuario: String,
    val nombre: String,
    val roles: List<String>,
    val permisos: Set<String>,
    /** Vencimiento del token (epoch ms). */
    val expiraEn: Long,
) {
    fun tiene(permiso: String): Boolean = permiso in permisos
}

/** Permisos de PharmaSoft (enum Permiso del backend). La UI decide por permiso, nunca por rol. */
object Permisos {
    const val CATALOGO_LEER = "CATALOGO_LEER"
    const val CATALOGO_ESCRIBIR = "CATALOGO_ESCRIBIR"
}
