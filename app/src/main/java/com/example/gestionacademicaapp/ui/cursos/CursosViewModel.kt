package com.example.gestionacademicaapp.ui.cursos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val carreraCursoRepository: CarreraCursoRepository
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    val cursosState: StateFlow<UiState<List<Curso>>> = reloadTrigger.flatMapLatest {
        flow {
            emit(UiState.Loading)
            cursoRepository.listar()
                .onSuccess { emit(UiState.Success(data = it)) }
                .onFailure { emit(UiState.Error(it.toUserMessage(), mapErrorType(it))) }
        }.catch { emit(UiState.Error(it.message ?: "Error desconocido", ErrorType.GENERAL)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Success())
    val actionState: StateFlow<UiState<Unit>> get() = _actionState

    private fun triggerReload() {
        viewModelScope.launch { reloadTrigger.emit(Unit) }
    }

    fun createItem(curso: Curso) {
        performAction(
            validate = { validateCurso(curso) },
            action = { cursoRepository.insertar(curso) },
            successMessage = "Curso creado exitosamente"
        )
    }

    fun updateItem(curso: Curso) {
        performAction(
            validate = { validateCurso(curso) },
            action = { cursoRepository.modificar(curso) },
            successMessage = "Curso actualizado exitosamente"
        )
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            carreraCursoRepository.listar()
                .onSuccess { relaciones ->
                    if (relaciones.any { it.pkCurso == id }) {
                        _actionState.value = UiState.Error(
                            message = "No se puede eliminar un curso asignado a una carrera.",
                            type = ErrorType.DEPENDENCY
                        )
                        return@launch
                    }
                    cursoRepository.eliminar(id)
                        .onSuccess {
                            _actionState.value = UiState.Success(message = "Curso eliminado exitosamente")
                            triggerReload()
                        }
                        .onFailure {
                            _actionState.value = UiState.Error(it.toUserMessage(), mapErrorType(it))
                        }
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.toUserMessage(), mapErrorType(it))
                }
        }
    }

    private fun performAction(
        validate: () -> Result<Unit>,
        action: suspend () -> Result<Unit>,
        successMessage: String
    ) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            validate().onSuccess {
                action()
                    .onSuccess {
                        _actionState.value = UiState.Success(message = successMessage)
                        triggerReload()
                    }
                    .onFailure {
                        _actionState.value = UiState.Error(it.toUserMessage(), mapErrorType(it))
                    }
            }.onFailure {
                _actionState.value = UiState.Error(it.message ?: "Datos inválidos", ErrorType.VALIDATION)
            }
        }
    }

    private fun validateCurso(curso: Curso): Result<Unit> {
        return when {
            curso.codigo.isBlank() -> Result.failure(Exception("El código no puede estar vacío"))
            curso.nombre.isBlank() -> Result.failure(Exception("El nombre no puede estar vacío"))
            curso.creditos !in 1..10 -> Result.failure(Exception("Los créditos deben estar entre 1 y 10"))
            curso.horasSemanales !in 1..40 -> Result.failure(Exception("Las horas semanales deben estar entre 1 y 40"))
            else -> Result.success(Unit)
        }
    }

    private fun mapErrorType(throwable: Throwable): ErrorType {
        val message = throwable.toUserMessage().lowercase()
        return when {
            message.contains("carrera") || message.contains("grupos") -> ErrorType.DEPENDENCY
            message.contains("formato") || message.contains("inválido") -> ErrorType.VALIDATION
            else -> ErrorType.GENERAL
        }
    }
}
