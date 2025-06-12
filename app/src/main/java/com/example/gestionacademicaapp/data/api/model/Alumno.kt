package com.example.gestionacademicaapp.data.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alumnos")
data class Alumno(
    @PrimaryKey val idAlumno: Long,
    @ColumnInfo(name = "cedula") var cedula: String,
    @ColumnInfo(name = "nombre") var nombre: String,
    @ColumnInfo(name = "telefono") var telefono: String,
    @ColumnInfo(name = "email") var email: String,
    @ColumnInfo(name = "fecha_nacimiento") var fechaNacimiento: String,
    @ColumnInfo(name = "pk_carrera") var pkCarrera: Long
)
