package com.example.gestionacademicaapp.ui.ciclos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.repository.CicloRepository
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CiclosViewModel @Inject constructor(
    private val cicloRepository: CicloRepository
) : ViewModel() {

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    private val reloadTrigger = MutableStateFlow(0)

    fun fetchItems() = reloadTrigger.flatMapLatest {
        flow {
            emit(ListUiState.Loading)
            cicloRepository.listar()
                .onSuccess { emit(ListUiState.Success(it)) }
                .onFailure { emit(ListUiState.Error(it.toUserMessage())) }
        }.catch { emit(ListUiState.Error(it.message ?: "Error desconocido")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ListUiState.Loading)

    private fun triggerReload() {
        reloadTrigger.value += 1
    }

    fun createItem(ciclo: Ciclo) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cicloRepository.insertar(ciclo)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Ciclo creado exitosamente")
                    triggerReload()
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun updateItem(ciclo: Ciclo) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cicloRepository.modificar(ciclo)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Ciclo actualizado exitosamente")
                    triggerReload()
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            val result = cicloRepository.obtenerPorId(id)
            if (result.isFailure) {
                _actionState.value = SingleUiState.Error("Error al obtener el ciclo: ${result.exceptionOrNull()?.toUserMessage()}")
                return@launch
            }

            val ciclo = result.getOrNull()
            if (ciclo == null) {
                _actionState.value = SingleUiState.Error("El ciclo no existe.")
                return@launch
            }

            if (ciclo.estado.equals("ACTIVO", ignoreCase = true)) {
                _actionState.value = SingleUiState.Error("No se puede eliminar un ciclo activo.")
                return@launch
            }

            cicloRepository.eliminar(id)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Ciclo eliminado exitosamente")
                    triggerReload()
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun searchCicloByAnio(anio: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cicloRepository.buscarPorAnio(anio)
                .onSuccess { ciclo ->
                    _actionState.value = SingleUiState.Success("Ciclo encontrado: ${ciclo.anio}-${ciclo.numero}")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun activateCiclo(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            // Validar que el ciclo exista antes de activarlo
            val result = cicloRepository.obtenerPorId(id)
            if (result.isFailure) {
                _actionState.value = SingleUiState.Error("Error al obtener el ciclo: ${result.exceptionOrNull()?.toUserMessage()}")
                return@launch
            }

            val ciclo = result.getOrNull()
            if (ciclo == null) {
                _actionState.value = SingleUiState.Error("El ciclo no existe.")
                return@launch
            }

            if (ciclo.estado.equals("ACTIVO", ignoreCase = true)) {
                _actionState.value = SingleUiState.Error("El ciclo ya está activo.")
                return@launch
            }

            cicloRepository.activar(id)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Ciclo activado exitosamente")
                    triggerReload()
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }
}
