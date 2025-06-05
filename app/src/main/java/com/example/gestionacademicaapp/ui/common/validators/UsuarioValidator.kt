package com.example.gestionacademicaapp.ui.common.validators

import com.example.gestionacademicaapp.data.api.model.Carrera
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Locale

class UsuarioValidator {
    val cedulaRequiredError = "La cédula es requerida"
    val claveRequiredError = "La clave es requerida"
    val tipoRequiredError = "El tipo de usuario es requerido"
    val nombreRequiredError = "El nombre es requerido"
    val telefonoRequiredError = "El número de telefono es requerido"
    val emailRequiredError = "El correo es requerido"
    val fechaNacimientoRequiredError = "La fecha de nacimiento es requerida"
    val carreraRequiredError = "La carrera es requerida"

    fun validateCedula(value: String): String? {
        if (value.isEmpty()) return cedulaRequiredError
        if (!value.matches(Regex("^[0-9]{9}$"))) return "Debe ser numérica y tener exactamente 9 dígitos"
        return null
    }

    fun validateClave(value: String, isEditMode: Boolean): String? {
        if (isEditMode) return null // Clave optional for edits
        if (value.isEmpty()) return claveRequiredError
        if (value.trim().isEmpty()) return "No puede contener solo espacios"
        return null
    }

    fun validateTipo(value: String): String? {
        if (value.isEmpty()) return tipoRequiredError
        if (value !in listOf("Administrador", "Matriculador", "Profesor", "Alumno")) return "Tipo no válido"
        return null
    }

    fun validateNombre(value: String): String? {
        if (value.isEmpty()) return nombreRequiredError
        if (value.trim().isEmpty()) return "No puede estar vacío"
        return null
    }

    fun validateEmail(value: String): String? {
        if (value.isEmpty()) return emailRequiredError
        if (!value.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))) return "Formato de correo inválido"
        return null
    }

    fun validateTelefono(value: String): String? {
        if (value.isEmpty()) return telefonoRequiredError
        if (!value.matches(Regex("^[0-9]{8}$"))) return "Debe ser numérico y tener exactamente 8 dígitos"
        return null
    }

    fun validateFechaNacimiento(value: String): String? {
        if (value.isEmpty()) return fechaNacimientoRequiredError
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
            val date = LocalDate.parse(value, formatter)
            if (date.isAfter(LocalDate.now())) return "No puede ser una fecha futura"
        } catch (e: DateTimeParseException) {
            return "Formato inválido (yyyy-MM-dd)"
        }
        return null
    }

    fun validateCarrera(value: String, carreras: List<Carrera>): String? {
        if (value.isEmpty()) return carreraRequiredError
        val carreraId = value.toLongOrNull() ?: return "Carrera inválida"
        if (carreras.none { it.idCarrera == carreraId }) return "Carrera no existe"
        return null
    }
}
