package com.example.gestionacademicaapp.data.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrera_cursos")
data class CarreraCurso(
    @PrimaryKey val idCarreraCurso: Long,
    @ColumnInfo(name = "pk_carrera") val pkCarrera: Long,
    @ColumnInfo(name = "pk_curso") val pkCurso: Long,
    @ColumnInfo(name = "pk_ciclo") val pkCiclo: Long
)
