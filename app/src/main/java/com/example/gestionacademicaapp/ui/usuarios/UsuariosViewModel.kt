package com.example.gestionacademicaapp.ui.usuarios

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.example.gestionacademicaapp.ui.common.state.ActionState
import com.example.gestionacademicaapp.utils.SessionManager
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val profesorRepository: ProfesorRepository,
    private val carreraRepository: CarreraRepository
) : ViewModel() {

    private val _usuarios = MutableLiveData<List<Usuario>>()
    val usuarios: LiveData<List<Usuario>> get() = _usuarios

    private val _carreras = MutableLiveData<List<Carrera>>(emptyList()) // Initialize with empty list
    val carreras: LiveData<List<Carrera>> get() = _carreras

    private val _actionState = MutableLiveData<ActionState>()
    val actionState: LiveData<ActionState> get() = _actionState

    private val _editingUserData = MutableLiveData<Pair<Usuario, Map<String, String>>?>()
    val editingUserData: LiveData<Pair<Usuario, Map<String, String>>?> get() = _editingUserData

    init {
        loadCarreras()
    }

    fun fetchUsuarios() {
        viewModelScope.launch {
            usuarioRepository.listar().onSuccess { usuarios ->
                _usuarios.value = usuarios
            }.onFailure { exception ->
                _actionState.value = ActionState.Error("No se pudieron cargar los usuarios: ${exception.message}")
            }
        }
    }

    fun prepareUserForEdit(usuario: Usuario) {
        viewModelScope.launch {
            val datosIniciales = mutableMapOf(
                "cedula" to usuario.cedula,
                "tipo" to usuario.tipo
            ) // Omit clave to keep it empty by default
            when (usuario.tipo) {
                "Alumno" -> {
                    alumnoRepository.buscarPorCedula(usuario.cedula).onSuccess { alumno ->
                        datosIniciales["nombre"] = alumno.nombre
                        datosIniciales["telefono"] = alumno.telefono
                        datosIniciales["email"] = alumno.email
                        datosIniciales["fechaNacimiento"] = alumno.fechaNacimiento
                        datosIniciales["carrera"] = alumno.pkCarrera.toString()
                        _editingUserData.value = usuario to datosIniciales
                    }.onFailure { exception ->
                        _actionState.value = ActionState.Error("Error al cargar datos del alumno: ${exception.message}")
                    }
                }
                "Profesor" -> {
                    profesorRepository.buscarPorCedula(usuario.cedula).onSuccess { profesor ->
                        datosIniciales["nombre"] = profesor.nombre
                        datosIniciales["telefono"] = profesor.telefono
                        datosIniciales["email"] = profesor.email
                        _editingUserData.value = usuario to datosIniciales
                    }.onFailure { exception ->
                        _actionState.value = ActionState.Error("Error al cargar datos del profesor: ${exception.message}")
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
            val cedula = datos["cedula"] ?: run {
                _actionState.value = ActionState.ValidationError("La cédula es obligatoria")
                return@launch
            }
            val tipo = datos["tipo"] ?: run {
                _actionState.value = ActionState.ValidationError("El tipo de usuario es obligatorio")
                return@launch
            }

            if (!isValidUserType(tipo)) {
                _actionState.value = ActionState.ValidationError("Tipo de usuario no válido")
                return@launch
            }

            val existingUserResult = usuarioRepository.buscarPorCedula(cedula)
            existingUserResult.onSuccess { existingUser ->
                if (idUsuario == null || existingUser.idUsuario != idUsuario) {
                    _actionState.value = ActionState.ValidationError("Ya existe un usuario con esta cédula")
                    return@launch
                }
            }.onFailure { _ -> }

            val clave = if (idUsuario != null && datos["clave"].isNullOrBlank()) {
                _usuarios.value?.find { it.idUsuario == idUsuario }?.clave ?: ""
            } else {
                datos["clave"]?.trim() ?: run {
                    _actionState.value = ActionState.ValidationError("La clave es obligatoria para nuevos usuarios")
                    return@launch
                }
            }

            if (clave.isBlank() && idUsuario == null) {
                _actionState.value = ActionState.ValidationError("La clave no puede estar vacía ni contener solo espacios")
                return@launch
            }

            val usuario = Usuario(
                idUsuario = idUsuario ?: 0L,
                cedula = cedula,
                clave = clave,
                tipo = tipo
            )

            if (idUsuario == null) {
                usuarioRepository.insertar(usuario).onSuccess {
                    when (tipo) {
                        "Alumno" -> saveAlumno(datos, cedula)
                        "Profesor" -> saveProfesor(datos, cedula)
                        "Administrador", "Matriculador" -> _actionState.value = ActionState.Success
                    }
                }.onFailure { exception ->
                    _actionState.value = ActionState.Error("Error al guardar el usuario: ${exception.message}")
                }
            } else {
                val oldCedula = _usuarios.value?.find { it.idUsuario == idUsuario }?.cedula ?: cedula
                val updateRelatedResult = when (tipo) {
                    "Alumno" -> updateAlumno(datos, oldCedula, cedula)
                    "Profesor" -> updateProfesor(datos, oldCedula, cedula)
                    else -> Result.success(Unit)
                }
                updateRelatedResult.onSuccess {
                    usuarioRepository.modificar(usuario).onSuccess {
                        _actionState.value = ActionState.Success
                    }.onFailure { exception ->
                        _actionState.value = ActionState.Error("Error al actualizar el usuario: ${exception.message}")
                    }
                }.onFailure { exception ->
                    _actionState.value = ActionState.Error("Error al actualizar datos relacionados: ${exception.message}")
                }
            }
        }
    }

    fun deleteUsuario(idUsuario: Long, context: Context) {
        viewModelScope.launch {
            _usuarios.value?.find { it.idUsuario == idUsuario } ?: run {
                _actionState.value = ActionState.Error("Usuario no encontrado")
                return@launch
            }

            val currentUserId = SessionManager.getUserId(context)
            if (idUsuario == currentUserId) {
                _actionState.value = ActionState.DependencyError("No puedes eliminarte a ti mismo")
                return@launch
            }

            usuarioRepository.eliminar(idUsuario).onSuccess {
                _actionState.value = ActionState.Success
            }.onFailure { exception ->
                val message = exception.toUserMessage()
                _actionState.value = when {
                    message.contains("matrículas asociadas") || message.contains("grupos asignados") -> {
                        ActionState.DependencyError(message)
                    }
                    message.contains("Sesión expirada") || message.contains("No autorizado") -> {
                        ActionState.Error(message)
                    }
                    else -> ActionState.Error("Error al eliminar usuario: $message")
                }
            }
        }
    }

    private fun saveAlumno(datos: Map<String, String>, cedula: String) {
        viewModelScope.launch {
            val nombre = datos["nombre"]?.trim() ?: run {
                _actionState.value = ActionState.ValidationError("El nombre del alumno no puede estar vacío")
                return@launch
            }
            if (nombre.isEmpty()) {
                _actionState.value = ActionState.ValidationError("El nombre del alumno no puede estar vacío")
                return@launch
            }

            val email = datos["email"]?.trim() ?: run {
                _actionState.value = ActionState.ValidationError("El correo del alumno es obligatorio")
                return@launch
            }
            if (!isValidEmail(email)) {
                _actionState.value = ActionState.ValidationError("El correo del alumno no tiene un formato válido")
                return@launch
            }

            val fechaNacimiento = datos["fechaNacimiento"]?.trim() ?: run {
                _actionState.value = ActionState.ValidationError("La fecha de nacimiento es obligatoria")
                return@launch
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
            val date = LocalDate.parse(fechaNacimiento, formatter)
            if (date.isAfter(LocalDate.now())) {
                _actionState.value = ActionState.ValidationError("La fecha de nacimiento no puede ser futura")
                return@launch
            }

            val carreraId = datos["carrera"]?.toLongOrNull() ?: run {
                _actionState.value = ActionState.ValidationError("La carrera es obligatoria")
                return@launch
            }
            val carreraExists = _carreras.value?.any { it.idCarrera == carreraId } == true
            if (!carreraExists) {
                _actionState.value = ActionState.ValidationError("La carrera seleccionada no existe")
                return@launch
            }

            val alumno = Alumno(
                idAlumno = 0L,
                cedula = cedula,
                nombre = nombre,
                telefono = datos["telefono"]?.trim() ?: "",
                email = email,
                fechaNacimiento = fechaNacimiento,
                pkCarrera = carreraId
            )
            alumnoRepository.insertar(alumno).onSuccess {
                _actionState.value = ActionState.Success
            }.onFailure { exception ->
                _actionState.value = ActionState.Error("Error al guardar datos del alumno: ${exception.message}")
            }
        }
    }

    private suspend fun updateAlumno(datos: Map<String, String>, oldCedula: String, newCedula: String): Result<Unit> {
        val nombre = datos["nombre"]?.trim() ?: return Result.failure(Exception("El nombre del alumno no puede estar vacío"))
        if (nombre.isEmpty()) return Result.failure(Exception("El nombre del alumno no puede estar vacío"))

        val email = datos["email"]?.trim() ?: return Result.failure(Exception("El correo del alumno es obligatorio"))
        if (!isValidEmail(email)) return Result.failure(Exception("El correo del alumno no tiene un formato válido"))

        val fechaNacimiento = datos["fechaNacimiento"]?.trim() ?: return Result.failure(Exception("La fecha de nacimiento es obligatoria"))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        val date = LocalDate.parse(fechaNacimiento, formatter)
        if (date.isAfter(LocalDate.now())) return Result.failure(Exception("La fecha de nacimiento no puede ser futura"))

        val carreraId = datos["carrera"]?.toLongOrNull() ?: return Result.failure(Exception("La carrera es obligatoria"))
        val carreraExists = _carreras.value?.any { it.idCarrera == carreraId } == true
        if (!carreraExists) return Result.failure(Exception("La carrera seleccionada no existe"))

        return try {
            val existingAlumnoResult = alumnoRepository.buscarPorCedula(oldCedula)
            if (existingAlumnoResult.isSuccess) {
                val existingAlumno = existingAlumnoResult.getOrNull()
                if (existingAlumno != null) {
                    val alumno = Alumno(
                        idAlumno = existingAlumno.idAlumno,
                        cedula = newCedula,
                        nombre = nombre,
                        telefono = datos["telefono"]?.trim() ?: "",
                        email = email,
                        fechaNacimiento = fechaNacimiento,
                        pkCarrera = carreraId
                    )
                    alumnoRepository.modificar(alumno)
                } else {
                    Result.failure(Exception("No se encontró el alumno para actualizar"))
                }
            } else {
                Result.failure(existingAlumnoResult.exceptionOrNull() ?: Exception("Error al buscar el alumno"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveProfesor(datos: Map<String, String>, cedula: String) {
        viewModelScope.launch {
            val nombre = datos["nombre"]?.trim() ?: run {
                _actionState.value = ActionState.ValidationError("El nombre del profesor no puede estar vacío")
                return@launch
            }
            if (nombre.isEmpty()) {
                _actionState.value = ActionState.ValidationError("El nombre del profesor no puede estar vacío")
                return@launch
            }

            val email = datos["email"]?.trim() ?: run {
                _actionState.value = ActionState.ValidationError("El correo del profesor es obligatorio")
                return@launch
            }
            if (!isValidEmail(email)) {
                _actionState.value = ActionState.ValidationError("El correo del profesor no tiene un formato válido")
                return@launch
            }

            val profesor = Profesor(
                idProfesor = 0L,
                cedula = cedula,
                nombre = nombre,
                telefono = datos["telefono"]?.trim() ?: "",
                email = email
            )
            profesorRepository.insertar(profesor).onSuccess {
                _actionState.value = ActionState.Success
            }.onFailure { exception ->
                _actionState.value = ActionState.Error("Error al guardar datos del profesor: ${exception.message}")
            }
        }
    }

    private suspend fun updateProfesor(datos: Map<String, String>, oldCedula: String, newCedula: String): Result<Unit> {
        val nombre = datos["nombre"]?.trim() ?: return Result.failure(Exception("El nombre del profesor no puede estar vacío"))
        if (nombre.isEmpty()) return Result.failure(Exception("El nombre del profesor no puede estar vacío"))

        val email = datos["email"]?.trim() ?: return Result.failure(Exception("El correo del profesor es obligatorio"))
        if (!isValidEmail(email)) return Result.failure(Exception("El correo del profesor no tiene un formato válido"))

        return try {
            val existingProfesorResult = profesorRepository.buscarPorCedula(oldCedula)
            if (existingProfesorResult.isSuccess) {
                val existingProfesor = existingProfesorResult.getOrNull()
                if (existingProfesor != null) {
                    val profesor = Profesor(
                        idProfesor = existingProfesor.idProfesor,
                        cedula = newCedula,
                        nombre = nombre,
                        telefono = datos["telefono"]?.trim() ?: "",
                        email = email
                    )
                    profesorRepository.modificar(profesor)
                } else {
                    Result.failure(Exception("No se encontró el profesor para actualizar"))
                }
            } else {
                Result.failure(existingProfesorResult.exceptionOrNull() ?: Exception("Error al buscar el profesor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadCarreras() {
        viewModelScope.launch {
            carreraRepository.listar().onSuccess { carreras ->
                _carreras.value = carreras // Empty list is valid
            }.onFailure { exception ->
                _actionState.value = ActionState.Error("No se pudieron cargar las carreras: ${exception.message}. Por favor, verifica la conexión o contacta al administrador.")
            }
        }
    }

    private fun isValidUserType(tipo: String): Boolean {
        return tipo in listOf("Administrador", "Matriculador", "Profesor", "Alumno")
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
    }
}
