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
import movil.proyect.Modelos.Nomina
import movil.proyect.Modelos.Usuario
import movil.proyect.formularios.FormularioNominaScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun NominasDashboardScreen(
    usuario: Usuario,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var nominas by remember { mutableStateOf<List<Nomina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var nominaEditar by remember { mutableStateOf<Nomina?>(null) }

    suspend fun cargar() {
        cargando = true
        nominas = cargarNominasDesdeServidor(usuario.id)
        cargando = false
    }

    // ---------- CARGA ----------
    LaunchedEffect(usuario.id) {
        try {
            cargar()
        } catch (e: Exception) {
            error = "Error al cargar nóminas"
        }
    }

    // ---------- FORMULARIO ----------
    if (mostrarFormulario) {
        FormularioNominaScreen(
            usuario = usuario,
            nomina = nominaEditar,
            onClose = {
                mostrarFormulario = false
                nominaEditar = null
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

            TextButton(onClick = onBack) {
                Text("← Volver")
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Nóminas · ${usuario.nombre}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )

            Button(onClick = {
                nominaEditar = null
                mostrarFormulario = true
            }) {
                Text("Nueva nómina")
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> CircularProgressIndicator()
            error != null -> Text(error!!, color = Color.Red)
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = nominas,
                        key = { it.id }
                    ) { nomina ->
                        NominaCard(
                            nomina = nomina,
                            onEditar = {
                                nominaEditar = nomina
                                mostrarFormulario = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NominaCard(
    nomina: Nomina,
    onEditar: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = String.format("%.2f €", nomina.importe),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Fecha: ${nomina.fecha?.format(fmt) ?: "—"}"
            )

            Text("Concepto: ${nomina.concepto}")

            Text(
                text = "Tipo: ${nomina.tipo}",
                fontWeight = FontWeight.Bold,
                color = when {
                    nomina.esSalario() -> Color(0xFF22FF22)
                    nomina.esDeduccion() -> Color(0xFFFF4444)
                    else -> Color(0xFFFFD700)
                }
            )

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onEditar) {
                Text("Editar")
            }
        }
    }
}

// ==================== TCP ====================

private suspend fun cargarNominasDesdeServidor(
    idUsuario: Int
): List<Nomina> =
    withContext(Dispatchers.IO) {

        val con = MainActivity.conexion ?: return@withContext emptyList()

        con.enviar("NOMINAS_USUARIO${MainActivity.SEP}$idUsuario")
        val respuesta = con.leerRespuestaCompleta()

        respuesta.split(MainActivity.JUMP)
            .filter { it.isNotBlank() }
            .mapNotNull {
                val c = it.split(MainActivity.SEP)
                try {
                    Nomina(
                        id = c[0].trim().toInt(),
                        importe = c[1].trim().toDouble(),
                        fecha = c[2].takeIf { it.isNotBlank() }?.let {
                            LocalDate.parse(it)
                        },
                        concepto = c[3],
                        tipo = c[4],
                        idUsuario = c[5].trim().toInt()
                    )
                } catch (_: Exception) {
                    null
                }
            }
    }
