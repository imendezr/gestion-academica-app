package com.example.gestionacademicaapp.data.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grupos")
data class Grupo(
    @PrimaryKey val idGrupo: Long,
    @ColumnInfo(name = "id_carrera_curso") var idCarreraCurso: Long,
    @ColumnInfo(name = "numero_grupo") var numeroGrupo: Long,
    @ColumnInfo(name = "horario") var horario: String,
    @ColumnInfo(name = "id_profesor") var idProfesor: Long
)
