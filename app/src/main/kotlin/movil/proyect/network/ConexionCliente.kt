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

    fun conectar() {
        socket = Socket(host, port)
        out = PrintWriter(socket!!.getOutputStream(), true)
        input = BufferedReader(InputStreamReader(socket!!.getInputStream()))
    }

    fun enviar(mensaje: String) {
        out?.println(mensaje)
    }

    fun leerLinea(): String? {
        return input?.readLine()
    }

    fun leerRespuestaCompleta(): String {
        val sb = StringBuilder()
        var linea: String?

        while (true) {
            linea = input?.readLine() ?: break
            if (linea == "FIN_COMANDO") break
            sb.appendLine(linea)
        }

        ultimaRespuesta = sb.toString().trim()
        return ultimaRespuesta
    }

    fun cerrar() {
        try { socket?.close() } catch (_: Exception) {}
    }
}
