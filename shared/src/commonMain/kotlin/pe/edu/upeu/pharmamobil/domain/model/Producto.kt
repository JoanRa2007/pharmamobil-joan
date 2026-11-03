package pe.edu.upeu.pharmamobil.domain.model

data class Producto(
    val id:Long,
    val nombre:String,
    val precio:Double,
    val stock: Int,
    // Unidad 2 (PharmaSoft): categoria, estado y marca de envio offline-first
    val categoriaId: Long = 0,
    val categoriaNombre: String = "",
    val estado: Boolean = true,
    /** true mientras el cambio no haya llegado al servidor (id negativo si es una creacion). */
    val pendiente: Boolean = false,
) {


    init {
        require(nombre.isNotBlank()) {
            "El nombre del producto no puede estar vacio"
        }
        require(precio > 0 && precio.isFinite()) {
            "El precio debe ser un numero mayor que cero"
        }
        require(stock >= 0) {
            "El stock no puede ser negativo"
        }
    }

    /**
     * Un producto necesita reposicion cuando su stock cae por debajo del
     * minimo que la botica mantiene en gondola.
     */
    val requiereReposicion: Boolean
        get() = stock < STOCK_MINIMO

    companion object {
        const val STOCK_MINIMO = 10
    }
}
