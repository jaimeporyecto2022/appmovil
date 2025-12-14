package movil.proyect.Modelos

import java.time.LocalDate

data class Tarea(
    var id: Int = 0,
    var titulo: String = "",
    var descripcion: String = "",
    var fechaCreacion: LocalDate? = null,
    var fechaInicio: LocalDate? = null,
    var fechaFin: LocalDate? = null,
    var estado: String = "",
    var nombreCreador: String = "",
    var nombreAsignado: String = "",
    var idAsignado: Int = 0
)
