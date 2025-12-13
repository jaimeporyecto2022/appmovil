package movil.proyect.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.Socket

class ConexionCliente(
    private val host: String,
    private val port: Int
) {

    private var socket: Socket? = null
    private var out: PrintWriter? = null
    private var input: BufferedReader? = null

    private var ultimaRespuesta: String = ""

    suspend fun conectar() = withContext(Dispatchers.IO) {
        socket = Socket(host, port)
        out = PrintWriter(socket!!.getOutputStream(), true)
        input = BufferedReader(InputStreamReader(socket!!.getInputStream()))
    }

    suspend fun enviar(mensaje: String) = withContext(Dispatchers.IO) {
        out?.println(mensaje)
    }

    suspend fun leerLinea(): String? = withContext(Dispatchers.IO) {
        input?.readLine()
    }

    suspend fun leerRespuestaCompleta(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        var linea: String?

        while (true) {
            linea = input?.readLine() ?: break
            if (linea == "FIN_COMANDO") break
            sb.appendLine(linea)
        }

        ultimaRespuesta = sb.toString().trim()
        ultimaRespuesta
    }

    fun cerrar() {
        try {
            socket?.close()
        } catch (_: Exception) {}
    }
}