package com.example.gestionacademicaapp.ui.usuarios

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.repository.AlumnoRepository
import com.example.gestionacademicaapp.data.repository.CarreraRepository
import com.example.gestionacademicaapp.data.repository.ProfesorRepository
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.ui.common.validators.UsuarioValidator
import com.example.gestionacademicaapp.utils.SessionManager
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val profesorRepository: ProfesorRepository,
    private val carreraRepository: CarreraRepository
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    val usuariosState: StateFlow<UiState<List<Usuario>>> = reloadTrigger.flatMapLatest {
        flow {
            emit(UiState.Loading)
            usuarioRepository.listar()
                .onSuccess { emit(UiState.Success(data = it)) }
                .onFailure { emit(UiState.Error(it.toUserMessage(), mapErrorType(it))) }
        }.catch { emit(UiState.Error(it.message ?: "Error desconocido", ErrorType.GENERAL)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Success())
    val actionState: StateFlow<UiState<Unit>> get() = _actionState

    private val _carreras = MutableStateFlow<List<Carrera>>(emptyList())
    val carreras: StateFlow<List<Carrera>> get() = _carreras

    private val _editingUserData = MutableStateFlow<Pair<Usuario, Map<String, String>>?>(null)
    val editingUserData: StateFlow<Pair<Usuario, Map<String, String>>?> get() = _editingUserData

    init {
        loadCarreras()
    }

    private fun triggerReload() {
        viewModelScope.launch { reloadTrigger.emit(Unit) }
    }

    fun prepareUserForEdit(usuario: Usuario) {
        viewModelScope.launch {
            val datosIniciales = mutableMapOf(
                "cedula" to usuario.cedula,
                "tipo" to usuario.tipo
            )
            when (usuario.tipo) {
                "Alumno" -> {
                    alumnoRepository.buscarPorCedula(usuario.cedula)
                        .onSuccess { alumno ->
                            datosIniciales["nombre"] = alumno.nombre
                            datosIniciales["telefono"] = alumno.telefono
                            datosIniciales["email"] = alumno.email
                            datosIniciales["fechaNacimiento"] = alumno.fechaNacimiento
                            datosIniciales["carrera"] = alumno.pkCarrera.toString()
                            _editingUserData.value = usuario to datosIniciales
                        }
                        .onFailure {
                            _actionState.value = UiState.Error("Error al cargar datos del alumno", ErrorType.GENERAL)
                        }
                }
                "Profesor" -> {
                    profesorRepository.buscarPorCedula(usuario.cedula)
                        .onSuccess { profesor ->
                            datosIniciales["nombre"] = profesor.nombre
                            datosIniciales["telefono"] = profesor.telefono
                            datosIniciales["email"] = profesor.email
                            _editingUserData.value = usuario to datosIniciales
                        }
                        .onFailure {
                            _actionState.value = UiState.Error("Error al cargar datos del profesor", ErrorType.GENERAL)
                        }
                }
                else -> {
                    _editingUserData.value = usuario to datosIniciales
                }
            }
        }
    }

    fun clearEditingUserData() {
        _editingUserData.value = null
    }

    fun saveUsuario(datos: Map<String, String>, idUsuario: Long? = null) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            validateUsuario(datos, idUsuario)
                .onSuccess {
                    val cedula = datos["cedula"]!!
                    val tipo = datos["tipo"]!!
                    val clave = if (idUsuario != null && datos["clave"].isNullOrBlank()) {
                        usuarioRepository.buscarPorCedula(cedula).getOrNull()?.clave ?: ""
                    } else {
                        datos["clave"]?.trim() ?: ""
                    }
                    usuarioRepository.buscarPorCedula(cedula)
                        .onSuccess { existingUser ->
                            if (idUsuario == null || existingUser.idUsuario != idUsuario) {
                                _actionState.value = UiState.Error("Ya existe un usuario con esta cédula", ErrorType.VALIDATION)
                                return@launch
                            }
                            performSaveUsuario(datos, idUsuario, cedula, tipo, clave)
                        }
                        .onFailure { performSaveUsuario(datos, idUsuario, cedula, tipo, clave) }
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.message ?: "Datos inválidos", ErrorType.VALIDATION)
                }
        }
    }

    private suspend fun performSaveUsuario(
        datos: Map<String, String>,
        idUsuario: Long?,
        cedula: String,
        tipo: String,
        clave: String
    ) {
        val usuario = Usuario(
            idUsuario = idUsuario ?: 0L,
            cedula = cedula,
            clave = clave,
            tipo = tipo
        )
        if (idUsuario == null) {
            usuarioRepository.insertar(usuario)
                .onSuccess {
                    when (tipo) {
                        "Alumno" -> saveAlumno(datos, cedula)
                        "Profesor" -> saveProfesor(datos, cedula)
                        else -> {
                            _actionState.value = UiState.Success(message = "Usuario creado exitosamente")
                            triggerReload()
                        }
                    }
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.toUserMessage(), mapErrorType(it))
                }
        } else {
            val oldCedula = usuarioRepository.buscarPorCedula(idUsuario.toString()).getOrNull()?.cedula ?: cedula
            when (tipo) {
                "Alumno" -> updateAlumno(datos, oldCedula, cedula)
                "Profesor" -> updateProfesor(datos, oldCedula, cedula)
                else -> Result.success(Unit)
            }.onSuccess {
                usuarioRepository.modificar(usuario)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Usuario actualizado exitosamente")
                        triggerReload()
                    }
                    .onFailure {
                        _actionState.value = UiState.Error(it.toUserMessage(), mapErrorType(it))
                    }
            }.onFailure {
                _actionState.value = UiState.Error(it.toUserMessage(), mapErrorType(it))
            }
        }
    }

    fun deleteUsuario(idUsuario: Long, context: Context) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            if (idUsuario == SessionManager.getUserId(context)) {
                _actionState.value = UiState.Error("No puedes eliminarte a ti mismo", ErrorType.DEPENDENCY)
                return@launch
            }
            usuarioRepository.eliminar(idUsuario)
                .onSuccess {
                    _actionState.value = UiState.Success(message = "Usuario eliminado exitosamente")
                    triggerReload()
                }
                .onFailure {
                    val message = it.toUserMessage()
                    _actionState.value = UiState.Error(
                        message = when {
                            message.contains("matrículas asociadas") || message.contains("grupos asignados") -> message
                            else -> "Error al eliminar usuario: $message"
                        },
                        type = if (message.contains("matrículas") || message.contains("grupos")) ErrorType.DEPENDENCY else ErrorType.GENERAL
                    )
                }
        }
    }

    private fun validateUsuario(datos: Map<String, String>, idUsuario: Long?): Result<Unit> {
        val validator = UsuarioValidator()
        return when {
            datos["cedula"].isNullOrBlank() -> Result.failure(Exception("La cédula es obligatoria"))
            validator.validateCedula(datos["cedula"]!!) != null -> Result.failure(Exception(validator.validateCedula(datos["cedula"]!!)))
            datos["tipo"].isNullOrBlank() -> Result.failure(Exception("El tipo de usuario es obligatorio"))
            validator.validateTipo(datos["tipo"]!!) != null -> Result.failure(Exception(validator.validateTipo(datos["tipo"]!!)))
            idUsuario == null && datos["clave"].isNullOrBlank() -> Result.failure(Exception("La clave es obligatoria para nuevos usuarios"))
            validator.validateClave(datos["clave"] ?: "", idUsuario != null) != null -> Result.failure(Exception(validator.validateClave(datos["clave"] ?: "", idUsuario != null)))
            datos["tipo"] == "Alumno" && (datos["nombre"].isNullOrBlank() || validator.validateNombre(datos["nombre"]!!) != null) -> Result.failure(Exception("Nombre inválido"))
            datos["tipo"] == "Alumno" && (datos["email"].isNullOrBlank() || validator.validateEmail(datos["email"]!!) != null) -> Result.failure(Exception("Email inválido"))
            datos["tipo"] == "Alumno" && (datos["fechaNacimiento"].isNullOrBlank() || validator.validateFechaNacimiento(datos["fechaNacimiento"]!!) != null) -> Result.failure(Exception("Fecha de nacimiento inválida"))
            datos["tipo"] == "Alumno" && (datos["carrera"].isNullOrBlank() || validator.validateCarrera(datos["carrera"]!!, _carreras.value) != null) -> Result.failure(Exception("Carrera inválida"))
            datos["tipo"] == "Alumno" && !datos["telefono"].isNullOrBlank() && validator.validateTelefono(datos["telefono"]!!) != null -> Result.failure(Exception(validator.validateTelefono(datos["telefono"]!!)))
            datos["tipo"] == "Profesor" && (datos["nombre"].isNullOrBlank() || validator.validateNombre(datos["nombre"]!!) != null) -> Result.failure(Exception("Nombre inválido"))
            datos["tipo"] == "Profesor" && (datos["email"].isNullOrBlank() || validator.validateEmail(datos["email"]!!) != null) -> Result.failure(Exception("Email inválido"))
            datos["tipo"] == "Profesor" && !datos["telefono"].isNullOrBlank() && validator.validateTelefono(datos["telefono"]!!) != null -> Result.failure(Exception(validator.validateTelefono(datos["telefono"]!!)))
            else -> Result.success(Unit)
        }
    }

    private suspend fun saveAlumno(datos: Map<String, String>, cedula: String) {
        val alumno = Alumno(
            idAlumno = 0L,
            cedula = cedula,
            nombre = datos["nombre"]!!.trim(),
            telefono = datos["telefono"]?.trim() ?: "",
            email = datos["email"]!!.trim(),
            fechaNacimiento = datos["fechaNacimiento"]!!,
            pkCarrera = datos["carrera"]!!.toLong()
        )
        alumnoRepository.insertar(alumno)
            .onSuccess {
                _actionState.value = UiState.Success(message = "Usuario creado exitosamente")
                triggerReload()
            }
            .onFailure {
                _actionState.value = UiState.Error("Error al guardar datos del alumno", mapErrorType(it))
            }
    }

    private suspend fun updateAlumno(datos: Map<String, String>, oldCedula: String, newCedula: String): Result<Unit> {
        val existingAlumno = alumnoRepository.buscarPorCedula(oldCedula).getOrNull() ?: return Result.failure(Exception("Alumno no encontrado"))
        val alumno = Alumno(
            idAlumno = existingAlumno.idAlumno,
            cedula = newCedula,
            nombre = datos["nombre"]!!.trim(),
            telefono = datos["telefono"]?.trim() ?: "",
            email = datos["email"]!!.trim(),
            fechaNacimiento = datos["fechaNacimiento"]!!,
            pkCarrera = datos["carrera"]!!.toLong()
        )
        return alumnoRepository.modificar(alumno)
    }

    private suspend fun saveProfesor(datos: Map<String, String>, cedula: String) {
        val profesor = Profesor(
            idProfesor = 0L,
            cedula = cedula,
            nombre = datos["nombre"]!!.trim(),
            telefono = datos["telefono"]?.trim() ?: "",
            email = datos["email"]!!.trim()
        )
        profesorRepository.insertar(profesor)
            .onSuccess {
                _actionState.value = UiState.Success(message = "Usuario creado exitosamente")
                triggerReload()
            }
            .onFailure {
                _actionState.value = UiState.Error("Error al guardar datos del profesor", mapErrorType(it))
            }
    }

    private suspend fun updateProfesor(datos: Map<String, String>, oldCedula: String, newCedula: String): Result<Unit> {
        val existingProfesor = profesorRepository.buscarPorCedula(oldCedula).getOrNull() ?: return Result.failure(Exception("Profesor no encontrado"))
        val profesor = Profesor(
            idProfesor = existingProfesor.idProfesor,
            cedula = newCedula,
            nombre = datos["nombre"]!!.trim(),
            telefono = datos["telefono"]?.trim() ?: "",
            email = datos["email"]!!.trim()
        )
        return profesorRepository.modificar(profesor)
    }

    private fun loadCarreras() {
        viewModelScope.launch {
            carreraRepository.listar()
                .onSuccess { _carreras.value = it }
                .onFailure { _actionState.value = UiState.Error("No se pudieron cargar las carreras", ErrorType.GENERAL) }
        }
    }

    private fun mapErrorType(throwable: Throwable): ErrorType {
        val message = throwable.toUserMessage().lowercase()
        return when {
            message.contains("matrículas") || message.contains("grupos") -> ErrorType.DEPENDENCY
            message.contains("formato") || message.contains("inválido") -> ErrorType.VALIDATION
            else -> ErrorType.GENERAL
        }
    }
}
