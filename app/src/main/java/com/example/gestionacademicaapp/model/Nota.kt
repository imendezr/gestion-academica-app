package com.example.gestionacademicaapp.model

data class Nota(
    val id: Int,
    var alumnoId: Int,
    var curso: String,
    var nota: Double
)
