package com.example.gestionacademicaapp.ui.historial_academico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.data.repository.MatriculaRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class HistorialAcademicoViewModel @Inject constructor(
    private val matriculaRepository: MatriculaRepository
) : ViewModel() {

    private val _historialState = MutableStateFlow<UiState<List<MatriculaAlumnoDto>>>(UiState.Loading)
    val historialState: StateFlow<UiState<List<MatriculaAlumnoDto>>> = _historialState.asStateFlow()

    fun loadHistorial(cedula: String) {
        viewModelScope.launch {
            _historialState.value = UiState.Loading
            matriculaRepository.listarPorCedula(cedula)
                .onSuccess { matriculas ->
                    _historialState.value = UiState.Success(matriculas)
                }
                .onFailure { ex ->
                    _historialState.value = UiState.Error(
                        message = ex.toUserMessage(),
                        type = mapErrorType(ex)
                    )
                }
        }
    }

    private fun mapErrorType(exception: Throwable): ErrorType {
        return when {
            exception is HttpException -> when (exception.code()) {
                400 -> ErrorType.VALIDATION
                404 -> ErrorType.VALIDATION
                else -> ErrorType.GENERAL
            }
            else -> ErrorType.GENERAL
        }
    }
}
