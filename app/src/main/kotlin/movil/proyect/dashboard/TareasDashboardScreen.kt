package movil.proyect.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import movil.proyect.MainActivity
import movil.proyect.Modelos.Tarea
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TareasDashboardScreen(
    onEditarTarea: (Tarea) -> Unit,
    onVerReportes: (Tarea) -> Unit,
    onNuevaTarea: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var tareas by remember { mutableStateOf<List<Tarea>>(emptyList()) }

    val usuario = MainActivity.usuarioActual ?: return

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val con = MainActivity.conexion ?: return@launch
                con.enviar("MIS_TAREAS_CREADAS${MainActivity.SEP}${usuario.id}")
                val respuesta = con.leerRespuestaCompleta()

                val lista = respuesta.split(MainActivity.JUMP)
                    .filter { it.isNotBlank() }
                    .mapNotNull { linea ->
                        val c = linea.split(MainActivity.SEP)
                        if (c.size < 10) return@mapNotNull null

                        try {
                            Tarea(
                                id = c[0].toInt(),
                                titulo = c[1].ifBlank { "Sin título" },
                                descripcion = c[2],
                                fechaCreacion = c[3].takeIf { it != "null" }?.let { LocalDate.parse(it) },
                                fechaInicio = c[4].takeIf { it != "null" }?.let { LocalDate.parse(it) },
                                fechaFin = c[5].takeIf { it != "null" }?.let { LocalDate.parse(it) },
                                estado = c[6],
                                nombreCreador = c[7],
                                nombreAsignado = c[8],
                                idAsignado = c[9].toInt()
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                tareas = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // 🔝 BARRA SUPERIOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Asignar Tareas",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Button(onClick = onNuevaTarea) {
                Icon(Icons.Default.Assignment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tarea")
            }
        }

        Spacer(Modifier.height(16.dp))

        // 📋 LISTADO
        LazyColumn {
            items(tareas) { tarea ->
                TareaRow(
                    tarea = tarea,
                    onEditar = { onEditarTarea(tarea) },
                    onReportes = { onVerReportes(tarea) }
                )
                Divider()
            }
        }
    }
}

@Composable
private fun TareaRow(
    tarea: Tarea,
    onEditar: () -> Unit,
    onReportes: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yy")

    val colorEstado = when (tarea.estado.lowercase()) {
        "completado" -> Color(0xFF00FF88)
        "pendiente" -> Color(0xFFFFD700)
        "no puedo hacerlo", "imposible" -> Color(0xFFFF4444)
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color(0xFF111111)),
    ) {

        Text(tarea.titulo, fontWeight = FontWeight.Bold)
        Text("Asignado a: ${tarea.nombreAsignado}")
        Text(
            "Estado: ${tarea.estado.uppercase()}",
            color = colorEstado,
            fontWeight = FontWeight.Bold
        )

        Text(
            buildString {
                tarea.fechaCreacion?.let { append("Creación: ${it.format(fmt)}\n") }
                tarea.fechaInicio?.let { append("Desde: ${it.format(fmt)} ") }
                tarea.fechaFin?.let { append("Hasta: ${it.format(fmt)}") }
            },
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(8.dp))

        Row {
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onReportes) {
                Icon(Icons.Default.Search, contentDescription = "Reportes")
            }
        }
    }
}
