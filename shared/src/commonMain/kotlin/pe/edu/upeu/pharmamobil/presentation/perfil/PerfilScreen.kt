package pe.edu.upeu.pharmamobil.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso

@Composable
fun PerfilScreen(modifier: Modifier = Modifier, vm: PerfilViewModel = koinViewModel()) {
    val sesion by vm.sesion.collectAsStateWithLifecycle()
    val notificaciones by vm.notificaciones.collectAsStateWithLifecycle()
    // Al entrar y al volver de Ajustes se consulta el estado real del permiso.
    LifecycleResumeEffect(Unit) {
        vm.refrescarPermiso()
        onPauseOrDispose { }
    }
    val s = sesion ?: return
    Column(
        modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.nombre, style = MaterialTheme.typography.titleLarge)
                Text("Usuario: ${s.usuario}")
                Text("Roles: ${s.roles.joinToString()}", color = MaterialTheme.colorScheme.primary)
            }
        }
        Text("Permisos", style = MaterialTheme.typography.titleMedium)
        s.permisos.sorted().forEach { p ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(p)
            }
        }
        SeccionNotificaciones(
            estado = notificaciones,
            alActivar = vm::activarNotificaciones,
            alAbrirAjustes = vm::abrirAjustes,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = vm::cerrarSesion,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

/** La interfaz EXPLICA para que se pide el permiso antes de pedirlo, y ofrece Ajustes cuando ya no se puede pedir. */
@Composable
private fun SeccionNotificaciones(estado: EstadoPermiso?, alActivar: () -> Unit, alAbrirAjustes: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "PharmaMobil te avisa cuando un producto registrado sin señal no se pudo enviar a PharmaSoft, " +
                    "para que lo corrijas antes de perderlo.",
                style = MaterialTheme.typography.bodyMedium,
            )
            when (estado) {
                null -> Unit
                EstadoPermiso.Concedido -> Text("Activadas", color = MaterialTheme.colorScheme.primary)
                EstadoPermiso.Denegado -> Button(onClick = alActivar) { Text("Activar notificaciones") }
                EstadoPermiso.DenegadoParaSiempre -> {
                    Text(
                        "Las desactivaste. Solo puedes activarlas desde los Ajustes del teléfono.",
                        color = MaterialTheme.colorScheme.error,
                    )
                    OutlinedButton(onClick = alAbrirAjustes) { Text("Abrir Ajustes") }
                }
            }
        }
    }
}
