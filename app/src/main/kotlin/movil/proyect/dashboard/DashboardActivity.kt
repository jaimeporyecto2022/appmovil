package movil.proyect.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import movil.proyect.MainActivity
import movil.proyect.Modelos.Usuario
import movil.proyect.ui.theme.MovilProyect_AppTheme
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuario = MainActivity.usuarioActual
        if (usuario == null) {
            finish() // No hay usuario, cerramos
            return
        }

        setContent {
            MovilProyect_AppTheme {
                DashboardScreen(
                    usuario = usuario,
                    onCerrarSesion = {
                        MainActivity.cerrarSesion()
                        finish()
                    }
                )
            }
        }
    }
}

// -----------------------------
// Pantalla principal (equivalente a BorderPane)
// -----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usuario: Usuario,
    onCerrarSesion: () -> Unit
) {
    var pantallaActual by remember { mutableStateOf("HOME") }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()   // ✅ AQUÍ

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(usuario = usuario) { opcion ->
                if (opcion == "SALIR") onCerrarSesion()
                else pantallaActual = opcion
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("${usuario.nombre.uppercase()} - ${usuario.rol.uppercase()}") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (pantallaActual) {
                    "TAREAS_USUARIO" -> TareasUsuarioScreen()
                    "ASIGNAR" -> TareasAsignadasScreen()
                    "USUARIOS" ->TareasUsuarioDashboardScreen()
                    "REPORTES" -> ReportesScreen()
                    else -> HomeScreen()
                }
            }
        }
    }
}


// -----------------------------
// Drawer lateral
// -----------------------------
@Composable
fun DrawerContent(usuario: Usuario, onSeleccion: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (!usuario.esAdmin()) {
            DrawerItem("Tareas") { onSeleccion("TAREAS_USUARIO") }
        }
        if (usuario.esAdmin()) {
            DrawerItem("Reportes") { onSeleccion("REPORTES") }
        }
        DrawerItem("Asignar") { onSeleccion("ASIGNAR") }

        if (
            usuario.esAdmin() ||
            usuario.nombreDepartamento == "contabilidad" ||
            usuario.nombreDepartamento == "Recursos Humanos"
        ) {
            DrawerItem("Usuarios") { onSeleccion("USUARIOS") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        DrawerItem("Salir") { onSeleccion("SALIR") }
    }
}

@Composable
fun DrawerItem(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(texto)
    }
}

// -----------------------------
// Contenido central (placeholders)
// -----------------------------
@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Bienvenido al sistema")
    }
}

@Composable
fun TareasUsuarioScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Tareas del usuario")
    }
}

@Composable
fun TareasAsignadasScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Asignación de tareas")
    }
}

@Composable
fun UsuariosScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Gestión de usuarios")
    }
}

@Composable
fun ReportesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Estadísticas por departamento")
    }
}

