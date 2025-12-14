package movil.proyect.formularios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import movil.proyect.MainActivity
import movil.proyect.Modelos.Tarea
import movil.proyect.Modelos.Usuario
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
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

    var fechaInicio by remember { mutableStateOf<LocalDate?>(tarea?.fechaInicio) }
    var fechaFin by remember { mutableStateOf<LocalDate?>(tarea?.fechaFin) }

    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }

    var estadoMenu by remember { mutableStateOf(false) }
    var usuarioMenu by remember { mutableStateOf(false) }

    var mostrarInicioPicker by remember { mutableStateOf(false) }
    var mostrarFinPicker by remember { mutableStateOf(false) }

    val estados = listOf("pendiente", "completado", "imposible")

    // ================= CARGAR USUARIOS =================
    LaunchedEffect(Unit) {
        usuarios = cargarUsuarios()
        if (esUpdate) {
            usuarioSeleccionado =
                usuarios.find { it.id == tarea?.idAsignado }
        }
    }

    // ================= DATE PICKERS =================
    if (mostrarInicioPicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarInicioPicker = false },
            confirmButton = {
                TextButton(onClick = { mostrarInicioPicker = false }) {
                    Text("OK")
                }
            }
        ) {
            val pickerState = rememberDatePickerState()
            DatePicker(state = pickerState)
            pickerState.selectedDateMillis?.let {
                fechaInicio = Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }
    }

    if (mostrarFinPicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarFinPicker = false },
            confirmButton = {
                TextButton(onClick = { mostrarFinPicker = false }) {
                    Text("OK")
                }
            }
        ) {
            val pickerState = rememberDatePickerState()
            DatePicker(state = pickerState)
            pickerState.selectedDateMillis?.let {
                fechaFin = Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }
    }

    // ================= UI =================
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(if (esUpdate) "Editar tarea" else "Nueva tarea")
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        guardarTarea(
                            esUpdate = esUpdate,
                            tareaId = tarea?.id,
                            titulo = titulo,
                            descripcion = descripcion,
                            estado = estado,
                            usuario = usuarioSeleccionado,
                            inicio = fechaInicio,
                            fin = fechaFin
                        )
                        onClose()
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Cancelar")
            }
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

                // -------- FECHAS --------
                OutlinedButton(
                    onClick = { mostrarInicioPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Inicio: ${fechaInicio ?: "Sin fecha"}")
                }

                OutlinedButton(
                    onClick = { mostrarFinPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Fin: ${fechaFin ?: "Sin fecha"}")
                }
            }
        }
    )
}

/* ======================================================
   ===================== GUARDAR ========================
   ====================================================== */

private suspend fun guardarTarea(
    esUpdate: Boolean,
    tareaId: Int?,
    titulo: String,
    descripcion: String,
    estado: String,
    usuario: Usuario?,
    inicio: LocalDate?,
    fin: LocalDate?
) {
    if (titulo.isBlank() || usuario == null) return

    val con = MainActivity.conexion ?: return
    val creador = MainActivity.usuarioActual ?: return

    withContext(Dispatchers.IO) {
        if (esUpdate) {
            con.enviar(
                "UPDATE_TAREA${MainActivity.SEP}$tareaId" +
                        "${MainActivity.SEP}${creador.id}" +
                        "${MainActivity.SEP}${usuario.id}" +
                        "${MainActivity.SEP}$descripcion" +
                        "${MainActivity.SEP}${inicio ?: ""}" +
                        "${MainActivity.SEP}${fin ?: ""}" +
                        "${MainActivity.SEP}$estado" +
                        "${MainActivity.SEP}$titulo"
            )
        } else {
            con.enviar(
                "INSERT_TAREA${MainActivity.SEP}${creador.id}" +
                        "${MainActivity.SEP}${usuario.id}" +
                        "${MainActivity.SEP}$descripcion" +
                        "${MainActivity.SEP}${inicio ?: ""}" +
                        "${MainActivity.SEP}${fin ?: ""}" +
                        "${MainActivity.SEP}$estado" +
                        "${MainActivity.SEP}$titulo"
            )
        }
    }
}

/* ======================================================
   ================= CARGAR USUARIOS ====================
   ====================================================== */

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

        resp.split(MainActivity.JUMP).forEach { linea ->
            val c = linea.trim().split(MainActivity.SEP)
            if (c.size >= 2) {
                try {
                    lista.add(
                        Usuario(
                            id = c[0].trim().toInt(),
                            nombre = c[1].trim()
                        )
                    )
                } catch (_: Exception) {}
            }
        }

        lista
    }
