package com.example.gestionacademicaapp.ui.common

data class CampoFormulario(
    val key: String,
    val label: String,
    val tipo: String = "text", // "text", "number", "spinner", "date"
    val obligatorio: Boolean = false,
    val editable: Boolean = true,
    val opciones: List<Pair<String, String>> = emptyList(),
    val rules: ((String) -> String?)? = null,
    val onValueChanged: ((String) -> Unit)? = null // Callback para notificar cambios
)
