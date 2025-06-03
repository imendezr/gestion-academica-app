package com.example.gestionacademicaapp.data.api.model.dto

data class GrupoProfesorDto(
    val idGrupo: Long,
    val numeroGrupo: Long,
    val horario: String,
    val codigoCurso: String,
    val nombreCurso: String,
    val codigoCarrera: String,
    val nombreCarrera: String,
    val anio: Long,
    val numeroCiclo: Long
)
