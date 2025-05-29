package com.example.gestionacademicaapp.data.api.model

data class Grupo(
    val idGrupo: Long,
    var idCarreraCurso: Long,
    var numeroGrupo: Long,
    var horario: String,
    var idProfesor: Long
)
