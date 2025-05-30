package com.example.gestionacademicaapp.ui.common.state

sealed class SingleUiState<out T> {
    data object Loading : SingleUiState<Nothing>()
    data class Success<T>(val data: T) : SingleUiState<T>()
    data class Error(val message: String) : SingleUiState<Nothing>()
}
