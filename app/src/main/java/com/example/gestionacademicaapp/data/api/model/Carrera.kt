package com.example.gestionacademicaapp.data.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Carrera(
    val idCarrera: Long,
    var codigo: String,
    var nombre: String,
    var titulo: String
) : Parcelable
