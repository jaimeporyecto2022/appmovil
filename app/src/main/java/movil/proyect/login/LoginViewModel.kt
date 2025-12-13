package movil.proyect.login
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import movil.proyect.Modelos.Usuario
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.*
import movil.proyect.MainActivity
import movil.proyect.network.ConexionCliente
import java.time.LocalDate
class LoginViewModel : ViewModel() {

    data class UiState(
        val usuario: String = "",
        val password: String = "",
        val mensaje: String = "",
        val esError: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun setUsuario(u: String) {
        _uiState.value = _uiState.value.copy(usuario = u)
    }

    fun setPassword(p: String) {
        _uiState.value = _uiState.value.copy(password = p)
    }

    fun login(onOK: (Usuario) -> Unit) {
        val usuario = _uiState.value.usuario.trim()
        val password = _uiState.value.password

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Completa todos los campos")
            return
        }

        mostrarMensaje("Conectando al Servidor...", false)

        viewModelScope.launch(Dispatchers.IO) {
            val conexion = MainActivity.conexion
            if (conexion == null) {
                withContext(Dispatchers.Main) {
                    mostrarError("No hay conexión con el servidor")
                }
                return@launch
            }

            try {
                // 👇 MISMO MENSAJE que JavaFX
                conexion.enviar(
                    "LOGIN${MainActivity.SEP}$usuario${MainActivity.SEP}$password"
                )

                val respuesta = conexion.leerRespuestaCompleta()

                withContext(Dispatchers.Main) {
                    when {
                        respuesta.startsWith("LOGIN_OK") -> {
                            val user = crearUsuarioDesdeRespuesta(respuesta)
                            MainActivity.usuarioActual = user
                            onOK(user)
                        }

                        respuesta.startsWith("LOGIN_ERROR") -> {
                            val msg = respuesta.split(MainActivity.SEP, limit = 2)
                                .getOrNull(1)
                                ?: "Acceso denegado"
                            mostrarError(msg)
                        }

                        else -> {
                            mostrarError("Respuesta desconocida del servidor")
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarError("No se pudo conectar al servidor")
                }
            }
        }
    }

    private fun crearUsuarioDesdeRespuesta(resp: String): Usuario {
        val datos = resp.split(MainActivity.SEP)

        val fechaAlta: LocalDate? = try {
            if (datos[6].isBlank() || datos[6] == "null") null
            else LocalDate.parse(datos[6])
        } catch (e: Exception) {
            null
        }

        return Usuario(
            id = datos[1].toInt(),
            nombre = datos[2],
            mail = datos[3],
            rol = datos[4],
            idDepartamento = datos[5].toIntOrNull() ?: 0,
            fechaAlta = fechaAlta,
            direccion = if (datos.size > 8) datos[8] else ""
        )
    }

    private fun mostrarError(msg: String) {
        _uiState.value = _uiState.value.copy(
            mensaje = msg,
            esError = true
        )
    }

    private fun mostrarMensaje(msg: String, error: Boolean) {
        _uiState.value = _uiState.value.copy(
            mensaje = msg,
            esError = error
        )
    }
}
