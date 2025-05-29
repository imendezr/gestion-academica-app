package com.example.gestionacademicaapp.ui.carreras

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.repository.CarreraRepository
import com.example.gestionacademicaapp.data.response.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarrerasViewModel @Inject constructor(
    private val carreraRepository: CarreraRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _carrerasState = MutableLiveData<CarrerasState>()
    val carrerasState: LiveData<CarrerasState> get() = _carrerasState

    private val _actionState = MutableLiveData<ActionState>()
    val actionState: LiveData<ActionState> get() = _actionState

    init {
        fetchCarreras()
    }

    fun fetchCarreras() {
        viewModelScope.launch {
            Log.d("CarrerasViewModel", "fetchCarreras called")
            _carrerasState.value = CarrerasState.Loading
            val response = carreraRepository.listar(context)
            _carrerasState.value = when (response) {
                is ApiResponse.Success -> CarrerasState.Success(response.data)
                is ApiResponse.Error -> CarrerasState.Error(response.message ?: "Error desconocido")
            }
        }
    }

    fun createCarrera(carrera: Carrera) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val response = carreraRepository.insertar(context, carrera)
            _actionState.value = when (response) {
                is ApiResponse.Success -> {
                    fetchCarreras()
                    ActionState.Success("Carrera creada exitosamente")
                }
                is ApiResponse.Error -> ActionState.Error(
                    response.message ?: "Error al crear carrera")
            }
        }
    }

    fun updateCarrera(carrera: Carrera) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val response = carreraRepository.modificar(context, carrera)
            _actionState.value = when (response) {
                is ApiResponse.Success -> {
                    fetchCarreras()
                    ActionState.Success("Carrera actualizada exitosamente")
                }
                is ApiResponse.Error -> ActionState.Error(
                    response.message ?: "Error al actualizar carrera")
            }
        }
    }

    fun deleteCarrera(id: Long) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val response = carreraRepository.eliminar(context, id)
            _actionState.value = when (response) {
                is ApiResponse.Success -> {
                    fetchCarreras()
                    ActionState.Success("Carrera eliminada exitosamente")
                }
                is ApiResponse.Error -> ActionState.Error(
                    response.message ?: "Error al eliminar carrera")
            }
        }
    }
}

sealed class CarrerasState {
    object Loading : CarrerasState()
    data class Success(val carreras: List<Carrera>) : CarrerasState()
    data class Error(val message: String) : CarrerasState()
}

sealed class ActionState {
    object Loading : ActionState()
    data class Success(val message: String) : ActionState()
    data class Error(val message: String) : ActionState()
}
