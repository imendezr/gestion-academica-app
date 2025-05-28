package com.example.gestionacademicaapp.data.api.model

data class Grupo(
    val idGrupo: Long,
    var pkCarreraCurso: Long,
    var numeroGrupo: Long,
    var horario: String,
    var pkProfesor: Long
)
