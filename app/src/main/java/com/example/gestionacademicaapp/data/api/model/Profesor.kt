package com.example.gestionacademicaapp.data.api.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profesor(
    val idProfesor: Long,
    var cedula: String,
    var nombre: String,
    var telefono: String,
    var email: String
): Parcelable
