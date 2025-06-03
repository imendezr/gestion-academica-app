package com.example.gestionacademicaapp.ui.ciclos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.repository.CicloRepository
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
class CiclosViewModel @Inject constructor(
    private val cicloRepository: CicloRepository
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    val ciclosState: StateFlow<UiState<List<Ciclo>>> = reloadTrigger.flatMapLatest {
        flow {
            emit(UiState.Loading)
            cicloRepository.listar()
                .onSuccess { emit(UiState.Success(data = it)) }
                .onFailure { emit(UiState.Error(it.toUserMessage(), mapErrorType(it))) }
        }.catch { emit(UiState.Error(it.message ?: "Error desconocido", ErrorType.GENERAL)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Success())
    val actionState: StateFlow<UiState<Unit>> get() = _actionState

    private fun triggerReload() {
        viewModelScope.launch { reloadTrigger.emit(Unit) }
    }

    fun createItem(ciclo: Ciclo) {
        viewModelScope.launch {
            performAction {
                cicloRepository.insertar(ciclo)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Ciclo creado exitosamente")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    fun updateItem(ciclo: Ciclo) {
        viewModelScope.launch {
            performAction {
                cicloRepository.modificar(ciclo)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Ciclo actualizado exitosamente")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            performAction {
                val result = cicloRepository.obtenerPorId(id)
                if (result.isFailure) {
                    throw Exception("Error al obtener el ciclo: ${result.exceptionOrNull()?.toUserMessage()}")
                }
                val ciclo = result.getOrNull() ?: throw Exception("El ciclo no existe.")
                if (ciclo.estado.equals("ACTIVO", ignoreCase = true)) {
                    throw Exception("No se puede eliminar un ciclo activo.")
                }
                cicloRepository.eliminar(id)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Ciclo eliminado exitosamente")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    fun activateCiclo(id: Long) {
        viewModelScope.launch {
            performAction {
                val result = cicloRepository.obtenerPorId(id)
                if (result.isFailure) {
                    throw Exception("Error al obtener el ciclo: ${result.exceptionOrNull()?.toUserMessage()}")
                }
                val ciclo = result.getOrNull() ?: throw Exception("El ciclo no existe.")
                if (ciclo.estado.equals("ACTIVO", ignoreCase = true)) {
                    throw Exception("El ciclo ya está activo.")
                }
                cicloRepository.activar(id)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Ciclo activado exitosamente")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
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
            message.contains("cursos asociados") -> ErrorType.DEPENDENCY
            message.contains("fecha") || message.contains("nulas") || message.contains("año") -> ErrorType.VALIDATION
            else -> ErrorType.GENERAL
        }
    }
}
