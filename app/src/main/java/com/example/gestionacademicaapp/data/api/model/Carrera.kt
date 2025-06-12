package com.example.gestionacademicaapp.data.api.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "carreras")
@Parcelize
data class Carrera(
    @PrimaryKey val idCarrera: Long,
    @ColumnInfo(name = "codigo") var codigo: String,
    @ColumnInfo(name = "nombre") var nombre: String,
    @ColumnInfo(name = "titulo") var titulo: String
) : Parcelable
