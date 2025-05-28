package com.example.gestionacademicaapp.data.api.model

import java.io.Serializable

data class Usuario(
    val idUsuario: Long,
    var cedula: String,
    var clave: String,
    var tipo: String
) : Serializable
