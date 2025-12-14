package movil.proyect.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.PersonAdd
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
    onNuevoUsuario: () -> Unit = {},
    onEditarUsuario: (Usuario) -> Unit = {},
    onVerNominas: (Usuario) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val usuarioActual = MainActivity.usuarioActual
    val conexion = MainActivity.conexion

    val puedeEditar =
        usuarioActual?.esAdmin() == true ||
                usuarioActual?.nombreDepartamento == "Recursos Humanos"

    val puedeVerNominas =
        usuarioActual?.esAdmin() == true ||
                usuarioActual?.nombreDepartamento == "contabilidad"

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                if (conexion == null) {
                    error = "Sin conexión"
                    cargando = false
                    return@launch
                }

                conexion.enviar("TODOS_USUARIOS")
                val respuesta = conexion.leerRespuestaCompleta()

                val lista = respuesta
                    .split(MainActivity.JUMP)
                    .filter { it.isNotBlank() }
                    .mapNotNull { linea ->
                        val c = linea.split(MainActivity.SEP)
                        if (c.size < 8) return@mapNotNull null

                        try {
                            Usuario(
                                id = c[0].toInt(),
                                nombre = c[1],
                                mail = c[2],
                                rol = c[3],
                                idDepartamento = c[4].toInt(),
                                nombreDepartamento = c[5],
                                fechaAlta = c[6].takeIf { it.isNotBlank() }
                                    ?.let { java.time.LocalDate.parse(it) },
                                direccion = c[7]
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                usuarios = lista
                cargando = false

            } catch (e: Exception) {
                e.printStackTrace()
                error = "Error cargando usuarios"
                cargando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔝 BARRA SUPERIOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Usuarios",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (puedeEditar) {
                IconButton(onClick = onNuevoUsuario) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Nuevo usuario"
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(error!!, color = Color.Red)
            }

            else -> {
                LazyColumn {
                    items(usuarios) { usuario ->
                        UsuarioRow(
                            usuario = usuario,
                            puedeEditar = puedeEditar,
                            puedeVerNominas = puedeVerNominas,
                            onEditar = { onEditarUsuario(usuario) },
                            onNominas = { onVerNominas(usuario) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun UsuarioRow(
    usuario: Usuario,
    puedeEditar: Boolean,
    puedeVerNominas: Boolean,
    onEditar: () -> Unit,
    onNominas: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111))
            .padding(12.dp)
    ) {
        Text(usuario.nombre, fontWeight = FontWeight.Bold)
        Text(usuario.mail)
        Text("Rol: ${usuario.rol}")
        Text("Departamento: ${usuario.nombreDepartamento}")
        Text(
            "Alta: ${
                usuario.fechaAlta?.format(fmt) ?: "—"
            }"
        )

        Spacer(Modifier.height(8.dp))

        Row {
            if (puedeVerNominas) {
                IconButton(onClick = onNominas) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Nóminas"
                    )
                }
            }
            if (puedeEditar) {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar usuario"
                    )
                }
            }
        }
    }
}
