package pe.edu.upeu.pharmamobil.presentation.sincronizacion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Estado de la sincronizacion; si hay filas fallidas ofrece una accion: reintentar ahora. */
@OptIn(ExperimentalTime::class)
@Composable
fun BarraSincronizacion(
    estado: SincronizacionUiState,
    onSincronizar: () -> Unit,
    onReintentarFallidas: () -> Unit,
) {
    // Se refresca cada minuto para que «hace N min» no quede congelado.
    val ahora by produceState(Clock.System.now().toEpochMilliseconds()) {
        while (true) {
            delay(60_000)
            value = Clock.System.now().toEpochMilliseconds()
        }
    }
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (estado.conectado) Icons.Default.CloudDone else Icons.Default.CloudOff, null,
                tint = if (estado.conectado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(estado.resumen(), style = MaterialTheme.typography.bodyMedium)
                Text(
                    estado.antiguedad(ahora),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (estado.enCurso) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onSincronizar) { Icon(Icons.Default.Sync, "Sincronizar") }
            }
        }
        if (estado.fallidas > 0) {
            Card(
                Modifier.fillMaxWidth().padding(top = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Row(Modifier.padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (estado.fallidas == 1) "1 producto no se pudo enviar. Revisa el motivo en la lista."
                        else "${estado.fallidas} productos no se pudieron enviar. Revisa el motivo en la lista.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onReintentarFallidas, enabled = estado.conectado && !estado.enCurso) {
                        Text("Reintentar ahora")
                    }
                }
            }
        }
    }
}
