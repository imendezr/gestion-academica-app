package com.example.gestionacademicaapp.ui.cursos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository
) : ViewModel() {

    private val _cursosState = MutableLiveData<ListUiState<Curso>>()
    val cursosState: LiveData<ListUiState<Curso>> get() = _cursosState

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    init {
        fetchCursos()
    }

    fun fetchCursos() {
        viewModelScope.launch {
            _cursosState.value = ListUiState.Loading
            cursoRepository.listar()
                .onSuccess { _cursosState.value = ListUiState.Success(it) }
                .onFailure { _cursosState.value = ListUiState.Error(it.toUserMessage()) }
        }
    }

    fun createCurso(curso: Curso) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cursoRepository.insertar(curso)
                .onSuccess {
                    fetchCursos()
                    _actionState.value = SingleUiState.Success("Curso creado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun updateCurso(curso: Curso) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cursoRepository.modificar(curso)
                .onSuccess {
                    fetchCursos()
                    _actionState.value = SingleUiState.Success("Curso actualizado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun deleteCurso(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            cursoRepository.eliminar(id)
                .onSuccess {
                    fetchCursos()
                    _actionState.value = SingleUiState.Success("Curso eliminado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }
}
