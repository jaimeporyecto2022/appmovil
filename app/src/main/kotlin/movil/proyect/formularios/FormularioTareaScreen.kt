package movil.proyect.formularios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import movil.proyect.MainActivity
import movil.proyect.Modelos.Tarea
import movil.proyect.Modelos.Usuario
import java.time.LocalDate

@Composable
fun FormularioTareaScreen(
    tarea: Tarea? = null,
    onClose: () -> Unit
) {
    val esUpdate = tarea != null
    val scope = rememberCoroutineScope()

    var titulo by remember { mutableStateOf(tarea?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tarea?.descripcion ?: "") }
    var estado by remember { mutableStateOf(tarea?.estado ?: "pendiente") }

    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }

    var estadoMenu by remember { mutableStateOf(false) }
    var usuarioMenu by remember { mutableStateOf(false) }

    val estados = listOf("pendiente", "completado", "imposible")

    // 🔄 cargar usuarios
    LaunchedEffect(Unit) {
        usuarios = cargarUsuarios()
        if (esUpdate) {
            usuarioSeleccionado =
                usuarios.find { it.id == tarea!!.idAsignado }
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Button(onClick = {
                guardarTarea(
                    esUpdate = esUpdate,
                    tareaId = tarea?.id,
                    titulo = titulo,
                    descripcion = descripcion,
                    estado = estado,
                    usuario = usuarioSeleccionado,
                    onClose = onClose
                )
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
            Text(if (esUpdate) "Editar tarea" else "Nueva tarea")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                // -------- ESTADO --------
                Box {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = {
                            IconButton(onClick = { estadoMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = estadoMenu,
                        onDismissRequest = { estadoMenu = false }
                    ) {
                        estados.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    estado = it
                                    estadoMenu = false
                                }
                            )
                        }
                    }
                }

                // -------- USUARIO --------
                Box {
                    OutlinedTextField(
                        value = usuarioSeleccionado?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignar a") },
                        trailingIcon = {
                            IconButton(onClick = { usuarioMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = usuarioMenu,
                        onDismissRequest = { usuarioMenu = false }
                    ) {
                        usuarios.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u.nombre) },
                                onClick = {
                                    usuarioSeleccionado = u
                                    usuarioMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

/* ===========================
   ======== GUARDAR ==========
   =========================== */

private fun guardarTarea(
    esUpdate: Boolean,
    tareaId: Int?,
    titulo: String,
    descripcion: String,
    estado: String,
    usuario: Usuario?,
    onClose: () -> Unit
) {
    if (titulo.isBlank() || usuario == null) return

    val con = MainActivity.conexion ?: return
    val creador = MainActivity.usuarioActual ?: return

    if (esUpdate) {
        con.enviar(
            "UPDATE_TAREA${MainActivity.SEP}$tareaId${MainActivity.SEP}${creador.id}" +
                    "${MainActivity.SEP}${usuario.id}${MainActivity.SEP}$descripcion" +
                    "${MainActivity.SEP}${LocalDate.now()}${MainActivity.SEP}${LocalDate.now()}" +
                    "${MainActivity.SEP}$estado${MainActivity.SEP}$titulo"
        )
    } else {
        con.enviar(
            "INSERT_TAREA${MainActivity.SEP}${creador.id}${MainActivity.SEP}${usuario.id}" +
                    "${MainActivity.SEP}$descripcion${MainActivity.SEP}${LocalDate.now()}" +
                    "${MainActivity.SEP}${LocalDate.now()}${MainActivity.SEP}$estado" +
                    "${MainActivity.SEP}$titulo"
        )
    }

    onClose()
}

/* ===========================
   ===== CARGAR USUARIOS =====
   =========================== */

private suspend fun cargarUsuarios(): List<Usuario> =
    withContext(Dispatchers.IO) {

        val con = MainActivity.conexion ?: return@withContext emptyList()
        val user = MainActivity.usuarioActual ?: return@withContext emptyList()

        if (user.esAdmin()) {
            con.enviar("USUARIOS_SIMPLE")
        } else {
            con.enviar("USUARIOS_DEP_SIMPLE${MainActivity.SEP}${user.idDepartamento}")
        }

        val resp = con.leerRespuestaCompleta()
        val lista = mutableListOf<Usuario>()

        resp.split(MainActivity.JUMP).forEach {
            val c = it.split(MainActivity.SEP)
            if (c.size >= 2) {
                lista.add(
                    Usuario(
                        id = c[0].toInt(),
                        nombre = c[1]
                    )
                )
            }
        }

        lista
    }
