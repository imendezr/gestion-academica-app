package com.example.gestionacademicaapp.data.api.model.dto

import androidx.room.ColumnInfo

data class MatriculaAlumnoDto(
    val idMatricula: Long,
    val nota: Double,
    @ColumnInfo(name = "numero_grupo")
    val numeroGrupo: String,
    val horario: String,
    val codigoCarrera: String,
    val nombreCarrera: String,
    val codigoCurso: String,
    val nombreCurso: String,
    val nombreProfesor: String,
    val cedulaProfesor: String
)
