package movil.proyect.Modelos

import java.time.LocalDate

data class Usuario(
    var id: Int = 0,
    var nombre: String = "",
    var mail: String = "",
    var password: String? = null,
    var rol: String = "empleado",
    var idDepartamento: Int = 0,
    var nombreDepartamento: String = "Sin departamento",
    var fechaAlta: LocalDate? = null,
    var direccion: String = ""
) {

    // ==================== MÉTODOS DE ROL ====================
    fun esAdmin() = rol.equals("admin", ignoreCase = true)
    fun esJefe() = rol.equals("jefe", ignoreCase = true)
    fun esEmpleado() = rol.equals("empleado", ignoreCase = true)
    fun esAdminOSuperior() = esAdmin()
    fun esJefeOSuperior() = esAdmin() || esJefe()

    // ==================== MÉTODOS ÚTILES ====================
    override fun toString(): String = nombre
}