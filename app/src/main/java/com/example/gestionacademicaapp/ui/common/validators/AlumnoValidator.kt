package com.example.gestionacademicaapp.ui.common.validators

import com.example.gestionacademicaapp.data.api.model.Carrera
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Locale

object AlumnoValidator {
    private const val CEDULA_ERROR = "La cédula del alumno es requerida"
    private const val CEDULA_FORMAT_ERROR = "La cédula del alumno debe ser numérica y tener exactamente 9 dígitos"
    private const val NOMBRE_ERROR = "El nombre del alumno es requerido"
    private const val NOMBRE_EMPTY_ERROR = "El nombre del alumno no puede estar vacío"
    private const val TELEFONO_ERROR = "El teléfono del alumno es requerido"
    private const val TELEFONO_FORMAT_ERROR = "El teléfono del alumno debe ser numérico y tener exactamente 8 dígitos"
    private const val EMAIL_ERROR = "El email del alumno es requerido"
    private const val EMAIL_FORMAT_ERROR = "El email del alumno tiene un formato inválido"
    private const val FECHA_NACIMIENTO_ERROR = "La fecha de nacimiento del alumno es requerida"
    private const val FECHA_NACIMIENTO_FORMAT_ERROR = "Formato de fecha inválido (yyyy-MM-dd)"
    private const val FECHA_NACIMIENTO_FUTURE_ERROR = "La fecha de nacimiento no puede ser futura"
    private const val CARRERA_ERROR = "La carrera del alumno es requerida"
    private const val CARRERA_INVALID_ERROR = "Carrera inválida"

    private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

    fun validateCedula(cedula: String): String? {
        if (cedula.isEmpty()) return CEDULA_ERROR
        if (!cedula.matches(Regex("^[0-9]{9}$"))) return CEDULA_FORMAT_ERROR
        return null
    }

    fun validateNombre(nombre: String): String? {
        if (nombre.isEmpty()) return NOMBRE_ERROR
        if (nombre.trim().isEmpty()) return NOMBRE_EMPTY_ERROR
        return null
    }

    fun validateTelefono(telefono: String): String? {
        if (telefono.isEmpty()) return TELEFONO_ERROR
        if (!telefono.matches(Regex("^[0-9]{8}$"))) return TELEFONO_FORMAT_ERROR
        return null
    }

    fun validateEmail(email: String): String? {
        if (email.isEmpty()) return EMAIL_ERROR
        if (!email.matches(EMAIL_REGEX)) return EMAIL_FORMAT_ERROR
        return null
    }

    fun validateFechaNacimiento(value: String): String? {
        if (value.isEmpty()) return FECHA_NACIMIENTO_ERROR
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
            val date = LocalDate.parse(value, formatter)
            if (date.isAfter(LocalDate.now())) return FECHA_NACIMIENTO_FUTURE_ERROR
        } catch (e: DateTimeParseException) {
            return FECHA_NACIMIENTO_FORMAT_ERROR
        }
        return null
    }

    fun validateCarrera(value: String, carreras: List<Carrera>): String? {
        if (value.isEmpty()) return CARRERA_ERROR
        val carreraId = value.toLongOrNull() ?: return CARRERA_INVALID_ERROR
        if (carreras.none { it.idCarrera == carreraId }) return CARRERA_INVALID_ERROR
        return null
    }
}
