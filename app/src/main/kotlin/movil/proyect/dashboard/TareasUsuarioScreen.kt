package movil.proyect.dashboard

import androidx.compose.foundation.background
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@Composable//esta clase es para ver las tareas al usuario en sesion
fun TareasUsuarioDashboardScreen() {

    val scope = rememberCoroutineScope()
    var tareas by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                cargando = true
                tareas = cargarTareasDesdeServidor()
            } catch (e: Exception) {
                error = "Error al cargar tareas"
            } finally {
                cargando = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Tareas: ${MainActivity.usuarioActual?.nombre ?: ""}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            cargando -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Text(error!!, color = Color.Red)
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tareas) { tarea ->
                        TareaCard(tarea)
                    }
                }
            }
        }
    }
}
@Composable
fun TareaCard(tarea: Tarea) {

    val colorEstado = when (tarea.estado?.lowercase()) {
        "completado" -> Color(0xFF1A4D1A)
        "pendiente" -> Color(0xFF3D3D00)
        "no puedo hacerlo", "imposible" -> Color(0xFF660000)
        else -> Color(0xFF222222)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorEstado)
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatearFechas(tarea),
                color = Color.LightGray,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tarea.estado?.uppercase() ?: "",
                fontWeight = FontWeight.Bold,
                color = when (tarea.estado?.lowercase()) {
                    "completado" -> Color(0xFF00FF88)
                    "pendiente" -> Color(0xFFFFD700)
                    "no puedo hacerlo", "imposible" -> Color(0xFFFF4444)
                    else -> Color.Gray
                }
            )
        }
    }
}
suspend fun cargarTareasDesdeServidor(): List<Tarea> =
    withContext(Dispatchers.IO) {

        val conexion = MainActivity.conexion
            ?: throw IllegalStateException("Sin conexión")

        conexion.enviar(
            "MIS_TAREAS${MainActivity.SEP}${MainActivity.usuarioActual!!.id}"
        )

        val respuesta = conexion.leerRespuestaCompleta()
        val tareas = mutableListOf<Tarea>()

        val lineas = respuesta.split(MainActivity.JUMP)

        for (linea in lineas) {
            if (linea.isBlank() || linea == "FIN_COMANDO") continue

            val c = linea.split(MainActivity.SEP)
            if (c.size < 6) continue

            val tarea = Tarea().apply {
                id = c[0].toInt()
                titulo = c[1]
                descripcion = c[2]
                fechaInicio = parseFecha(c[3])
                fechaFin = parseFecha(c[4])
                estado = c[5]
                nombreAsignado = c.getOrNull(6) ?: ""
            }

            tareas.add(tarea)
        }

        tareas
    }
fun parseFecha(valor: String): LocalDate? =
    if (valor.isBlank() || valor == "Sin fecha") null
    else LocalDate.parse(valor)

fun formatearFechas(t: Tarea): String {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yy")
    val sb = StringBuilder()

    t.fechaInicio?.let { sb.append("Inicio: ${it.format(fmt)} ") }
    t.fechaFin?.let { sb.append("Fin: ${it.format(fmt)}") }

    return sb.toString().ifBlank { "Sin fechas" }
}

