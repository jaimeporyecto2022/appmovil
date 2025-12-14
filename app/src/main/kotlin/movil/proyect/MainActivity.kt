package movil.proyect
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import movil.proyect.Modelos.Usuario
import movil.proyect.login.LoginScreen
import movil.proyect.login.LoginViewModel
import movil.proyect.network.ConexionCliente
import movil.proyect.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import movil.proyect.dashboard.DashboardActivity


class MainActivity : ComponentActivity() {

    companion object {
        var usuarioActual: Usuario? = null
        var conexion: ConexionCliente? = null

        const val SEP = "@Tr&m"
        const val JUMP = "@Jump"

        fun cerrarSesion() {
            conexion?.cerrar()
            conexion = null
            usuarioActual = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        

        setContent {
            MovilProyect_AppTheme {
                val viewModel: LoginViewModel = viewModel()

                LoginScreen(
                    viewModel = viewModel,
                    onLoginOK = {
                        startActivity(
                            Intent(this@MainActivity, DashboardActivity::class.java)
                        )
                        finish()
                    }
                )
            }
        }
    }
}

