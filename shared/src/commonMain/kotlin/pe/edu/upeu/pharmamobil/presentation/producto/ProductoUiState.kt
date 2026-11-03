package pe.edu.upeu.pharmamobil.presentation.producto

import pe.edu.upeu.pharmamobil.domain.model.Categoria

data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val categorias: List<Categoria> = emptyList(),
    val formulario: FormularioProducto = FormularioProducto(),
    val registrando: Boolean = false,
    val mensajeExito: String? = null,
    /** Se decide por permiso de la sesion (el servidor igual responde 403 si no lo tiene). */
    val puedeEscribir: Boolean = false,
    val sincronizando: Boolean = false,
    val conectado: Boolean = true,
    val ultimaSync: Long? = null,
) {

    /** Fases excluyentes del inventario: solo una puede estar activa. */
    sealed interface Fase {

        data object Cargando : Fase

        data object SinProductos : Fase

        data class ConProductos(val productos: List<ProductoUi>) : Fase
    }
}

data class FormularioProducto(
    /** null = registro nuevo; con valor = se esta corrigiendo ese producto. */
    val idEditando: Long? = null,
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val categoriaId: Long? = null,
    /** Mensaje por campo: validacion local o validationErrors del servidor (mismos nombres). */
    val errores: Map<String, String> = emptyMap(),
)
