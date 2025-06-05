package com.example.gestionacademicaapp.ui.common.validators

class CarreraValidator {

    val codigoRequiredError = "El código es requerido"
    val nombreRequiredError = "El nombre es requerido"
    val tituloRequiredError = "El título es requerido"

    fun validateCodigo(value: String): String? {
        if (value.isBlank()) return codigoRequiredError
        return if (value.length !in 3..10) "Debe tener entre 3 y 10 caracteres" else null
    }

    fun validateNombre(value: String): String? {
        if (value.isBlank()) return nombreRequiredError
        return if (value.length < 5) "Debe tener al menos 5 caracteres" else null
    }

    fun validateTitulo(value: String): String? {
        if (value.isBlank()) return tituloRequiredError
        return if (value.length < 5) "Debe tener al menos 5 caracteres" else null
    }
}
