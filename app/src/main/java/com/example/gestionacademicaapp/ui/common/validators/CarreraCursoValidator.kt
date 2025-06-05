package com.example.gestionacademicaapp.ui.common.validators

import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.ui.carreras.model.CarreraCursoUI

class CarreraCursoValidator(
    private val cursosDisponibles: List<Curso>,
    private val ciclosDisponibles: List<Ciclo>,
    private val existingCarreraCursos: List<CarreraCursoUI> = emptyList(),
    private val carreraId: Long = 0
) {

    val cursoRequiredError = "El curso es requerido"
    val cicloRequiredError = "El ciclo es requerido"

    fun validateCurso(value: String, currentCursoId: Long? = null): String? {
        if (value.isEmpty()) return cursoRequiredError
        val idCurso = value.toLongOrNull() ?: return "Curso no válido"
        if (cursosDisponibles.none { it.idCurso == idCurso }) return "Curso no válido"
        if (currentCursoId != idCurso && existingCarreraCursos.any { it.curso.idCurso == idCurso && it.carreraId == carreraId }) {
            return "El curso ya está asociado a esta carrera"
        }
        return null
    }

    fun validateCiclo(value: String): String? {
        if (value.isEmpty()) return cicloRequiredError
        val idCiclo = value.toLongOrNull() ?: return "Ciclo no válido"
        if (ciclosDisponibles.none { it.idCiclo == idCiclo }) return "Ciclo no válido"
        return null
    }
}
