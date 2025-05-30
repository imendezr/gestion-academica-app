package com.example.gestionacademicaapp.ui.common.state

sealed class ListUiState<out T> {
    data object Loading : ListUiState<Nothing>()
    data class Success<T>(val data: List<T>) : ListUiState<T>()
    data class Error(val message: String) : ListUiState<Nothing>()
}
