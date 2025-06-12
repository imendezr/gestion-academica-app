package com.example.gestionacademicaapp.data.api.model.dto

import androidx.room.ColumnInfo

data class CursoDto(
    val idCurso: Long,
    val codigo: String,
    val nombre: String,
    val creditos: Long,
    @ColumnInfo(name = "horas_semanales")
    val horasSemanales: Long,
    val idCarreraCurso: Long?,
    val anio: Long?,
    val numero: Long?,
    val idCiclo: Long?
)
