package com.example.gestionacademicaapp.data.api.model

data class Ciclo(
    val idCiclo: Long,
    var anio: Long,
    var numero: Long,
    var fechaInicio: String,
    var fechaFin: String,
    var estado: String
)
