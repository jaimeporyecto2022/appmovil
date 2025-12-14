package movil.proyect.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import movil.proyect.MainActivity
import movil.proyect.Modelos.Usuario
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

        mostrarMensaje("Conectando al servidor...", false)

        viewModelScope.launch(Dispatchers.IO) {
            val con = ConexionCliente("192.168.0.13", 5000)

            try {
                // 🔌 CONECTAR
                con.conectar()

                // 📤 ENVIAR LOGIN (PROTOCOLO CORRECTO)
                val mensaje = "LOGIN${MainActivity.SEP}$usuario${MainActivity.SEP}$password"
                con.enviar(mensaje)

                // 📥 LEER RESPUESTA
                val respuesta = con.leerRespuestaCompleta()
                Log.d("LOGIN", "RESPUESTA CRUDA -> [$respuesta]")

                withContext(Dispatchers.Main) {
                    if (respuesta.trim().startsWith("LOGIN_OK")) {
                        val user = crearUsuarioDesdeRespuesta(respuesta)
                        MainActivity.usuarioActual = user
                        MainActivity.conexion = con
                        onOK(user)
                    } else {
                        mostrarError("Usuario o contraseña incorrectos")
                        con.cerrar()
                    }
                }

            } catch (e: Exception) {
                Log.e("LOGIN", "Error en login", e)
                withContext(Dispatchers.Main) {
                    mostrarError("No se pudo conectar al servidor")
                }
                con.cerrar()
            }
        }
    }

    private fun crearUsuarioDesdeRespuesta(resp: String): Usuario {
        val datos = resp.trim().split(MainActivity.SEP)

        return Usuario(
            id = datos[1].toInt(),
            nombre = datos[2],
            mail = datos[3],
            rol = datos[4],
            idDepartamento = datos[5].toIntOrNull() ?: 0,
            nombreDepartamento = datos[6],
            fechaAlta = datos[7]
                .takeIf { it.isNotBlank() && it != "null" }
                ?.let { LocalDate.parse(it) },
            direccion = datos.getOrNull(8) ?: ""
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
