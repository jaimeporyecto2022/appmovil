package movil.proyect.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import movil.proyect.MainActivity
import movil.proyect.Modelos.Usuario
import movil.proyect.ui.theme.MovilProyect_AppTheme

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuario = MainActivity.usuarioActual
        if (usuario == null) {
            finish()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usuario: Usuario,
    onCerrarSesion: () -> Unit
) {
    var pantallaActual by remember { mutableStateOf("HOME") }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                usuario = usuario,
                onSeleccion = { opcion ->
                    if (opcion == "SALIR") {
                        onCerrarSesion()
                    } else {
                        pantallaActual = opcion
                        scope.launch { drawerState.close() }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("${usuario.nombre.uppercase()} - ${usuario.rol.uppercase()}")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
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

                    "HOME" -> HomeScreen()

                    "USUARIOS" -> UsuariosScreen()

                    "ASIGNAR" -> {
                        TareasDashboardScreen(
                            onBack = {
                                pantallaActual = "USUARIOS"
                            }
                        )
                    }

                    "NOMINAS" -> {
                        NominasDashboardScreen(
                            usuario = usuario,
                            onBack = {
                                pantallaActual = "USUARIOS"
                            }
                        )
                    }

                    "REPORTES" -> {
                        Text(
                            "Reportes globales (pendiente)",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> HomeScreen()
                }
            }
        }
    }
}


// --------------------------------------------------
// Drawer lateral
// --------------------------------------------------
@Composable
fun DrawerContent(
    usuario: Usuario,
    onSeleccion: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        DrawerItem("Inicio") { onSeleccion("HOME") }

        if (!usuario.esAdmin()) {
            DrawerItem("Mis tareas") { onSeleccion("TAREAS_USUARIO") }
        }

        DrawerItem("Asignar tareas") { onSeleccion("ASIGNAR") }

        if (
            usuario.esAdmin() ||
            usuario.nombreDepartamento == "contabilidad" ||
            usuario.nombreDepartamento == "Recursos Humanos"
        ) {
            DrawerItem("Usuarios") { onSeleccion("USUARIOS") }
        }

        if (usuario.esAdmin()) {
            DrawerItem("Nóminas") { onSeleccion("NOMINAS") }
        }

        if (usuario.esAdmin()) {
            DrawerItem("Reportes") { onSeleccion("REPORTES") }
        }

        Spacer(modifier = Modifier.weight(1f))

        DrawerItem("Salir") { onSeleccion("SALIR") }
    }
}

@Composable
fun DrawerItem(
    texto: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(texto)
    }
}

// --------------------------------------------------
// Pantallas base
// --------------------------------------------------
@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Bienvenido al sistema")
    }
}
