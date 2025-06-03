package com.example.gestionacademicaapp.ui.profesores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.repository.ProfesorRepository
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProfesoresViewModel @Inject constructor(
    private val profesorRepository: ProfesorRepository
) : ViewModel() {

    private val _profesoresState = MutableLiveData<ListUiState<Profesor>>()
    val profesoresState: LiveData<ListUiState<Profesor>> get() = _profesoresState

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    init {
        fetchProfesores()
    }

    private fun fetchProfesores() {
        viewModelScope.launch {
            _profesoresState.value = ListUiState.Loading
            profesorRepository.listar()
                .onSuccess { lista ->
                    _profesoresState.value = ListUiState.Success(lista)
                }
                .onFailure { ex ->
                    _profesoresState.value = ListUiState.Error(ex.toUserMessage())
                }
        }
    }

    fun createProfesor(profesor: Profesor) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            profesorRepository.insertar(profesor)
                .onSuccess {
                    fetchProfesores()
                    _actionState.value = SingleUiState.Success("Profesor creado exitosamente")
                }
                .onFailure { ex ->
                    _actionState.value = SingleUiState.Error(ex.toUserMessage())
                }
        }
    }

    fun updateProfesor(profesor: Profesor) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            profesorRepository.modificar(profesor)
                .onSuccess {
                    fetchProfesores()
                    _actionState.value = SingleUiState.Success("Profesor actualizado exitosamente")
                }
                .onFailure { ex ->
                    _actionState.value = SingleUiState.Error(ex.toUserMessage())
                }
        }
    }

    fun deleteProfesor(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            profesorRepository.eliminar(id)
                .onSuccess {
                    fetchProfesores()
                    _actionState.value = SingleUiState.Success("Profesor eliminado exitosamente")
                }
                .onFailure { ex ->
                    _actionState.value = SingleUiState.Error(ex.toUserMessage())
                }
        }
    }
}
