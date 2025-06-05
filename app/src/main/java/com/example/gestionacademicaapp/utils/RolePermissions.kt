package com.example.gestionacademicaapp.utils

import com.example.gestionacademicaapp.R

object RolePermissions {
    // Mapa de destinos a roles permitidos
    val DESTINATION_ROLES = mapOf(
        R.id.nav_inicio to emptyList(), // Accesible para todos los autenticados
        R.id.nav_cursos to listOf("Administrador"),
        R.id.nav_carreras to listOf("Administrador"),
        R.id.nav_profesores to listOf("Administrador"),
        R.id.nav_alumnos to listOf("Administrador"),
        R.id.nav_ciclos to listOf("Administrador"),
        R.id.nav_ofertaAcademica to listOf("Administrador"),
        R.id.nav_usuarios to listOf("Administrador"),
        R.id.nav_matricula to listOf("Matriculador"),
        R.id.nav_notas to listOf("Profesor"),
        R.id.nav_historial to listOf("Administrador", "Alumno"),
        R.id.nav_perfil to emptyList() // Accesible para todos los autenticados
    )

    // Destino por defecto si no se tiene acceso
    val DEFAULT_DESTINATION = R.id.nav_inicio
}
