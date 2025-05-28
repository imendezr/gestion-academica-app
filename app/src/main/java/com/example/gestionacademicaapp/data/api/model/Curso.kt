package com.example.gestionacademicaapp.data.api.model

data class Curso(
    val idCurso: Long,
    var codigo: String,
    var nombre: String,
    var creditos: Long,
    var horasSemanales: Long
)
