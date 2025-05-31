package com.example.gestionacademicaapp.data.api.model.support

import com.example.gestionacademicaapp.data.api.model.Usuario

data class NuevoUsuario(
    val usuario: Usuario,
    val profesorData: ProfesorData? = null,
    val alumnoData: AlumnoData? = null
)
