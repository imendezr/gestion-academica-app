package com.example.gestionacademicaapp.data.api.model.dto

data class GrupoDto(
    val idGrupo: Long,
    val idCarreraCurso: Long,
    val numeroGrupo: Int,
    val horario: String,
    val idProfesor: Long,
    val nombreProfesor: String
)
