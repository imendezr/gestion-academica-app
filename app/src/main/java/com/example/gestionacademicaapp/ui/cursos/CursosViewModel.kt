package com.example.gestionacademicaapp.ui.cursos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository
) : ViewModel() {

    private val _cursosState = MutableLiveData<CursosState>()
    val cursosState: LiveData<CursosState> get() = _cursosState

    private val _actionState = MutableLiveData<ActionState>()
    val actionState: LiveData<ActionState> get() = _actionState

    init {
        fetchCursos()
    }

    fun fetchCursos() {
        viewModelScope.launch {
            _cursosState.value = CursosState.Loading
            cursoRepository.listar()
                .onSuccess { _cursosState.value = CursosState.Success(it) }
                .onFailure { _cursosState.value = CursosState.Error(it.toUserMessage()) }
        }
    }

    fun createCurso(curso: Curso) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            cursoRepository.insertar(curso)
                .onSuccess {
                    fetchCursos()
                    _actionState.value = ActionState.Success("Curso creado exitosamente")
                }
                .onFailure {
                    _actionState.value = ActionState.Error(it.toUserMessage())
                }
        }
    }

    fun updateCurso(curso: Curso) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            cursoRepository.modificar(curso)
                .onSuccess {
                    fetchCursos()
                    _actionState.value = ActionState.Success("Curso actualizado exitosamente")
                }
                .onFailure {
                    _actionState.value = ActionState.Error(it.toUserMessage())
                }
        }
    }

    fun deleteCurso(id: Long) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            cursoRepository.eliminar(id)
                .onSuccess {
                    fetchCursos()
                    _actionState.value = ActionState.Success("Curso eliminado exitosamente")
                }
                .onFailure {
                    _actionState.value = ActionState.Error(it.toUserMessage())
                }
        }
    }
}

sealed class CursosState {
    object Loading : CursosState()
    data class Success(val cursos: List<Curso>) : CursosState()
    data class Error(val message: String) : CursosState()
}

sealed class ActionState {
    object Loading : ActionState()
    data class Success(val message: String) : ActionState()
    data class Error(val message: String) : ActionState()
}
