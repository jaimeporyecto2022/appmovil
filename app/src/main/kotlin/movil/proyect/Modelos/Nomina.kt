package movil.proyect.Modelos

import java.time.LocalDate

data class Nomina(
    var id: Int = 0,
    var importe: Double = 0.0,
    var fecha: LocalDate? = null,
    var concepto: String = "",
    var tipo: String = "",       // salario, hora_extra, plus, deduccion
    var idUsuario: Int = 0
) {
    fun esSalario() = tipo.equals("salario", ignoreCase = true)
    fun esHoraExtra() = tipo.equals("hora_extra", ignoreCase = true)
    fun esPlus() = tipo.equals("plus", ignoreCase = true)
    fun esDeduccion() = tipo.equals("deduccion", ignoreCase = true)
}
