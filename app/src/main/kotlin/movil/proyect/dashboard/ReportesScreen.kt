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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import movil.proyect.MainActivity
import movil.proyect.Modelos.Reporte
import movil.proyect.Modelos.Tarea
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReportesScreen(
    tarea: Tarea,
    hideIt: Boolean
) {
    val scope = rememberCoroutineScope()

    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tarea.id) {
        scope.launch {
            try {
                cargando = true
                reportes = cargarReportesDesdeServidor(tarea.id)
            } catch (e: Exception) {
                error = "Error al cargar reportes"
            } finally {
                cargando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ---------------- CABECERA ----------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Reportes · Fecha límite: ${tarea.fechaFin ?: "Sin fecha"}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )

            if (!hideIt) {
                Button(onClick = {
                    // AQUÍ ABRIRÍAS FormularioReporte (otra pantalla)
                }) {
                    Text("Nuevo reporte")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------- CONTENIDO ----------------
        when {
            cargando -> CircularProgressIndicator()
            error != null -> Text(error!!, color = Color.Red)
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reportes) { reporte ->
                        val scope = rememberCoroutineScope()
                        ReporteCard(
                            reporte = reporte,
                            onEditar = { /* ... */ },
                            onCerrar = {
                                cerrarReporte(reporte, scope)
                                scope.launch {
                                    reportes = cargarReportesDesdeServidor(tarea.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
//equivale a una fila del TableView de kotlin
@Composable
fun ReporteCard(
    reporte: Reporte,
    onEditar: () -> Unit,
    onCerrar: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = "Creación: ${
                    reporte.fechacreacion?.format(fmt) ?: "Sin fecha"
                }",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Usuario: ${reporte.nombreUsuario}",
                color = Color.LightGray
            )

            Text(
                text = "Estado: ${reporte.estado}",
                color = when (reporte.estado.lowercase()) {
                    "cerrado" -> Color(0xFF22FF22)
                    else -> Color(0xFFFFD700)
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = reporte.informacion?.let {
                    if (it.length > 20) it.substring(0, 20) + "..."
                    else it
                } ?: "",
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onCerrar) {
                    Text("Cerrar")
                }
            }
        }
    }
}
//Carga desde servidor
suspend fun cargarReportesDesdeServidor(idTarea: Int): List<Reporte> =
    withContext(Dispatchers.IO) {

        val con = MainActivity.conexion
            ?: throw IllegalStateException("Sin conexión")

        con.enviar("REPORTES${MainActivity.SEP}$idTarea")

        val respuesta = con.leerRespuestaCompleta()
        val lista = mutableListOf<Reporte>()

        val lineas = respuesta.split(MainActivity.JUMP)

        for (linea in lineas) {
            if (linea.isBlank()) continue

            val campos = linea.split(MainActivity.SEP)
            try {
                val r = Reporte().apply {
                    id = campos[0].toInt()
                    fechacreacion = LocalDate.parse(campos[1])
                    informacion = campos[2]
                    estado = campos[3]
                    idUsuarioReporte = campos[4].toInt()
                    nombreUsuario = campos[5]
                }
                lista.add(r)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lista
    }
//Cerrar reporte
fun cerrarReporte(reporte: Reporte, scope: CoroutineScope) {
    scope.launch(Dispatchers.IO) {
        try {
            val con = MainActivity.conexion ?: return@launch

            con.enviar("CERRAR_REPORTE${MainActivity.SEP}${reporte.id}")
            con.leerRespuestaCompleta()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}