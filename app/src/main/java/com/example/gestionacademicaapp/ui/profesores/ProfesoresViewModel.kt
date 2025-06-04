package com.example.gestionacademicaapp.ui.profesores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.repository.ProfesorRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class ProfesoresViewModel @Inject constructor(
    private val profesorRepository: ProfesorRepository
) : ViewModel() {

    private val _profesoresState = MutableStateFlow<UiState<List<Profesor>>>(UiState.Loading)
    val profesoresState: StateFlow<UiState<List<Profesor>>> = _profesoresState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val actionState: StateFlow<UiState<Unit>> = _actionState.asStateFlow()

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    init {
        viewModelScope.launch {
            reloadTrigger.collect { fetchProfesores() }
        }
    }

    private fun fetchProfesores() {
        viewModelScope.launch {
            _profesoresState.value = UiState.Loading
            profesorRepository.listar()
                .onSuccess { lista ->
                    _profesoresState.value = UiState.Success(lista)
                }
                .onFailure { ex ->
                    _profesoresState.value = UiState.Error(
                        message = ex.toUserMessage(),
                        type = mapErrorType(ex)
                    )
                }
        }
    }

    fun createProfesor(profesor: Profesor) {
        viewModelScope.launch {
            performAction {
                validateProfesor(profesor)
                profesorRepository.insertar(profesor)
                    .onSuccess {
                        reloadTrigger.tryEmit(Unit)
                        _actionState.value = UiState.Success(message = "Profesor creado exitosamente")
                    }
                    .onFailure { ex ->
                        _actionState.value = UiState.Error(
                            message = ex.toUserMessage(),
                            type = mapErrorType(ex)
                        )
                    }
            }
        }
    }

    fun updateProfesor(profesor: Profesor) {
        viewModelScope.launch {
            performAction {
                validateProfesor(profesor)
                profesorRepository.modificar(profesor)
                    .onSuccess {
                        reloadTrigger.tryEmit(Unit)
                        _actionState.value = UiState.Success(message = "Profesor actualizado exitosamente")
                    }
                    .onFailure { ex ->
                        _actionState.value = UiState.Error(
                            message = ex.toUserMessage(),
                            type = mapErrorType(ex)
                        )
                    }
            }
        }
    }

    private fun validateProfesor(profesor: Profesor) {
        ProfesorValidator.validateCedula(profesor.cedula)?.let { throw IllegalArgumentException(it) }
        ProfesorValidator.validateNombre(profesor.nombre)?.let { throw IllegalArgumentException(it) }
        ProfesorValidator.validateTelefono(profesor.telefono)?.let { throw IllegalArgumentException(it) }
        ProfesorValidator.validateEmail(profesor.email)?.let { throw IllegalArgumentException(it) }
    }

    private fun performAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                action()
            } catch (e: Exception) {
                _actionState.value = UiState.Error(
                    message = e.toUserMessage(),
                    type = mapErrorType(e)
                )
            }
        }
    }

    private fun mapErrorType(exception: Throwable): ErrorType {
        return when {
            exception is IllegalArgumentException -> ErrorType.VALIDATION
            exception is HttpException -> when (exception.code()) {
                400 -> ErrorType.VALIDATION
                409 -> ErrorType.DEPENDENCY
                else -> ErrorType.GENERAL
            }
            exception.message?.contains("20010") == true -> ErrorType.DEPENDENCY // User associated
            exception.message?.contains("20024") == true -> ErrorType.VALIDATION // Empty name
            exception.message?.contains("20025") == true -> ErrorType.VALIDATION // Invalid email
            exception.message?.contains("20030") == true -> ErrorType.DEPENDENCY // Groups assigned
            exception.message?.contains("20038") == true -> ErrorType.VALIDATION // Invalid cedula
            exception.message?.contains("20039") == true -> ErrorType.VALIDATION // Invalid phone
            else -> ErrorType.GENERAL
        }
    }
}
