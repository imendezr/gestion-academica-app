package com.example.gestionacademicaapp.ui.carreras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.data.repository.CarreraRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
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


@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CarrerasViewModel @Inject constructor(
    private val carreraRepository: CarreraRepository,
    private val carreraCursoRepository: CarreraCursoRepository
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Success())
    val actionState: StateFlow<UiState<Unit>> get() = _actionState

    fun fetchItems() = reloadTrigger.flatMapLatest {
        flow {
            emit(UiState.Loading)
            carreraRepository.listar()
                .onSuccess { emit(UiState.Success(it)) }
                .onFailure { throw it }
        }.catch { emit(UiState.Error(it.toUserMessage(), mapErrorType(it))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private fun triggerReload() {
        viewModelScope.launch { reloadTrigger.emit(Unit) }
    }

    fun createItem(carrera: Carrera) {
        viewModelScope.launch {
            performAction {
                validateCarrera(carrera)
                carreraRepository.insertar(carrera)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Carrera creada exitosamente")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    fun updateItem(carrera: Carrera) {
        viewModelScope.launch {
            performAction {
                validateCarrera(carrera)
                carreraRepository.modificar(carrera)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Carrera actualizada exitosamente")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            performAction {
                val tieneCursos = carreraCursoRepository.listar().getOrNull()
                    ?.any { it.pkCarrera == id } == true
                if (tieneCursos) {
                    throw IllegalStateException("No se puede eliminar: la carrera tiene cursos asignados")
                }
                carreraRepository.eliminar(id)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Carrera eliminada exitosamente")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    private fun validateCarrera(carrera: Carrera) {
        if (carrera.codigo.isBlank()) throw IllegalArgumentException("El código es requerido")
        if (carrera.nombre.isBlank()) throw IllegalArgumentException("El nombre es requerido")
        if (carrera.titulo.isBlank()) throw IllegalArgumentException("El título es requerido")
        if (carrera.codigo.length !in 3..10) throw IllegalArgumentException("El código debe tener entre 3 y 10 caracteres")
        if (carrera.nombre.length < 5) throw IllegalArgumentException("El nombre debe tener al menos 5 caracteres")
        if (carrera.titulo.length < 5) throw IllegalArgumentException("El título debe tener al menos 5 caracteres")
    }

    private suspend fun performAction(action: suspend () -> Unit) {
        _actionState.value = UiState.Loading
        try {
            action()
        } catch (e: Exception) {
            _actionState.value = UiState.Error(e.toUserMessage(), mapErrorType(e))
        }
    }

    private fun mapErrorType(throwable: Throwable): ErrorType {
        val message = throwable.toUserMessage().lowercase()
        return when {
            message.contains("cursos asociados") || message.contains("alumnos inscritos") -> ErrorType.DEPENDENCY
            message.contains("requerido") || message.contains("caracteres") ||
                    message.contains("duplicado") || message.contains("no existe") -> ErrorType.VALIDATION
            else -> ErrorType.GENERAL
        }
    }
}
