package movil.proyect.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import movil.proyect.MainActivity
import movil.proyect.Modelos.Usuario
import java.time.format.DateTimeFormatter

@Composable
fun UsuariosScreen(
    onEditarUsuario: (Usuario) -> Unit,
    onVerNominas: (Usuario) -> Unit
) {
    val scope = rememberCoroutineScope()
    var usuarios by remember { mutableStateOf(listOf<Usuario>()) }

    val usuarioActual = MainActivity.usuarioActual ?: return

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val con = MainActivity.conexion ?: return@launch
                con.enviar("TODOS_USUARIOS")
                val respuesta = con.leerRespuestaCompleta()

                val lista = respuesta.split(MainActivity.JUMP)
                    .filter { it.isNotBlank() }
                    .mapNotNull { linea ->
                        val c = linea.split(MainActivity.SEP)
                        if (c.size < 8) return@mapNotNull null

                        Usuario(
                            id = c[0].toInt(),
                            nombre = c[1],
                            mail = c[2],
                            rol = c[3],
                            idDepartamento = c[4].toInt(),
                            nombreDepartamento = c[5],
                            fechaAlta = c[6].takeIf { it.isNotBlank() }?.let {
                                java.time.LocalDate.parse(it)
                            },
                            direccion = c[7]
                        )
                    }

                usuarios = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // 🔝 BARRA SUPERIOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Usuarios",
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (usuarioActual.esAdmin()
                || usuarioActual.nombreDepartamento.equals("Recursos Humanos", true)
            ) {
                IconButton(onClick = {
                    // abrir formulario crear usuario
                }) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = "Nuevo usuario"
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 📋 LISTA
        LazyColumn {
            items(usuarios) { usuario ->
                UsuarioRow(
                    usuario = usuario,
                    puedeEditar = usuarioActual.esAdmin()
                            || usuarioActual.nombreDepartamento.equals("Recursos Humanos", true),
                    puedeNominas = usuarioActual.esAdmin()
                            || usuarioActual.nombreDepartamento.equals("contabilidad", true),
                    onEditar = onEditarUsuario,
                    onNominas = onVerNominas
                )
                Divider()
            }
        }
    }
}

@Composable
private fun UsuarioRow(
    usuario: Usuario,
    puedeEditar: Boolean,
    puedeNominas: Boolean,
    onEditar: (Usuario) -> Unit,
    onNominas: (Usuario) -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(usuario.nombre, fontWeight = FontWeight.Bold)
            Text(usuario.mail, style = MaterialTheme.typography.bodySmall)
            Text(usuario.rol.uppercase(), color = Color.Gray)
            Text(
                usuario.nombreDepartamento,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                usuario.fechaAlta?.format(fmt) ?: "—",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (puedeNominas) {
            IconButton(onClick = { onNominas(usuario) }) {
                Icon(Icons.Default.Receipt, contentDescription = "Nóminas")
            }
        }

        if (puedeEditar) {
            IconButton(onClick = { onEditar(usuario) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    }
}
