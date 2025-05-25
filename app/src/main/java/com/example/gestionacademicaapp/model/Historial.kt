package com.example.gestionacademicaapp.model

data class Historial(
    val id: Int,
    var alumnoId: Int,
    var ciclo: String,
    var curso: String,
    var nota: Double,
    var fecha: String
)
