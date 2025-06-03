package com.example.gestionacademicaapp.utils

object Constants {
    /* BASE_URL para conectarse al backend:
     - 10.0.2.2 para emulador de Android Studio (equivalente a localhost).
     - Cambiar esta dirección cuando el backend esté desplegado en un servidor real. */
    const val BASE_URL = "http://10.0.2.2:8080/api/"
    const val PREFS_NAME = "GestionAcademicaPreferencias"
    const val USER_KEY = "usuario_actual"
}
