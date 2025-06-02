package com.example.gestionacademicaapp.ui.carreras

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.data.repository.CarreraRepository
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
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class) // Anotación para flatMapLatest
class CarrerasViewModel @Inject constructor(
    private val carreraRepository: CarreraRepository,
    private val carreraCursoRepository: CarreraCursoRepository
) : ViewModel() {

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    private val reloadTrigger = MutableStateFlow(0)

    fun fetchItems() = reloadTrigger.flatMapLatest {
        flow {
            emit(ListUiState.Loading)
            carreraRepository.listar()
                .onSuccess { emit(ListUiState.Success(it)) }
                .onFailure { emit(ListUiState.Error(it.toUserMessage())) }
        }.catch { emit(ListUiState.Error(it.message ?: "Error desconocido")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ListUiState.Loading)

    private fun triggerReload() {
        reloadTrigger.value += 1
    }

    fun createItem(carrera: Carrera) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraRepository.insertar(carrera)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Carrera creada exitosamente")
                    triggerReload()
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }

    fun updateItem(carrera: Carrera) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraRepository.modificar(carrera)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Carrera actualizada exitosamente")
                    triggerReload()
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            val tieneCursos = carreraCursoRepository.listar().getOrNull()
                ?.any { it.pkCarrera == id } == true
            if (tieneCursos) {
                _actionState.value = SingleUiState.Error("No se puede eliminar una carrera con cursos asignados.")
                return@launch
            }
            carreraRepository.eliminar(id)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Carrera eliminada exitosamente")
                    triggerReload()
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun createCarreraCurso(carreraCurso: CarreraCurso) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraCursoRepository.insertar(carreraCurso)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Curso agregado a la carrera")
                    triggerReload()
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }

    fun updateCarreraCurso(carreraCurso: CarreraCurso) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraCursoRepository.modificar(carreraCurso)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Orden del curso actualizado")
                    triggerReload()
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }

    fun deleteCarreraCurso(idCarrera: Long, idCurso: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraCursoRepository.eliminar(idCarrera, idCurso)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Curso eliminado de la carrera")
                    triggerReload()
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }
}
