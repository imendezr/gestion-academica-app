package com.example.gestionacademicaapp.data.api.model.dto

data class CursoDto(
    val idCurso: Long,
    val codigo: String,
    val nombre: String,
    val creditos: Long,
    val horasSemanales: Long,
    val idCarreraCurso: Long?,
    val anio: Long?,
    val numero: Long?,
    val idCiclo: Long?
)
