package com.example.gestionacademicaapp.ui.cursos

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.data.response.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    @ApplicationContext private val context: Context
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
            val response = cursoRepository.listar(context)
            _cursosState.value = when (response) {
                is ApiResponse.Success -> CursosState.Success(response.data)
                is ApiResponse.Error -> CursosState.Error(response.message ?: "Error desconocido")
            }
        }
    }

    fun createCurso(curso: Curso) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val response = cursoRepository.insertar(context, curso)
            _actionState.value = when (response) {
                is ApiResponse.Success -> {
                    fetchCursos() // Actualizar lista
                    ActionState.Success("Curso creado exitosamente")
                }
                is ApiResponse.Error -> ActionState.Error(response.message ?: "Error al crear curso")
            }
        }
    }

    fun updateCurso(curso: Curso) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val response = cursoRepository.modificar(context, curso)
            _actionState.value = when (response) {
                is ApiResponse.Success -> {
                    fetchCursos() // Actualizar lista
                    ActionState.Success("Curso actualizado exitosamente")
                }
                is ApiResponse.Error -> ActionState.Error(response.message ?: "Error al actualizar curso")
            }
        }
    }

    fun deleteCurso(id: Long) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val response = cursoRepository.eliminar(context, id)
            _actionState.value = when (response) {
                is ApiResponse.Success -> {
                    fetchCursos() // Actualizar lista
                    ActionState.Success("Curso eliminado exitosamente")
                }
                is ApiResponse.Error -> ActionState.Error(response.message ?: "Error al eliminar curso")
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
