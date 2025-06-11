package com.example.gestionacademicaapp.ui.matricula

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.repository.AlumnoRepository
import com.example.gestionacademicaapp.data.repository.CicloRepository
import com.example.gestionacademicaapp.data.repository.GrupoRepository
import com.example.gestionacademicaapp.data.repository.MatriculaRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MatriculaViewModel @Inject constructor(
    private val matriculaRepository: MatriculaRepository,
    private val alumnoRepository: AlumnoRepository,
    private val cicloRepository: CicloRepository,
    private val grupoRepository: GrupoRepository
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    var selectedCicloId: Long? = null
        private set
    private var idAlumno: Long = 0L
    private var idCiclo: Long = 0L
    private var idCarrera: Long = 0L
    internal var idCurso: Long? = null
    var idGrupo: Long? = null

    // Agregar al inicio de la clase
    private val _matriculaUpdated = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val matriculaUpdated: SharedFlow<Unit> = _matriculaUpdated.asSharedFlow()

    val alumnosState: StateFlow<UiState<List<Alumno>>> = reloadTrigger
        .debounce(100) // Add debounce
        .flatMapLatest {
            selectedCicloId?.let { idCiclo ->
                flow {
                    emit(UiState.Loading)
                    emit(alumnoRepository.alumnosConOfertaEnCiclo(idCiclo).toUiState())
                }
            } ?: flowOf(UiState.Success(emptyList()))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val ciclos: StateFlow<UiState<List<Ciclo>>> = flow {
        emit(UiState.Loading)
        val result = cicloRepository.listar()
        result.onSuccess { ciclos ->
            println("All cycles: ${ciclos.size}, estados=${ciclos.map { it.estado }}")
            emit(UiState.Success(ciclos))
            val activeCycle = ciclos.firstOrNull { it.estado.equals("activo", ignoreCase = true) }
            if (activeCycle != null && selectedCicloId == null) {
                setCiclo(activeCycle.idCiclo)
            }
        }.onFailure {
            emit(UiState.Error(it.toUserMessage(), mapErrorType(it)))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val cursos: StateFlow<UiState<List<CursoDto>>> = reloadTrigger
        .debounce(100)
        .flatMapLatest {
            if (idCiclo == 0L || idCarrera == 0L) flowOf(UiState.Success(emptyList()))
            else flow {
                println("Starting course fetch for carrera=$idCarrera, ciclo=$idCiclo")
                emit(UiState.Loading)
                try {
                    val result = grupoRepository.cursosPorCarreraYCiclo(idCarrera, idCiclo)
                    println("Courses fetched: ${result.getOrNull()?.size ?: 0}, result=$result")
                    emit(result.toUiState())
                } catch (e: Exception) {
                    println("Course fetch failed: ${e.message}")
                    emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    val gruposState: StateFlow<UiState<List<GrupoDto>>> = reloadTrigger
        .debounce(100)
        .flatMapLatest {
            if (idCiclo == 0L || idCurso == null || idCurso == 0L) flowOf(UiState.Success(emptyList()))
            else flow {
                println("Starting group fetch for curso=$idCurso, ciclo=$idCiclo, carrera=$idCarrera")
                emit(UiState.Loading)
                try {
                    val result =
                        grupoRepository.gruposPorCursoCicloCarrera(idCurso!!, idCiclo, idCarrera)
                    println("Groups fetched: ${result.getOrNull()?.size ?: 0}, result=$result")
                    emit(result.toUiState())
                } catch (e: Exception) {
                    println("Group fetch error: ${e.message}")
                    emit(
                        UiState.Error(
                            "No hay grupos disponibles para este curso",
                            mapErrorType(e)
                        )
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    private val _actionState = MutableStateFlow<UiState<Unit>?>(null)
    val actionState: StateFlow<UiState<Unit>?> = _actionState.asStateFlow()

    init {
        reloadTrigger.tryEmit(Unit)
    }

    fun setParams(idAlumno: Long, idCiclo: Long, idCarrera: Long) {
        if (this.idAlumno != idAlumno || this.idCiclo != idCiclo || this.idCarrera != idCarrera) {
            this.idAlumno = idAlumno
            this.idCiclo = idCiclo
            this.idCarrera = idCarrera
            println("Params set: alumno=$idAlumno, ciclo=$idCiclo, carrera=$idCarrera")
            idCurso = null
            idGrupo = null
            reloadTrigger.tryEmit(Unit)
        }
    }

    fun setCiclo(idCiclo: Long) {
        if (selectedCicloId != idCiclo) {
            selectedCicloId = idCiclo
            this.idCiclo = idCiclo
            println("Cycle set to: $idCiclo")
            reloadTrigger.tryEmit(Unit)
        }
    }

    fun setCurso(idCurso: Long) {
        if (this.idCurso != idCurso && idCurso != 0L) { // Only update if course changes and is valid
            this.idCurso = idCurso
            this.idGrupo = null
            println("Curso set to: $idCurso")
            reloadTrigger.tryEmit(Unit)
        } else {
            println("Curso not changed or invalid: idCurso=$idCurso, current=$idCurso")
        }
    }

    fun selectGrupo(idGrupo: Long) {
        this.idGrupo = idGrupo
        println("Grupo selected: $idGrupo")
    }

    fun confirmarMatricula(idMatricula: Long? = null) {
        viewModelScope.launch {
            _actionState.emit(UiState.Loading)
            try {
                idGrupo?.let { grupoId ->
                    if (idMatricula != null && idMatricula != 0L) {
                        println("Intentando modificar matrícula: idMatricula=$idMatricula, grupoId=$grupoId")
                        matriculaRepository.modificarGrupoMatricula(idMatricula, grupoId).fold(
                            onSuccess = {
                                println("Matrícula actualizada exitosamente: idMatricula=$idMatricula")
                                _actionState.emit(UiState.Success(Unit))
                                _matriculaUpdated.tryEmit(Unit)
                            },
                            onFailure = { e ->
                                println("Fallo al actualizar matrícula: ${e.message}, stackTrace=${e.stackTraceToString()}")
                                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
                            }
                        )
                    } else {
                        println("Creando nueva matrícula para alumno=$idAlumno, grupo=$grupoId")
                        val exists =
                            matriculaRepository.existeMatriculaPorAlumnoYGrupo(idAlumno, grupoId)
                                .getOrNull() == true
                        if (exists) {
                            println("Matrícula ya existe para alumno=$idAlumno, grupo=$grupoId")
                            _actionState.emit(
                                UiState.Error(
                                    "El alumno ya está matriculado en este grupo",
                                    ErrorType.VALIDATION
                                )
                            )
                            return@launch
                        }
                        val matricula = Matricula(
                            idMatricula = 0,
                            pkAlumno = idAlumno,
                            pkGrupo = grupoId,
                            nota = 0
                        )
                        matriculaRepository.insertar(matricula).fold(
                            onSuccess = {
                                println("Matrícula creada exitosamente")
                                _actionState.emit(UiState.Success(Unit))
                                _matriculaUpdated.tryEmit(Unit)
                            },
                            onFailure = { e ->
                                println("Fallo al crear matrícula: ${e.message}, stackTrace=${e.stackTraceToString()}")
                                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
                            }
                        )
                    }
                } ?: run {
                    println("Error: No se seleccionó un grupo")
                    _actionState.emit(UiState.Error("Seleccione un grupo", ErrorType.VALIDATION))
                }
            } catch (e: Exception) {
                println("Excepción en confirmarMatricula: ${e.message}, stackTrace=${e.stackTraceToString()}")
                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
            }
        }
    }

    private fun <T> Result<T>.toUiState(transform: (T) -> T = { it }): UiState<T> = fold(
        onSuccess = { UiState.Success(transform(it)) },
        onFailure = { e -> UiState.Error(e.toUserMessage(), mapErrorType(e)) }
    )

    private fun mapErrorType(throwable: Throwable): ErrorType = when {
        throwable is HttpException -> when (throwable.code()) {
            in 400..499 -> {
                println("HTTP Error ${throwable.code()}: ${throwable.message()}")
                ErrorType.VALIDATION
            }

            else -> {
                println("HTTP Error ${throwable.code()}: ${throwable.message()}")
                ErrorType.GENERAL
            }
        }

        throwable.message?.contains("dependencias", ignoreCase = true) == true -> {
            println("Error de dependencia detectado")
            ErrorType.DEPENDENCY
        }

        throwable.message?.contains("ya está matriculado", ignoreCase = true) == true -> {
            println("Error de matrícula duplicada")
            ErrorType.VALIDATION
        }

        throwable.message?.contains("matricula duplicada", ignoreCase = true) == true -> {
            println("Error de matrícula duplicada")
            ErrorType.VALIDATION
        }

        throwable.message?.contains("-2000", ignoreCase = true) == true -> {
            println("Error de validación específico (-2000)")
            ErrorType.VALIDATION
        }

        else -> {
            println("Error general: ${throwable.message}")
            ErrorType.GENERAL
        }
    }
}
