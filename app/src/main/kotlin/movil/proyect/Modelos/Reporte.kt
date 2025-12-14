package movil.proyect.Modelos

import java.time.LocalDate

data class Reporte(
    var id: Int? = null,
    var fechacreacion: LocalDate? = null,
    var informacion: String = "",
    var estado: String = "en_curso",  // en_curso, finalizada, irrealizable, transferir
    var idUsuarioReporte: Int? = null,
    var idTarea: Int? = null,
    var nombreUsuario: String = ""  // viene del servidor
)
