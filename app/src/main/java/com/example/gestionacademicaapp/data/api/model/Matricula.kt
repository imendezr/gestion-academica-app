package com.example.gestionacademicaapp.data.api.model

data class Matricula(
    val idMatricula: Long,
    var pkAlumno: Long,
    var pkGrupo: Long,
    var nota: Long
)
