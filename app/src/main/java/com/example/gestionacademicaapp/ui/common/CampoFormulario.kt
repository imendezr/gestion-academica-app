package com.example.gestionacademicaapp.ui.common

/**
 * Representa un campo de un formulario genérico.
 *
 * @property key Identificador único del campo.
 * @property label Etiqueta visible para el usuario.
 * @property tipo Tipo de campo.
 * @property obligatorio Indica si el campo es requerido.
 * @property editable Indica si el campo puede editarse.
 * @property opciones Lista de opciones para campos tipo spinner (clave, valor visible).
 * @property obligatorioError Mensaje de error personalizado para campos obligatorios vacíos (opcional).
 * @property rules Función de validación que devuelve un mensaje de error o null si es válido.
 * @property onValueChanged Callback invocado cuando el valor del campo cambia.
 */
data class CampoFormulario(
    val key: String,
    val label: String,
    val tipo: CampoTipo = CampoTipo.TEXT,
    val obligatorio: Boolean = false,
    val editable: Boolean = true,
    val opciones: List<Pair<String, String>> = emptyList(),
    val obligatorioError: String? = null,
    val rules: ((String, Map<String, String>) -> String?)? = null,
    val onValueChanged: ((String) -> Unit)? = null
)

/**
 * Tipos de campos soportados por el formulario.
 */
enum class CampoTipo {
    TEXT, NUMBER, SPINNER, DATE
}
