package com.example.gestionacademicaapp.ui.common.state

/**
 * Representa el estado de la UI para cualquier operación o datos.
 * @param T Tipo de datos asociado al estado de éxito.
 */
sealed class UiState<out T> {
    /**
     * Estado de carga.
     */
    data object Loading : UiState<Nothing>()

    /**
     * Estado de éxito, con datos opcionales y un mensaje opcional.
     * @param data Datos asociados al éxito (e.g., List<Curso>, Curso, Unit).
     * @param message Mensaje opcional para la UI (e.g., "Curso creado").
     */
    data class Success<out T>(val data: T? = null, val message: String? = null) : UiState<T>()

    /**
     * Estado de error, con mensaje y tipo de error.
     * @param message Mensaje descriptivo del error.
     * @param type Tipo de error para manejo específico en la UI.
     */
    data class Error(val message: String, val type: ErrorType = ErrorType.GENERAL) : UiState<Nothing>()
}

/**
 * Tipos de error para diferenciar el manejo en la UI.
 */
enum class ErrorType {
    /**
     * Error de validación (e.g., campo vacío, formato inválido).
     */
    VALIDATION,

    /**
     * Error por dependencias (e.g., curso asignado a carrera, usuario con matrículas).
     */
    DEPENDENCY,

    /**
     * Error genérico (e.g., fallo de red, excepción inesperada).
     */
    GENERAL
}
