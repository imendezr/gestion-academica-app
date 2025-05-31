package com.example.gestionacademicaapp.ui.usuarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.api.model.support.AlumnoData
import com.example.gestionacademicaapp.data.api.model.support.NuevoUsuario
import com.example.gestionacademicaapp.data.api.model.support.ProfesorData
import com.example.gestionacademicaapp.data.repository.AlumnoRepository
import com.example.gestionacademicaapp.data.repository.ProfesorRepository
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val profesorRepository: ProfesorRepository,
    private val alumnoRepository: AlumnoRepository
) : ViewModel() {

    private val _usuariosState = MutableLiveData<ListUiState<Usuario>>()
    val usuariosState: LiveData<ListUiState<Usuario>> get() = _usuariosState

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    private val _showDialogEvent = MutableStateFlow<Event<Usuario?>?>(null)
    val showDialogEvent: StateFlow<Event<Usuario?>?> get() = _showDialogEvent.asStateFlow()

    var idUsuarioActual: Long = -1L

    init {
        fetchUsuarios()
    }

    private fun fetchUsuarios() {
        viewModelScope.launch {
            _usuariosState.value = ListUiState.Loading
            usuarioRepository.listar()
                .onSuccess { _usuariosState.value = ListUiState.Success(it) }
                .onFailure { _usuariosState.value = ListUiState.Error(it.toUserMessage()) }
        }
    }

    fun createUsuario(nuevoUsuario: NuevoUsuario) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            val usuario = nuevoUsuario.usuario

            usuarioRepository.insertar(usuario)
                .onSuccess {
                    when (usuario.tipo) {
                        "Profesor" -> {
                            nuevoUsuario.profesorData?.let { data ->
                                val profesor = Profesor(
                                    idProfesor = 0,
                                    cedula = usuario.cedula,
                                    nombre = data.nombre,
                                    telefono = data.telefono,
                                    email = data.email
                                )
                                profesorRepository.insertar(profesor)
                                    .onSuccess {
                                        fetchUsuarios()
                                        _actionState.value = SingleUiState.Success("Usuario y Profesor creados exitosamente")
                                    }
                                    .onFailure {
                                        _actionState.value = SingleUiState.Error(it.toUserMessage())
                                    }
                            }
                        }
                        "Alumno" -> {
                            nuevoUsuario.alumnoData?.let { data ->
                                val alumno = Alumno(
                                    idAlumno = 0,
                                    cedula = usuario.cedula,
                                    nombre = data.nombre,
                                    telefono = data.telefono,
                                    email = data.email,
                                    fechaNacimiento = data.fechaNacimiento,
                                    pkCarrera = data.pkCarrera
                                )
                                alumnoRepository.insertar(alumno)
                                    .onSuccess {
                                        fetchUsuarios()
                                        _actionState.value = SingleUiState.Success("Usuario y Alumno creados exitosamente")
                                    }
                                    .onFailure {
                                        _actionState.value = SingleUiState.Error(it.toUserMessage())
                                    }
                            }
                        }
                        else -> {
                            fetchUsuarios()
                            _actionState.value = SingleUiState.Success("Usuario creado exitosamente")
                        }
                    }
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun updateUsuario(usuario: Usuario, profesorData: ProfesorData?, alumnoData: AlumnoData?) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading

            val usuarioFinal = if (usuario.idUsuario == idUsuarioActual) {
                usuario.copy(clave = "")
            } else if (usuario.clave?.isBlank() != false) {
                usuario.copy(clave = "")
            } else {
                usuario
            }

            usuarioRepository.modificar(usuarioFinal)
                .onSuccess {
                    when (usuario.tipo) {
                        "Profesor" -> {
                            alumnoRepository.buscarPorCedula(usuario.cedula)
                                .onSuccess { alumno -> alumnoRepository.eliminar(alumno.idAlumno) }
                            profesorData?.let { data ->
                                val profesor = Profesor(
                                    idProfesor = 0,
                                    cedula = usuario.cedula,
                                    nombre = data.nombre,
                                    telefono = data.telefono,
                                    email = data.email
                                )
                                profesorRepository.buscarPorCedula(usuario.cedula)
                                    .onSuccess { existingProfesor ->
                                        profesorRepository.modificar(profesor.copy(idProfesor = existingProfesor.idProfesor))
                                    }
                                    .onFailure { profesorRepository.insertar(profesor) }
                            }
                        }
                        "Alumno" -> {
                            profesorRepository.buscarPorCedula(usuario.cedula)
                                .onSuccess { profesor -> profesorRepository.eliminar(profesor.idProfesor) }
                            alumnoData?.let { data ->
                                val alumno = Alumno(
                                    idAlumno = 0,
                                    cedula = usuario.cedula,
                                    nombre = data.nombre,
                                    telefono = data.telefono,
                                    email = data.email,
                                    fechaNacimiento = data.fechaNacimiento,
                                    pkCarrera = data.pkCarrera
                                )
                                alumnoRepository.buscarPorCedula(usuario.cedula)
                                    .onSuccess { existingAlumno ->
                                        alumnoRepository.modificar(alumno.copy(idAlumno = existingAlumno.idAlumno))
                                    }
                                    .onFailure { alumnoRepository.insertar(alumno) }
                            }
                        }
                        else -> {
                            profesorRepository.buscarPorCedula(usuario.cedula)
                                .onSuccess { profesor -> profesorRepository.eliminar(profesor.idProfesor) }
                            alumnoRepository.buscarPorCedula(usuario.cedula)
                                .onSuccess { alumno -> alumnoRepository.eliminar(alumno.idAlumno) }
                        }
                    }
                    fetchUsuarios()
                    _actionState.value = SingleUiState.Success("Usuario actualizado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun deleteUsuario(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading

            if (id == idUsuarioActual) {
                _actionState.value = SingleUiState.Error("No puedes eliminar tu propia cuenta mientras estás conectado.")
                return@launch
            }

            usuarioRepository.eliminar(id)
                .onSuccess {
                    fetchUsuarios()
                    _actionState.value = SingleUiState.Success("Usuario eliminado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun validarClaveActual(cedula: String, clave: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            usuarioRepository.login(cedula, clave)
                .onSuccess { callback(true) }
                .onFailure { callback(false) }
        }
    }

    fun searchUsuarioByCedula(cedula: String) {
        viewModelScope.launch {
            _usuariosState.value = ListUiState.Loading
            usuarioRepository.buscarPorCedula(cedula)
                .onSuccess { usuario ->
                    _usuariosState.value = ListUiState.Success(listOf(usuario))
                }
                .onFailure {
                    _usuariosState.value = ListUiState.Error(it.toUserMessage())
                }
        }
    }

    fun triggerDialog(usuario: Usuario? = null) {
        _showDialogEvent.value = Event(usuario)
    }
}

// Clase auxiliar para eventos de una sola ejecución (Event)
data class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }
}
