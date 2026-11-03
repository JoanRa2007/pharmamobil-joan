package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.AlmacenSesionIos
import pe.edu.upeu.pharmamobil.data.BaseLocalIos
import pe.edu.upeu.pharmamobil.data.MonitorConexionIos
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.BaseLocal
import pe.edu.upeu.pharmamobil.data.remote.ConfigPlataforma
import pe.edu.upeu.pharmamobil.domain.repository.MonitorConexion

/** Dependencias de iOS. El simulador de iOS llega a la PC por localhost. */
actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single { ConfigPlataforma(urlBase = "http://localhost:8080/api/v1/") }
    single<AlmacenSeguro> { AlmacenSesionIos() }
    single<BaseLocal> { BaseLocalIos() }
    single<MonitorConexion> { MonitorConexionIos() }
}
