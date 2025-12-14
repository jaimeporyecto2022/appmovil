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
import movil.proyect.Modelos.Usuario
import movil.proyect.formularios.FormularioUsuarioScreen

@Composable
fun UsuariosScreen() {

    val scope = rememberCoroutineScope()

    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // navegación interna
    var mostrarFormulario by remember { mutableStateOf(false) }
    var usuarioEditar by remember { mutableStateOf<Usuario?>(null) }

    var verNominas by remember { mutableStateOf(false) }
    var usuarioNominas by remember { mutableStateOf<Usuario?>(null) }

    // ---------- NÓMINAS ----------
    if (verNominas && usuarioNominas != null) {
        NominasDashboardScreen(
            usuario = usuarioNominas!!,
            onBack = {
                verNominas = false
                usuarioNominas = null
            }
        )
        return
    }

    // ---------- FORMULARIO ----------
    if (mostrarFormulario) {
        FormularioUsuarioScreen(
            usuario = usuarioEditar,
            onClose = {
                mostrarFormulario = false
                usuarioEditar = null
                scope.launch { cargarUsuarios { usuarios = it } }
            }
        )
    }

    // ---------- CARGA ----------
    LaunchedEffect(Unit) {
        try {
            cargando = true
            cargarUsuarios { usuarios = it }
        } catch (e: Exception) {
            error = "Error al cargar usuarios"
        } finally {
            cargando = false
        }
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
                text = "Usuarios",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )

            Button(onClick = {
                usuarioEditar = null
                mostrarFormulario = true
            }) {
                Text("Nuevo usuario")
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
                        items = usuarios,
                        key = { it.id }
                    ) { usuario ->
                        UsuarioCard(
                            usuario = usuario,
                            onEditar = {
                                usuarioEditar = usuario
                                mostrarFormulario = true
                            },
                            onVerNominas = {
                                usuarioNominas = usuario
                                verNominas = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsuarioCard(
    usuario: Usuario,
    onEditar: () -> Unit,
    onVerNominas: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                usuario.nombre,
                fontWeight = FontWeight.Bold
            )

            Text("Mail: ${usuario.mail}")
            Text("Rol: ${usuario.rol}")
            Text("Departamento: ${usuario.nombreDepartamento}")

            Spacer(Modifier.height(8.dp))

            Row {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onVerNominas) {
                    Text("Nóminas")
                }
            }
        }
    }
}

// ==================== TCP ====================

private suspend fun cargarUsuarios(onResult: (List<Usuario>) -> Unit) =
    withContext(Dispatchers.IO) {

        val con = MainActivity.conexion ?: return@withContext
        con.enviar("USUARIOS")

        val respuesta = con.leerRespuestaCompleta()

        val lista = respuesta
            .split(MainActivity.JUMP)
            .filter { it.isNotBlank() }
            .mapNotNull {
                val c = it.split(MainActivity.SEP)
                try {
                    Usuario(
                        id = c[0].trim().toInt(),
                        nombre = c[1],
                        mail = c[2],
                        rol = c[3],
                        nombreDepartamento = c.getOrNull(4) ?: ""
                    )
                } catch (_: Exception) {
                    null
                }
            }

        onResult(lista)
    }
