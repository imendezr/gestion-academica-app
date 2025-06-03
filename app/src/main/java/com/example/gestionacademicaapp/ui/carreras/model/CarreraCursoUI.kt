package com.example.gestionacademicaapp.ui.carreras.model

import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Curso

data class CarreraCursoUI(
    val idCarreraCurso: Long,
    val carreraId: Long,
    val curso: Curso,
    val cicloId: Long,
    val ciclo: Ciclo? = null // Se cargará por separado si es necesario
)
