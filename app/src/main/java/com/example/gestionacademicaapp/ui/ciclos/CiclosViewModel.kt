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
import kotlinx.coroutines.launch

@HiltViewModel
class CiclosViewModel @Inject constructor(
    private val cicloRepository: CicloRepository
) : ViewModel() {

    private val _ciclosState = MutableLiveData<ListUiState<Ciclo>>()
    val ciclosState: LiveData<ListUiState<Ciclo>> get() = _ciclosState

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    init {
        fetchCiclos()
    }

    fun fetchCiclos() {
        viewModelScope.launch {
            _ciclosState.value = ListUiState.Loading
            cicloRepository.listar()
                .onSuccess { _ciclosState.value = ListUiState.Success(it) }
                .onFailure { _ciclosState.value = ListUiState.Error(it.toUserMessage()) }
        }
    }

    fun createCiclo(ciclo: Ciclo) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cicloRepository.insertar(ciclo)
                .onSuccess {
                    fetchCiclos()
                    _actionState.value = SingleUiState.Success("Ciclo creado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun updateCiclo(ciclo: Ciclo) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cicloRepository.modificar(ciclo)
                .onSuccess {
                    fetchCiclos()
                    _actionState.value = SingleUiState.Success("Ciclo actualizado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun deleteCiclo(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading

            // Buscar el ciclo a eliminar desde la lista actual
            val cicloActivo = _ciclosState.value
                .takeIf { it is ListUiState.Success }
                ?.let { (it as ListUiState.Success).data.firstOrNull { ciclo -> ciclo.idCiclo == id } }

            // Validar si es activo
            if (cicloActivo?.estado?.equals("ACTIVO", ignoreCase = true) == true) {
                _actionState.value =
                    SingleUiState.Error("No se puede eliminar un ciclo activo.")
                return@launch
            }

            cicloRepository.eliminar(id)
                .onSuccess {
                    fetchCiclos()
                    _actionState.value = SingleUiState.Success("Ciclo eliminado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun searchCicloByAnio(anio: Int) {
        viewModelScope.launch {
            _ciclosState.value = ListUiState.Loading
            cicloRepository.buscarPorAnio(anio)
                .onSuccess { ciclo ->
                    _ciclosState.value = ListUiState.Success(listOf(ciclo))
                }
                .onFailure {
                    _ciclosState.value = ListUiState.Error(it.toUserMessage())
                }
        }
    }

    fun activateCiclo(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cicloRepository.activar(id)
                .onSuccess {
                    fetchCiclos()
                    _actionState.value = SingleUiState.Success("Ciclo activado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }
}
