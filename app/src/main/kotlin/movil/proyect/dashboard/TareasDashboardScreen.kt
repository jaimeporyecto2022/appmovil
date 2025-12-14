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
import movil.proyect.Modelos.Tarea
import movil.proyect.formularios.FormularioTareaScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@Composable
fun TareasDashboardScreen(
    onBack: () -> Unit
) {
    val usuario = MainActivity.usuarioActual

    if (usuario == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No hay usuario activo", color = Color.Red)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Volver") }
        }
        return
    }

    val scope = rememberCoroutineScope()
    val tareas = remember { mutableStateListOf<Tarea>() }

    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var tareaEditar by remember { mutableStateOf<Tarea?>(null) }

    // 🔥 NUEVO
    var verReportes by remember { mutableStateOf(false) }
    var tareaReportes by remember { mutableStateOf<Tarea?>(null) }

    // ================= CARGA =================
    LaunchedEffect(usuario.id) {
        try {
            cargando = true
            tareas.clear()
            tareas.addAll(cargarTareasAsignadasDesdeServidor())
        } catch (e: Exception) {
            error = "Error al cargar tareas"
        } finally {
            cargando = false
        }
    }

    // ================= FORMULARIO =================
    if (mostrarFormulario) {
        FormularioTareaScreen(
            tarea = tareaEditar,
            onClose = {
                mostrarFormulario = false
                tareaEditar = null
                scope.launch {
                    tareas.clear()
                    tareas.addAll(cargarTareasAsignadasDesdeServidor())
                }
            }
        )
    }

    // ================= REPORTES =================
    if (verReportes && tareaReportes != null) {
        ReportesScreen(
            tarea = tareaReportes!!,
            hideIt = false,
            onBack = {
                verReportes = false
                tareaReportes = null
            }
        )
        return
    }

    // ================= UI =================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Asignar tareas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )

            Button(onClick = {
                tareaEditar = null
                mostrarFormulario = true
            }) {
                Text("Nueva tarea")
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> CircularProgressIndicator()
            error != null -> Text(error!!, color = Color.Red)
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tareas, key = { it.id }) { tarea ->
                        TareaAsignadaCard(
                            tarea = tarea,
                            onEditar = {
                                tareaEditar = tarea
                                mostrarFormulario = true
                            },
                            onVerReportes = {
                                tareaReportes = tarea
                                verReportes = true
                            }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun TareaAsignadaCard(
    tarea: Tarea,
    onEditar: () -> Unit,
    onVerReportes: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = tarea.titulo,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Asignado a: ${tarea.nombreAsignado}",
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Estado: ${tarea.estado}",
                color = when (tarea.estado?.lowercase()) {
                    "completado" -> Color(0xFF00FF88)
                    "pendiente" -> Color(0xFFFFD700)
                    "imposible", "no puedo hacerlo" -> Color(0xFFFF4444)
                    else -> Color.Gray
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = buildString {
                    tarea.fechaInicio?.let { append("Inicio: ${it.format(fmt)} ") }
                    tarea.fechaFin?.let { append("Fin: ${it.format(fmt)}") }
                }.ifBlank { "Sin fechas" },
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onVerReportes) {
                    Text("Reportes")
                }
            }
        }
    }
}

// ---------- SERVIDOR ----------
suspend fun cargarTareasAsignadasDesdeServidor(): List<Tarea> =
    withContext(Dispatchers.IO) {

        val con = MainActivity.conexion
            ?: throw IllegalStateException("Sin conexión")

        val usuario = MainActivity.usuarioActual
            ?: throw IllegalStateException("Sin usuario")

        con.enviar("MIS_TAREAS_CREADAS${MainActivity.SEP}${usuario.id}")

        val respuesta = con.leerRespuestaCompleta()
        val lista = mutableListOf<Tarea>()

        respuesta.split(MainActivity.JUMP).forEach { linea ->
            if (linea.isBlank()) return@forEach

            val c = linea.split(MainActivity.SEP)
            if (c.size < 10) return@forEach

            try {
                lista.add(
                    Tarea(
                        id = c[0].trim().toInt(),
                        titulo = c[1].trim(),
                        descripcion = c[2].trim(),
                        fechaCreacion = parseFecha2(c[3]),
                        fechaInicio = parseFecha2(c[4]),
                        fechaFin = parseFecha2(c[5]),
                        estado = c[6].trim(),
                        nombreCreador = c[7].trim(),
                        nombreAsignado = c[8].trim(),
                        idAsignado = c[9].trim().toInt()
                    )
                )
            } catch (_: Exception) {}
        }

        lista
    }

private fun parseFecha2(valor: String): LocalDate? =
    valor.trim().takeIf { it.isNotBlank() && it != "null" }?.let {
        LocalDate.parse(it)
    }
