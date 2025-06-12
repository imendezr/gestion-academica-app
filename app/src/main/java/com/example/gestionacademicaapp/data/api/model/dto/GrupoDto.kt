package com.example.gestionacademicaapp.data.api.model.dto

import androidx.room.ColumnInfo

data class GrupoDto(
    val idGrupo: Long,
    val idCarreraCurso: Long,
    @ColumnInfo(name = "numero_grupo")
    val numeroGrupo: Long,
    val horario: String,
    val idProfesor: Long,
    val nombreProfesor: String
)
