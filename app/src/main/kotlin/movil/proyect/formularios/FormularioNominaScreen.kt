package movil.proyect.formularios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*;
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import movil.proyect.MainActivity
import movil.proyect.Modelos.Nomina
import movil.proyect.Modelos.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioNominaScreen(
    usuario: Usuario,
    nomina: Nomina? = null,
    onClose: () -> Unit
) {
    val esUpdate = nomina != null
    val scope = rememberCoroutineScope()

    var importe by remember { mutableStateOf(nomina?.importe?.toString() ?: "") }
    var concepto by remember { mutableStateOf(nomina?.concepto ?: "") }
    var tipo by remember { mutableStateOf(nomina?.tipo ?: "") }

    val tipos = listOf("salario", "hora_extra", "plus", "deduccion")

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(if (esUpdate) "Editar nómina" else "Nueva nómina")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = importe,
                    onValueChange = { importe = it },
                    label = { Text("Importe (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto") },
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    var expanded by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = tipo,
                        onValueChange = {},
                        label = { Text("Tipo") },
                        readOnly = true,
                        modifier = Modifier.menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        tipos.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    tipo = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        guardarNomina(
                            usuario = usuario,
                            nomina = nomina,
                            importe = importe,
                            concepto = concepto,
                            tipo = tipo
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
        }
    )
}
private fun guardarNomina(
    usuario: Usuario,
    nomina: Nomina?,
    importe: String,
    concepto: String,
    tipo: String
) {
    val con = MainActivity.conexion ?: return

    val valor = importe.toDoubleOrNull() ?: return

    if (nomina != null) {
        // UPDATE
        con.enviar(
            "UPDATE_NOMINA" +
                    MainActivity.SEP + nomina.id +
                    MainActivity.SEP + valor +
                    MainActivity.SEP + concepto +
                    MainActivity.SEP + tipo
        )
    } else {
        // INSERT
        con.enviar(
            "INSERT_NOMINA" +
                    MainActivity.SEP + usuario.id +
                    MainActivity.SEP + valor +
                    MainActivity.SEP + concepto +
                    MainActivity.SEP + tipo
        )
    }
}
