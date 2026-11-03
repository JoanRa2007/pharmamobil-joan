package pe.edu.upeu.pharmamobil.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobil.domain.repository.MonitorConexion

/** Observa la conectividad para sincronizar al recuperar la senal. */
class MonitorConexionAndroid(context: Context) : MonitorConexion {
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private val redes = mutableSetOf<Network>()
    private val _conectado = MutableStateFlow(hayRed())
    override val conectado: StateFlow<Boolean> = _conectado.asStateFlow()

    init {
        val pedido = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        cm.registerNetworkCallback(pedido, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                synchronized(redes) { redes += network }
                _conectado.value = true
            }

            override fun onLost(network: Network) {
                val quedan = synchronized(redes) { redes -= network; redes.isNotEmpty() }
                _conectado.value = quedan || hayRed()
            }
        })
    }

    private fun hayRed(): Boolean =
        cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
