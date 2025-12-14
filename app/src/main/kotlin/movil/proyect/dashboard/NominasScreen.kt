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
    usuario: Usuario
) {
    val scope = rememberCoroutineScope()

    var nominas by remember { mutableStateOf<List<Nomina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var nominaEditar by remember { mutableStateOf<Nomina?>(null) }

    // 🔄 CARGA INICIAL
    LaunchedEffect(usuario.id) {
        scope.launch {
            try {
                cargando = true
                nominas = cargarNominasDesdeServidor(usuario.id)
            } catch (e: Exception) {
                error = "Error al cargar nóminas"
            } finally {
                cargando = false
            }
        }
    }

    // 🧾 FORMULARIO
    if (mostrarFormulario) {
        FormularioNominaScreen(
            usuario = usuario,
            nomina = nominaEditar,
            onClose = {
                mostrarFormulario = false
                nominaEditar = null
                scope.launch {
                    nominas = cargarNominasDesdeServidor(usuario.id)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ---------- CABECERA ----------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Nóminas",
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

        Spacer(modifier = Modifier.height(16.dp))

        // ---------- CONTENIDO ----------
        when {
            cargando -> CircularProgressIndicator()
            error != null -> Text(error!!, color = Color.Red)
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(nominas) { nomina ->
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
fun NominaCard(
    nomina: Nomina,
    onEditar: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = String.format("%.2f €", nomina.importe),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Fecha: ${nomina.fecha?.format(fmt) ?: "—"}",
                color = Color.LightGray
            )

            Text(
                text = "Concepto: ${nomina.concepto}",
                color = Color.LightGray
            )

            Text(
                text = "Tipo: ${nomina.tipo}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onEditar) {
                Text("Editar")
            }
        }
    }
}
suspend fun cargarNominasDesdeServidor(
    usuarioId: Int
): List<Nomina> = withContext(Dispatchers.IO) {

    val con = MainActivity.conexion
        ?: throw IllegalStateException("Sin conexión")

    con.enviar("NOMINAS_USUARIO${MainActivity.SEP}$usuarioId")

    val respuesta = con.leerRespuestaCompleta()
    val lista = mutableListOf<Nomina>()

    val lineas = respuesta.split(MainActivity.JUMP)

    for (linea in lineas) {
        if (linea.isBlank()) continue

        val c = linea.split(MainActivity.SEP)
        if (c.size < 6) continue

        try {
            val n = Nomina().apply {
                id = c[0].toInt()
                importe = c[1].toDouble()
                fecha = if (c[2].isBlank()) null else LocalDate.parse(c[2])
                concepto = c[3]
                tipo = c[4]
                idUsuario = c[5].toInt()
            }
            lista.add(n)
        } catch (_: Exception) { }
    }

    lista
}

