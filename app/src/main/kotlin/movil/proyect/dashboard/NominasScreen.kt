package movil.proyect.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
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
import movil.proyect.Modelos.Nomina
import movil.proyect.Modelos.Usuario
import java.time.format.DateTimeFormatter

@Composable
fun NominasScreen(
    usuario: Usuario,
    onNuevaNomina: () -> Unit = {},
    onEditarNomina: (Nomina) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var nominas by remember { mutableStateOf<List<Nomina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val conexion = MainActivity.conexion

    LaunchedEffect(usuario.id) {
        scope.launch(Dispatchers.IO) {
            try {
                if (conexion == null) {
                    error = "Sin conexión"
                    cargando = false
                    return@launch
                }

                conexion.enviar(
                    "NOMINAS_USUARIO${MainActivity.SEP}${usuario.id}"
                )

                val respuesta = conexion.leerRespuestaCompleta()

                val lista = respuesta
                    .split(MainActivity.JUMP)
                    .filter { it.isNotBlank() }
                    .mapNotNull { linea ->
                        val c = linea.split(MainActivity.SEP)
                        if (c.size < 6) return@mapNotNull null

                        try {
                            Nomina(
                                id = c[0].toInt(),
                                importe = c[1].toDouble(),
                                fecha = c[2].takeIf { it.isNotBlank() }
                                    ?.let { java.time.LocalDate.parse(it) },
                                concepto = c[3],
                                tipo = c[4],
                                idUsuario = c[5].toInt()
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                nominas = lista
                cargando = false

            } catch (e: Exception) {
                e.printStackTrace()
                error = "Error cargando nóminas"
                cargando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔝 BARRA SUPERIOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nóminas",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onNuevaNomina) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva nómina"
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(error!!, color = Color.Red)
            }

            nominas.isEmpty() -> {
                Text("No hay nóminas registradas")
            }

            else -> {
                LazyColumn {
                    items(nominas) { nomina ->
                        NominaRow(
                            nomina = nomina,
                            onEditar = { onEditarNomina(nomina) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun NominaRow(
    nomina: Nomina,
    onEditar: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111))
            .padding(12.dp)
    ) {
        Text(
            text = String.format("%.2f €", nomina.importe),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Fecha: ${nomina.fecha?.format(fmt) ?: "—"}"
        )

        Text("Concepto: ${nomina.concepto}")
        Text("Tipo: ${nomina.tipo}")

        Spacer(Modifier.height(8.dp))

        IconButton(onClick = onEditar) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar nómina"
            )
        }
    }
}
