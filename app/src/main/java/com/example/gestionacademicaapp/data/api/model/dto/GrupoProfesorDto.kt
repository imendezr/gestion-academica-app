package com.example.gestionacademicaapp.data.api.model.dto

import androidx.room.ColumnInfo

data class GrupoProfesorDto(
    val idGrupo: Long,
    @ColumnInfo(name = "numero_grupo")
    val numeroGrupo: Long,
    val horario: String,
    val codigoCurso: String,
    val nombreCurso: String,
    val codigoCarrera: String,
    val nombreCarrera: String,
    val anio: Long,
    val numeroCiclo: Long
)
