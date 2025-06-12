package com.example.gestionacademicaapp.data.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matriculas")
data class Matricula(
    @PrimaryKey val idMatricula: Long,
    @ColumnInfo(name = "pk_alumno") var pkAlumno: Long,
    @ColumnInfo(name = "pk_grupo") var pkGrupo: Long,
    @ColumnInfo(name = "nota") var nota: Long
)
