package com.example.gestionacademicaapp.ui.alumnos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.repository.AlumnoRepository
import com.example.gestionacademicaapp.data.repository.CarreraRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import android.util.Log
import com.example.gestionacademicaapp.ui.common.validators.AlumnoValidator

@HiltViewModel
class AlumnosViewModel @Inject constructor(
    private val alumnoRepository: AlumnoRepository,
    private val carreraRepository: CarreraRepository
) : ViewModel() {

    private val _carrerasState = MutableStateFlow<List<Carrera>>(emptyList())
    val carrerasState: StateFlow<List<Carrera>> = _carrerasState.asStateFlow()

    private val _alumnosState = MutableStateFlow<UiState<List<Alumno>>>(UiState.Loading)
    val alumnosState: StateFlow<UiState<List<Alumno>>> = _alumnosState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val actionState: StateFlow<UiState<Unit>> = _actionState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            Log.d("AlumnosViewModel", "Loading initial data")
            // Load carreras
            carreraRepository.listar()
                .onSuccess { carreras ->
                    _carrerasState.value = carreras
                    Log.d("AlumnosViewModel", "Carreras loaded: ${carreras.size}")
                }
                .onFailure { ex ->
                    _carrerasState.value = emptyList()
                    Log.e("AlumnosViewModel", "Error loading carreras: ${ex.message}")
                }
            // Load alumnos
            fetchAlumnos()
        }
    }

    private fun fetchAlumnos() {
        viewModelScope.launch {
            Log.d("AlumnosViewModel", "Fetching alumnos")
            _alumnosState.value = UiState.Loading
            alumnoRepository.listar()
                .onSuccess { lista ->
                    _alumnosState.value = UiState.Success(lista)
                    Log.d("AlumnosViewModel", "Alumnos loaded: ${lista.size}")
                }
                .onFailure { ex ->
                    _alumnosState.value = UiState.Error(
                        message = ex.toUserMessage(),
                        type = mapErrorType(ex)
                    )
                    Log.e("AlumnosViewModel", "Error loading alumnos: ${ex.message}")
                }
        }
    }

    fun createAlumno(alumno: Alumno) {
        viewModelScope.launch {
            performAction {
                validateAlumno(alumno)
                alumnoRepository.insertar(alumno)
                    .onSuccess {
                        fetchAlumnos()
                        _actionState.value = UiState.Success(message = "Alumno creado exitosamente")
                        Log.d("AlumnosViewModel", "Alumno created successfully")
                    }
                    .onFailure { ex ->
                        _actionState.value = UiState.Error(
                            message = ex.toUserMessage(),
                            type = mapErrorType(ex)
                        )
                        Log.e("AlumnosViewModel", "Error creating alumno: ${ex.message}")
                    }
            }
        }
    }

    fun updateAlumno(alumno: Alumno) {
        viewModelScope.launch {
            performAction {
                validateAlumno(alumno)
                alumnoRepository.modificar(alumno)
                    .onSuccess {
                        fetchAlumnos()
                        _actionState.value = UiState.Success(message = "Alumno actualizado exitosamente")
                        Log.d("AlumnosViewModel", "Alumno updated successfully")
                    }
                    .onFailure { ex ->
                        _actionState.value = UiState.Error(
                            message = ex.toUserMessage(),
                            type = mapErrorType(ex)
                        )
                        Log.e("AlumnosViewModel", "Error updating alumno: ${ex.message}")
                    }
            }
        }
    }

    private fun validateAlumno(alumno: Alumno) {
        AlumnoValidator.validateCedula(alumno.cedula)?.let { throw IllegalArgumentException(it) }
        AlumnoValidator.validateNombre(alumno.nombre)?.let { throw IllegalArgumentException(it) }
        AlumnoValidator.validateTelefono(alumno.telefono)?.let { throw IllegalArgumentException(it) }
        AlumnoValidator.validateEmail(alumno.email)?.let { throw IllegalArgumentException(it) }
        AlumnoValidator.validateFechaNacimiento(alumno.fechaNacimiento)?.let { throw IllegalArgumentException(it) }
        AlumnoValidator.validateCarrera(alumno.pkCarrera.toString(), _carrerasState.value)?.let { throw IllegalArgumentException(it) }
    }

    private fun performAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                action()
            } catch (e: Exception) {
                _actionState.value = UiState.Error(
                    message = e.toUserMessage(),
                    type = mapErrorType(e)
                )
                Log.e("AlumnosViewModel", "Action error: ${e.message}")
            }
        }
    }

    private fun mapErrorType(exception: Throwable): ErrorType {
        return when {
            exception is IllegalArgumentException -> ErrorType.VALIDATION
            exception is HttpException -> when (exception.code()) {
                400 -> ErrorType.VALIDATION
                409 -> ErrorType.DEPENDENCY
                else -> ErrorType.GENERAL
            }
            exception.message?.contains("20010") == true -> ErrorType.DEPENDENCY
            exception.message?.contains("20024") == true -> ErrorType.VALIDATION
            exception.message?.contains("20025") == true -> ErrorType.VALIDATION
            exception.message?.contains("20030") == true -> ErrorType.DEPENDENCY
            exception.message?.contains("20038") == true -> ErrorType.VALIDATION
            exception.message?.contains("20039") == true -> ErrorType.VALIDATION
            else -> ErrorType.GENERAL
        }
    }
}
