package com.example.gestionacademicaapp.ui.common.validators

import jakarta.inject.Inject

class GrupoValidator @Inject constructor() {

    companion object {
        const val ERROR_NUMERO_GRUPO_REQUERIDO = "El número de grupo es requerido"
        const val ERROR_NUMERO_GRUPO_INVALIDO = "El número de grupo debe ser mayor a 0"
        const val ERROR_HORARIO_REQUERIDO = "El horario es requerido"
        const val ERROR_PROFESOR_REQUERIDO = "El profesor es requerido"
    }

    fun validate(numeroGrupo: Long, horario: String, idProfesor: Long): List<String> {
        val errors = mutableListOf<String>()
        validateNumeroGrupo(numeroGrupo.toString())?.let { errors.add(it) }
        validateHorario(horario)?.let { errors.add(it) }
        validateProfesor(idProfesor.toString())?.let { errors.add(it) }
        return errors
    }

    fun validateNumeroGrupo(value: String): String? {
        if (value.isBlank()) return ERROR_NUMERO_GRUPO_REQUERIDO
        return try {
            val num = value.toLong()
            if (num <= 0) ERROR_NUMERO_GRUPO_INVALIDO else null
        } catch (e: NumberFormatException) {
            ERROR_NUMERO_GRUPO_INVALIDO
        }
    }

    fun validateHorario(value: String): String? {
        return if (value.isBlank()) ERROR_HORARIO_REQUERIDO else null
    }

    fun validateProfesor(value: String): String? {
        return if (value.isBlank()) ERROR_PROFESOR_REQUERIDO else null
    }
}