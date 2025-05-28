package com.example.gestionacademicaapp.data.api.model.dto

data class MatriculaAlumnoDto(
    val idMatricula: Long,
    val nota: Double,
    val numeroGrupo: String,
    val horario: String,
    val codigoCarrera: String,
    val nombreCarrera: String,
    val codigoCurso: String,
    val nombreCurso: String,
    val nombreProfesor: String,
    val cedulaProfesor: String
)
