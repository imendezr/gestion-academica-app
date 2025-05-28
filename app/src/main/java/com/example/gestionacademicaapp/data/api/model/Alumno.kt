package com.example.gestionacademicaapp.data.api.model

data class Alumno(
    val idAlumno: Long,
    var cedula: String,
    var nombre: String,
    var telefono: String,
    var email: String,
    var fechaNacimiento: String, // usar String o LocalDate según cómo se maneje el parsing JSON
    var pkCarrera: Long
)
