package com.example.gestionacademicaapp.data.api.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "profesores")
@Parcelize
data class Profesor(
    @PrimaryKey val idProfesor: Long,
    @ColumnInfo(name = "cedula") var cedula: String,
    @ColumnInfo(name = "nombre") var nombre: String,
    @ColumnInfo(name = "telefono") var telefono: String,
    @ColumnInfo(name = "email") var email: String
) : Parcelable
