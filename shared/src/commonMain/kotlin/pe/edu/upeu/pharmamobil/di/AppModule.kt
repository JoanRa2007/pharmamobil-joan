package pe.edu.upeu.pharmamobil.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.firebase.TelemetriaFirebase
import pe.edu.upeu.pharmamobil.data.local.BaseLocal
import pe.edu.upeu.pharmamobil.data.local.EstadoSesion
import pe.edu.upeu.pharmamobil.data.local.abrir
import pe.edu.upeu.pharmamobil.data.remote.PharmaApi
import pe.edu.upeu.pharmamobil.data.remote.crearClienteHttp
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.EnvioRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ProductoLocalRepositoryImpl
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositoryImpl
import pe.edu.upeu.pharmamobil.data.repository.SesionRepositoryImpl
import pe.edu.upeu.pharmamobil.domain.port.Telemetria
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoLocalRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRemotoRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.repository.SesionRepository
import pe.edu.upeu.pharmamobil.domain.usecase.AvisarFallidasUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarCategoriasUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarFallidasUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarPendientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ReintentarFallidasUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarUltimaSincronizacionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RestaurarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.comun.Avisos
import pe.edu.upeu.pharmamobil.presentation.perfil.PerfilViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.presentation.sesion.LoginViewModel
import pe.edu.upeu.pharmamobil.presentation.sesion.RaizViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val CALIFICADOR_IO = named("io")
val CALIFICADOR_RELOJ = named("reloj")

/** Red: UNA sola instancia de HttpClient para toda la app. */
val redModule = module {
    single { EstadoSesion() }
    single { crearClienteHttp(motor = get(), almacen = get(), estadoSesion = get(), config = get()) }
    single { PharmaApi(get()) }
}

@OptIn(ExperimentalTime::class)
val dataModule = module {
    single(CALIFICADOR_IO) { Dispatchers.IO }
    single<() -> Long>(CALIFICADOR_RELOJ) { { Clock.System.now().toEpochMilliseconds() } }
    single { get<BaseLocal>().abrir() }
    single<SesionRepository> { SesionRepositoryImpl(get(), get(), get(), get(CALIFICADOR_RELOJ)) }
    single<ProductoRepository> { ProductoRepositoryImpl(get(), get(), get(CALIFICADOR_IO)) }
    single<ProductoLocalRepository> { ProductoLocalRepositoryImpl(get(), get(CALIFICADOR_IO)) }
    single<ProductoRemotoRepository> { EnvioRepositorioRest(get()) }
    single<Telemetria> { TelemetriaFirebase() }
    single<ClienteRepository> { ClienteRepositorioEnMemoria() }
}

val domainModule = module {
    factory { IniciarSesionUseCase(get()) }
    factory { RestaurarSesionUseCase(get()) }
    factory { ObservarSesionUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { ObservarProductosUseCase(get()) }
    factory { ObservarCategoriasUseCase(get()) }
    factory { ObservarUltimaSincronizacionUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    factory { ObservarPendientesUseCase(get()) }
    factory { ObservarFallidasUseCase(get()) }
    factory { ReintentarFallidasUseCase(get()) }
    factory { AvisarFallidasUseCase(get(), get()) }
    factory { SincronizarUseCase(get(), get(), get(), get(), get(CALIFICADOR_RELOJ)) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
}

val presentationModule = module {
    single { Avisos() }
    viewModel { RaizViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { PerfilViewModel(get(), get(), get()) }
    viewModel { ProductoViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ClienteViewModel(get(), get()) }
}


expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            redModule,
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}
