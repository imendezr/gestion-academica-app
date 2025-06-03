package com.example.gestionacademicaapp.data.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    val idUsuario: Long,
    var cedula: String,
    var clave: String,
    var tipo: String
) : Parcelable
