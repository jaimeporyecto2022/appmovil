package movil.proyect.formularios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import movil.proyect.MainActivity
import movil.proyect.Modelos.Reporte
import movil.proyect.Modelos.Tarea

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioReporte(
    tarea: Tarea,
    reporte: Reporte? = null,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val esUpdate = reporte != null

    var info by remember { mutableStateOf(reporte?.informacion ?: "") }
    var estado by remember { mutableStateOf(reporte?.estado ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(if (esUpdate) "Editar reporte" else "Nuevo reporte")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = info,
                    onValueChange = { info = it },
                    label = { Text("Información del reporte") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                val estados = listOf(
                    "pendiente",
                    "No puedo hacerlo",
                    "imposible",
                    "completado"
                )

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = false, onDismissRequest = {}) {}
                }

                // Selector simple
                estados.forEach {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(it)
                        RadioButton(
                            selected = estado == it,
                            onClick = { estado = it }
                        )
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando,
                onClick = {
                    if (info.isBlank() || estado.isBlank()) {
                        error = "Completa todos los campos"
                        return@TextButton
                    }

                    scope.launch(Dispatchers.IO) {
                        try {
                            guardando = true
                            val con = MainActivity.conexion ?: return@launch

                            // 🔥 INSERT (igual que JavaFX)
                            con.enviar(
                                "CREAR_REPORTE" +
                                        MainActivity.SEP + tarea.id +
                                        MainActivity.SEP + info +
                                        MainActivity.SEP + estado +
                                        MainActivity.SEP + MainActivity.usuarioActual!!.id
                            )

                            con.leerRespuestaCompleta()

                            onClose()

                        } catch (e: Exception) {
                            error = "Error guardando reporte"
                            e.printStackTrace()
                        } finally {
                            guardando = false
                        }
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
        }
    )
}
