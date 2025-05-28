package com.example.gestionacademicaapp.ui.common

data class CampoFormulario(
    val key: String,
    val label: String,
    val tipo: String = "text", // "text", "number", "select" (futuro)
    val obligatorio: Boolean = false
)
