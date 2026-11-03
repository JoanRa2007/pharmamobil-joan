package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import pe.edu.upeu.pharmamobil.domain.model.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Permisos
import pe.edu.upeu.pharmamobil.domain.model.Resultado
import pe.edu.upeu.pharmamobil.domain.repository.MonitorConexion
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarCategoriasUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarUltimaSincronizacionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarUseCase
import pe.edu.upeu.pharmamobil.presentation.comun.Aviso
import pe.edu.upeu.pharmamobil.presentation.comun.Avisos

/**
 * Inventario offline-first: la pantalla observa la BASE LOCAL; la red solo se usa para
 * registrar, corregir, eliminar y sincronizar.
 */
class ProductoViewModel(
    observarProductos: ObservarProductosUseCase,
    observarCategorias: ObservarCategoriasUseCase,
    observarUltimaSincronizacion: ObservarUltimaSincronizacionUseCase,
    observarSesion: ObservarSesionUseCase,
    private val registrarProducto: RegistrarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val sincronizarUseCase: SincronizarUseCase,
    private val monitor: MonitorConexion,
    private val avisos: Avisos,
) : ViewModel() {

    private val formulario = MutableStateFlow(FormularioProducto())
    private val operacion = MutableStateFlow(Operacion())
    private val candado = Mutex()

    private data class Operacion(val registrando: Boolean = false, val sincronizando: Boolean = false, val exito: String? = null)

    val uiState: StateFlow<ProductoUiState> = combine(
        combine(observarProductos(), observarCategorias(), ::Pair),
        formulario,
        operacion,
        combine(observarSesion(), monitor.conectado, observarUltimaSincronizacion()) { s, red, ultima -> Triple(s, red, ultima) },
    ) { (productos, categorias), form, op, (sesion, red, ultima) ->
        ProductoUiState(
            fase = if (productos.isEmpty()) ProductoUiState.Fase.SinProductos
            else ProductoUiState.Fase.ConProductos(productos.map { it.aUi() }),
            categorias = categorias,
            formulario = form,
            registrando = op.registrando,
            mensajeExito = op.exito,
            puedeEscribir = sesion?.tiene(Permisos.CATALOGO_ESCRIBIR) == true,
            sincronizando = op.sincronizando,
            conectado = red,
            ultimaSync = ultima,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductoUiState())

    init {
        // Sincronizacion automatica al entrar con red y al recuperar la conexion.
        viewModelScope.launch { monitor.conectado.collect { if (it) sincronizar() } }
    }

    fun onNombreChange(v: String) = editar { it.copy(nombre = v, errores = it.errores - "nombre") }
    fun onPrecioChange(v: String) = editar { it.copy(precio = v, errores = it.errores - "precio") }
    fun onStockChange(v: String) = editar { it.copy(stock = v, errores = it.errores - "stock") }
    fun onCategoriaChange(id: Long) = editar { it.copy(categoriaId = id, errores = it.errores - "categoriaId") }

    private fun editar(cambio: (FormularioProducto) -> FormularioProducto) {
        formulario.update(cambio)
        operacion.update { it.copy(exito = null) }
    }

    fun corregir(producto: ProductoUi) {
        val p = producto.producto
        formulario.value = FormularioProducto(
            idEditando = p.id, nombre = p.nombre, precio = p.precio.toString(),
            stock = p.stock.toString(), categoriaId = p.categoriaId,
        )
    }

    fun cancelarEdicion() { formulario.value = FormularioProducto() }

    fun registrar() {
        if (operacion.value.registrando) return
        val f = formulario.value
        operacion.update { it.copy(registrando = true, exito = null) }
        viewModelScope.launch {
            val categoria = uiState.value.categorias.firstOrNull { it.id == f.categoriaId }
            when (val r = registrarProducto(f.idEditando, f.nombre, f.precio, f.stock, f.categoriaId, categoria?.nombre.orEmpty())) {
                is Resultado.Exito -> {
                    formulario.value = FormularioProducto()
                    val texto = if (r.dato.pendiente) "\"${r.dato.nombre}\" guardado sin conexión: se enviará al sincronizar"
                    else "Producto \"${r.dato.nombre}\" guardado en PharmaSoft"
                    operacion.update { it.copy(registrando = false, exito = texto) }
                }
                is Resultado.Fallo -> {
                    operacion.update { it.copy(registrando = false) }
                    when (val e = r.error) {
                        // 400: cada mensaje va bajo el campo que lo origino.
                        is ErrorApi.Validacion -> formulario.update { it.copy(errores = e.campos) }
                        else -> avisos.informar(e) { registrar() }
                    }
                }
            }
        }
    }

    fun eliminar(producto: ProductoUi) {
        viewModelScope.launch {
            when (val r = eliminarProducto(producto.id)) {
                is Resultado.Exito -> avisos.mostrar("\"${producto.nombre}\" eliminado")
                is Resultado.Fallo -> when (r.error) {
                    ErrorApi.SinConexion, ErrorApi.TiempoAgotado ->
                        avisos.mostrar("Eliminar un producto sincronizado requiere conexión")
                    is ErrorApi.NoEncontrado -> avisos.mostrar("El producto ya no existía; se actualizó la lista")
                    else -> avisos.informar(r.error) { eliminar(producto) }
                }
            }
        }
    }

    /** Primero sube lo pendiente y luego baja lo actualizado. */
    fun sincronizar() {
        if (!candado.tryLock()) return
        viewModelScope.launch {
            operacion.update { it.copy(sincronizando = true) }
            try {
                when (val r = sincronizarUseCase()) {
                    is Resultado.Exito -> avisos.mostrar(r.dato.texto())
                    is Resultado.Fallo -> when (val e = r.error) {
                        ErrorApi.SinConexion, ErrorApi.TiempoAgotado ->
                            avisos.mostrar("Sin conexión: se sincronizará al recuperar la señal")
                        else -> avisos.informar(e) { sincronizar() }
                    }
                }
            } finally {
                operacion.update { it.copy(sincronizando = false) }
                candado.unlock()
            }
        }
    }
}
