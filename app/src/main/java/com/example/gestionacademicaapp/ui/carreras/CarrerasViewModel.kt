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
import kotlinx.coroutines.launch


@HiltViewModel
class CarrerasViewModel @Inject constructor(
    private val carreraRepository: CarreraRepository,
    private val carreraCursoRepository: CarreraCursoRepository
) : ViewModel() {

    private val _carrerasState = MutableLiveData<ListUiState<Carrera>>()
    val carrerasState: LiveData<ListUiState<Carrera>> get() = _carrerasState

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    init {
        fetchCarreras()
    }

    private fun fetchCarreras() {
        viewModelScope.launch {
            _carrerasState.value = ListUiState.Loading
            carreraRepository.listar()
                .onSuccess { _carrerasState.value = ListUiState.Success(it) }
                .onFailure { _carrerasState.value = ListUiState.Error(it.toUserMessage()) }
        }
    }

    fun createCarrera(carrera: Carrera) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraRepository.insertar(carrera)
                .onSuccess {
                    fetchCarreras()
                    _actionState.value = SingleUiState.Success("Carrera creada exitosamente")
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }

    fun updateCarrera(carrera: Carrera) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraRepository.modificar(carrera)
                .onSuccess {
                    fetchCarreras()
                    _actionState.value = SingleUiState.Success("Carrera actualizada exitosamente")
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }

    fun deleteCarrera(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading

            // Validación antes de eliminar
            val tieneCursos = carreraCursoRepository.listar().getOrNull()
                ?.any { it.pkCarrera == id } == true

            if (tieneCursos) {
                _actionState.value =
                    SingleUiState.Error("No se puede eliminar una carrera con cursos asignados.")
                return@launch
            }

            carreraRepository.eliminar(id)
                .onSuccess {
                    fetchCarreras()
                    _actionState.value = SingleUiState.Success("Carrera eliminada exitosamente")
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
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }
}
