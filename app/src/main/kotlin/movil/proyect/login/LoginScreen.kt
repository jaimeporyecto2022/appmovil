package movil.proyect.login
import movil.proyect.Modelos.*
import movil.proyect.login.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import movil.proyect.Modelos.Usuario


@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginOK: (Usuario) -> Unit
) {
    val uiState = viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = uiState.value.usuario,
            onValueChange = { viewModel.setUsuario(it) },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.value.password,
            onValueChange = { viewModel.setPassword(it) },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = uiState.value.mensaje,
            color = if (uiState.value.esError) Color.Red else Color.Yellow
        )

        Button(
            onClick = { viewModel.login(onLoginOK) },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("Entrar")
        }
    }
}