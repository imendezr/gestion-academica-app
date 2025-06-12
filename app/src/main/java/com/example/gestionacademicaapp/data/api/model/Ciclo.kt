package com.example.gestionacademicaapp.data.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ciclos")
data class Ciclo(
    @PrimaryKey val idCiclo: Long,
    @ColumnInfo(name = "anio") var anio: Long,
    @ColumnInfo(name = "numero") var numero: Long,
    @ColumnInfo(name = "fecha_inicio") var fechaInicio: String,
    @ColumnInfo(name = "fecha_fin") var fechaFin: String,
    @ColumnInfo(name = "estado") var estado: String
)
