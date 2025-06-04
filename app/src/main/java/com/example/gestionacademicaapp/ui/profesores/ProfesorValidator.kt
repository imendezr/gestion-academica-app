package com.example.gestionacademicaapp.ui.profesores

object ProfesorValidator {
    private const val CEDULA_ERROR = "La cédula del profesor es requerida"
    private const val CEDULA_FORMAT_ERROR = "La cédula del profesor debe ser numérica y tener exactamente 9 dígitos"
    private const val NOMBRE_ERROR = "El nombre del profesor es requerido"
    private const val NOMBRE_EMPTY_ERROR = "El nombre del profesor no puede estar vacío"
    private const val TELEFONO_ERROR = "El teléfono del profesor es requerido"
    private const val TELEFONO_FORMAT_ERROR = "El teléfono del profesor debe ser numérico y tener exactamente 8 dígitos"
    private const val EMAIL_ERROR = "El email del profesor es requerido"
    private const val EMAIL_FORMAT_ERROR = "El email del profesor tiene un formato inválido"
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
}
