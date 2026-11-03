package pe.edu.upeu.pharmamobil.di

import android.content.Context
import android.content.pm.ApplicationInfo
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobil.data.BaseLocalAndroid
import pe.edu.upeu.pharmamobil.data.MonitorConexionAndroid
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.BaseLocal
import pe.edu.upeu.pharmamobil.data.remote.ApiConfig
import pe.edu.upeu.pharmamobil.data.remote.ConfigPlataforma
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.port.NotificadorSistema
import pe.edu.upeu.pharmamobil.domain.repository.MonitorConexion
import pe.edu.upeu.pharmamobil.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobil.permiso.NotificadorAndroid

/** Dependencias propias de Android: motor HTTP, almacen seguro, base local, red, permisos y notificaciones. */
actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single {
        val depurable = (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        ConfigPlataforma(urlBase = ApiConfig.URL_BASE, depurar = depurable)
    }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single<BaseLocal> { BaseLocalAndroid(androidContext()) }
    single<MonitorConexion> { MonitorConexionAndroid(androidContext()) }
    single {
        GestorPermisosAndroid(androidContext(), androidContext().getSharedPreferences("pharma_permisos", Context.MODE_PRIVATE))
    } bind GestorPermisos::class
    single<NotificadorSistema> { NotificadorAndroid(androidContext()) }
}
