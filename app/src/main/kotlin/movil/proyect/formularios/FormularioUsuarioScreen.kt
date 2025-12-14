package movil.proyect.formularios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import movil.proyect.MainActivity
import movil.proyect.Modelos.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioUsuarioScreen(
    usuario: Usuario? = null,          // null = crear | != null = editar
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val esUpdate = usuario != null

    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var mail by remember { mutableStateOf(usuario?.mail ?: "") }
    var password by remember { mutableStateOf("") }

    var rol by remember { mutableStateOf(usuario?.rol ?: "") }
    var departamento by remember { mutableStateOf(usuario?.nombreDepartamento ?: "") }
    var direccion by remember { mutableStateOf(usuario?.direccion ?: "") }

    var departamentos by remember { mutableStateOf<List<String>>(emptyList()) }
    var expandedRol by remember { mutableStateOf(false) }
    var expandedDep by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }

    // 🔄 Cargar departamentos
    LaunchedEffect(Unit) {
        scope.launch {
            departamentos = cargarDepartamentos()
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val ok = guardarUsuario(
                        esUpdate = esUpdate,
                        usuarioId = usuario?.id ?: 0,
                        nombre = nombre,
                        mail = mail,
                        password = password,
                        rol = rol,
                        departamento = departamento,
                        direccion = direccion
                    )
                    if (ok) onClose()
                    else error = "Error al guardar usuario"
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Cancelar")
            }
        },
        title = {
            Text(if (esUpdate) "Editar usuario" else "Nuevo usuario")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mail,
                    onValueChange = { mail = it },
                    label = { Text("Mail") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (!esUpdate) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // -------- ROL --------
                ExposedDropdownMenuBox(
                    expanded = expandedRol,
                    onExpandedChange = { expandedRol = !expandedRol }
                ) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRol,
                        onDismissRequest = { expandedRol = false }
                    ) {
                        listOf("admin", "jefe", "empleado").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    rol = it
                                    expandedRol = false
                                }
                            )
                        }
                    }
                }

                // -------- DEPARTAMENTO --------
                ExposedDropdownMenuBox(
                    expanded = expandedDep,
                    onExpandedChange = { expandedDep = !expandedDep }
                ) {
                    OutlinedTextField(
                        value = departamento,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Departamento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDep,
                        onDismissRequest = { expandedDep = false }
                    ) {
                        departamentos.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    departamento = it
                                    expandedDep = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// ============================================================
// ======================= SERVIDOR ===========================
// ============================================================

private suspend fun guardarUsuario(
    esUpdate: Boolean,
    usuarioId: Int,
    nombre: String,
    mail: String,
    password: String,
    rol: String,
    departamento: String,
    direccion: String
): Boolean = withContext(Dispatchers.IO) {

    val con = MainActivity.conexion ?: return@withContext false

    if (nombre.isBlank() || mail.isBlank() || rol.isBlank()) return@withContext false

    if (esUpdate) {
        con.enviar(
            "UPDATE_USUARIO" +
                    MainActivity.SEP + usuarioId +
                    MainActivity.SEP + nombre +
                    MainActivity.SEP + mail +
                    MainActivity.SEP + rol +
                    MainActivity.SEP + departamento +
                    MainActivity.SEP + direccion
        )
    } else {
        if (password.isBlank()) return@withContext false

        con.enviar(
            "CREAR_USUARIO" +
                    MainActivity.SEP + nombre +
                    MainActivity.SEP + mail +
                    MainActivity.SEP + password +   // 🔥 HASH EN SERVIDOR
                    MainActivity.SEP + rol +
                    MainActivity.SEP + departamento +
                    MainActivity.SEP + direccion
        )
    }

    true
}

private suspend fun cargarDepartamentos(): List<String> =
    withContext(Dispatchers.IO) {

        val con = MainActivity.conexion ?: return@withContext emptyList()

        con.enviar("LISTAR_DEPARTAMENTOS_SIMPLE")
        val resp = con.leerRespuestaCompleta()

        resp.split(MainActivity.JUMP)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
