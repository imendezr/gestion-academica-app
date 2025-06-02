package com.example.gestionacademicaapp.ui.cursos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class) // Anotación para flatMapLatest
class CursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val carreraCursoRepository: CarreraCursoRepository
) : ViewModel() {

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    private val reloadTrigger = MutableStateFlow(0)

    fun fetchItems() = reloadTrigger.flatMapLatest {
        flow {
            emit(ListUiState.Loading)
            cursoRepository.listar()
                .onSuccess { emit(ListUiState.Success(it)) }
                .onFailure { emit(ListUiState.Error(it.toUserMessage())) }
        }.catch { emit(ListUiState.Error(it.message ?: "Error desconocido")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ListUiState.Loading)

    private fun triggerReload() {
        reloadTrigger.value += 1
    }

    fun createItem(curso: Curso) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cursoRepository.insertar(curso)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Curso creado exitosamente")
                    triggerReload()
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun updateItem(curso: Curso) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cursoRepository.modificar(curso)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Curso actualizado exitosamente")
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
            val tieneCarreras = carreraCursoRepository.listar().getOrNull()
                ?.any { it.pkCurso == id } == true
            if (tieneCarreras) {
                _actionState.value = SingleUiState.Error("No se puede eliminar un curso asignado a una carrera.")
                return@launch
            }
            cursoRepository.eliminar(id)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Curso eliminado exitosamente")
                    triggerReload()
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }
}
