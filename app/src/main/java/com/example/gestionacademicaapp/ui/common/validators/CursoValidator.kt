package com.example.gestionacademicaapp.ui.common.validators

class CursoValidator {
    val codigoRequiredError = "El código es requerido"
    val nombreRequiredError = "El nombre es requerido"
    val creditosRequiredError = "Los créditos son requeridos"
    val horasSemanalesRequiredError = "Las horas semanales son requeridas"

    fun validateCodigo(value: String): String? {
        if (value.isEmpty()) return null
        if (value.length !in 3..10) return "Debe tener entre 3 y 10 caracteres"
        if (!value.matches(Regex("^[A-Za-z0-9]+$"))) return "Solo letras y números"
        return null
    }

    fun validateNombre(value: String): String? {
        if (value.isEmpty()) return null
        if (value.length < 5) return "Debe tener al menos 5 caracteres"
        if (!value.matches(Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$"))) return "Solo letras y espacios"
        return null
    }

    fun validateCreditos(value: String): String? {
        if (value.isEmpty()) return null
        val creditos = value.toLongOrNull()
        if (creditos == null || creditos !in 1..10) return "Debe estar entre 1 y 10"
        return null
    }

    fun validateHorasSemanales(value: String): String? {
        if (value.isEmpty()) return null
        val horas = value.toLongOrNull()
        if (horas == null || horas !in 1..40) return "Debe estar entre 1 y 40"
        return null
    }
}
