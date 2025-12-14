package movil.proyect.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import movil.proyect.MainActivity
import movil.proyect.Modelos.Reporte
import movil.proyect.Modelos.Tarea
import movil.proyect.formularios.FormularioReporte
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReportesScreen(
    tarea: Tarea,
    onBack: () -> Unit,
    hideIt: Boolean = false
) {
    val scope = rememberCoroutineScope()

    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun cargar() {
        cargando = true
        reportes = cargarReportesDesdeServidor(tarea.id)
        cargando = false
    }

    LaunchedEffect(tarea.id) {
        try {
            cargar()
        } catch (e: Exception) {
            error = "Error al cargar reportes"
        }
    }

    // ---------- FORMULARIO ----------
    if (mostrarFormulario) {
        FormularioReporte(
            tarea = tarea,
            onClose = {
                mostrarFormulario = false
                scope.launch { cargar() }
            }
        )
    }

    // ---------- UI ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // CABECERA
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reportes · Fecha límite: ${tarea.fechaFin ?: "Sin fecha"}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (!hideIt) {
                Button(onClick = { mostrarFormulario = true }) {
                    Text("Nuevo reporte")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> CircularProgressIndicator()
            error != null -> Text(error!!, color = Color.Red)
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reportes, key = { it.id }) { reporte ->
                        ReporteCard(reporte = reporte)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Volver")
        }
    }
}

@Composable
private fun ReporteCard(reporte: Reporte) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                "Creación: ${reporte.fechacreacion?.format(fmt) ?: "—"}",
                fontWeight = FontWeight.Bold
            )

            Text("Usuario: ${reporte.nombreUsuario}")

            Text(
                "Estado: ${reporte.estado}",
                color = if (reporte.estado.lowercase() == "cerrado")
                    Color(0xFF22FF22) else Color(0xFFFFD700)
            )

            Spacer(Modifier.height(6.dp))

            Text(reporte.informacion ?: "")
        }
    }
}

// ==================== TCP ====================

suspend fun cargarReportesDesdeServidor(idTarea: Int): List<Reporte> =
    withContext(Dispatchers.IO) {

        val con = MainActivity.conexion ?: return@withContext emptyList()

        con.enviar("REPORTES${MainActivity.SEP}$idTarea")
        val respuesta = con.leerRespuestaCompleta()

        respuesta.split(MainActivity.JUMP)
            .filter { it.isNotBlank() }
            .mapNotNull {
                val c = it.split(MainActivity.SEP)
                try {
                    Reporte().apply {
                        id = c[0].toInt()
                        fechacreacion = LocalDate.parse(c[1])
                        informacion = c[2]
                        estado = c[3]
                        idUsuarioReporte = c[4].toInt()
                        nombreUsuario = c[5]
                    }
                } catch (_: Exception) {
                    null
                }
            }
    }
