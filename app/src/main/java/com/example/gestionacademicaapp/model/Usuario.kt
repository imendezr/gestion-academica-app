package com.example.gestionacademicaapp.model

import java.io.Serializable

data class Usuario(
    val id: Int,
    var username: String,
    var nombre: String,
    var correo: String,
    var password: String,
    var tipoUsuario: String // Ejemplo: "Alumno", "Profesor", "Administrador"
) : Serializable
