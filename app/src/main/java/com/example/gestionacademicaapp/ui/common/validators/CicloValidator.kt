package com.example.gestionacademicaapp.ui.common.validators

class CicloValidator {

    val anioRequiredError = "El año es requerido"
    val numeroRequiredError = "El número es requerido"
    val fechaInicioRequiredError = "La fecha de inicio es requerida"
    val fechaFinRequiredError = "La fecha de fin es requerida"

    fun validateAnio(value: String): String? {
        if (value.isEmpty()) return anioRequiredError
        val year = value.toLongOrNull() ?: return "El año debe ser un número válido"
        return if (year !in 1900..2100) "El año debe estar entre 1900 y 2100" else null
    }

    fun validateNumero(value: String): String? {
        return if (value !in listOf("1", "2")) "El número debe ser 1 o 2" else null
    }

    fun validateFechaInicio(value: String, fechaFin: String?): String? {
        if (value.isEmpty()) return fechaInicioRequiredError
        if (fechaFin?.isNotEmpty() == true && value > fechaFin) {
            return "La fecha de inicio debe ser anterior a la fecha de fin"
        }
        return null
    }

    fun validateFechaFin(value: String, fechaInicio: String?): String? {
        if (value.isEmpty()) return fechaFinRequiredError
        if (fechaInicio?.isNotEmpty() == true && value < fechaInicio) {
            return "La fecha de fin debe ser posterior a la fecha de inicio"
        }
        return null
    }
}
