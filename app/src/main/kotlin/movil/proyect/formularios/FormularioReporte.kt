package movil.proyect.formularios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import movil.proyect.MainActivity
import movil.proyect.Modelos.Tarea

@Composable
fun FormularioReporte(
    tarea: Tarea,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var info by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("pendiente") }
    var error by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    val estados = listOf(
        "pendiente",
        "No puedo hacerlo",
        "imposible",
        "completado"
    )

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Nuevo reporte") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = info,
                    onValueChange = { info = it },
                    label = { Text("Información del reporte") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Estado")

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

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando,
                onClick = {
                    if (info.isBlank()) {
                        error = "La información no puede estar vacía"
                        return@TextButton
                    }

                    scope.launch(Dispatchers.IO) {
                        try {
                            guardando = true

                            val con = MainActivity.conexion ?: return@launch
                            val usuario = MainActivity.usuarioActual ?: return@launch

                            // 🔥 INSERT SIN ESPERAR RESPUESTA
                            con.enviar(
                                "CREAR_REPORTE" +
                                        MainActivity.SEP + tarea.id +
                                        MainActivity.SEP + info +
                                        MainActivity.SEP + estado +
                                        MainActivity.SEP + usuario.id
                            )

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
