package movil.proyect.formularios

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import movil.proyect.MainActivity
import movil.proyect.Modelos.Nomina
import movil.proyect.Modelos.Usuario
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
    var tipo by remember { mutableStateOf(nomina?.tipo ?: "salario") }
    var fecha by remember { mutableStateOf(nomina?.fecha ?: LocalDate.now()) }

    var tipoMenu by remember { mutableStateOf(false) }

    val tipos = listOf("salario", "hora_extra", "plus", "deduccion")

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        guardarNomina(
                            esUpdate = esUpdate,
                            nominaId = nomina?.id,
                            usuarioId = usuario.id,
                            importe = importe,
                            concepto = concepto,
                            tipo = tipo,
                            fecha = fecha
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
        title = {
            Text(if (esUpdate) "Editar nómina" else "Nueva nómina")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = importe,
                    onValueChange = { importe = it },
                    label = { Text("Importe (€)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth()
                )

                // ---------- TIPO ----------
                Box {
                    OutlinedTextField(
                        value = tipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { tipoMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = tipoMenu,
                        onDismissRequest = { tipoMenu = false }
                    ) {
                        tipos.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    tipo = it
                                    tipoMenu = false
                                }
                            )
                        }
                    }
                }

                // ---------- FECHA ----------
                Text("Fecha")

                val pickerState = rememberDatePickerState(
                    initialSelectedDateMillis =
                        fecha.toEpochDay() * 86_400_000
                )

                DatePicker(
                    state = pickerState,
                    showModeToggle = false
                )

                pickerState.selectedDateMillis?.let {
                    fecha = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
            }
        }
    )
}

/* =========================
   ========= GUARDAR =======
   ========================= */

private fun guardarNomina(
    esUpdate: Boolean,
    nominaId: Int?,
    usuarioId: Int,
    importe: String,
    concepto: String,
    tipo: String,
    fecha: LocalDate
) {
    val con = MainActivity.conexion ?: return

    val importeDouble = importe.toDoubleOrNull() ?: return

    if (esUpdate) {
        con.enviar(
            "UPDATE_NOMINA${MainActivity.SEP}$nominaId" +
                    "${MainActivity.SEP}$importeDouble" +
                    "${MainActivity.SEP}$fecha" +
                    "${MainActivity.SEP}$concepto" +
                    "${MainActivity.SEP}$tipo"
        )
    } else {
        con.enviar(
            "INSERT_NOMINA${MainActivity.SEP}$usuarioId" +
                    "${MainActivity.SEP}$importeDouble" +
                    "${MainActivity.SEP}$fecha" +
                    "${MainActivity.SEP}$concepto" +
                    "${MainActivity.SEP}$tipo"
        )
    }
}
