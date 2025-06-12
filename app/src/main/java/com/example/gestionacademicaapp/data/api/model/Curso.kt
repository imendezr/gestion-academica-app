package com.example.gestionacademicaapp.data.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cursos")
data class Curso(
    @PrimaryKey val idCurso: Long,
    @ColumnInfo(name = "codigo") var codigo: String,
    @ColumnInfo(name = "nombre") var nombre: String,
    @ColumnInfo(name = "creditos") var creditos: Long,
    @ColumnInfo(name = "horas_semanales") var horasSemanales: Long
)
