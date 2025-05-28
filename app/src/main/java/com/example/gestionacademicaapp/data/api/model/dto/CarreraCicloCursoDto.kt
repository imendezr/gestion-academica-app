package com.example.gestionacademicaapp.data.api.model.dto

data class CarreraCicloCursoDto(
    val idCurso: Long,
    val codigo: String,
    val nombre: String,
    val creditos: Long,
    val horasSemanales: Long,
    val idCarreraCurso: Long
)
