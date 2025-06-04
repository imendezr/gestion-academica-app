package com.example.gestionacademicaapp.ui.matricula

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private var idCurso: Long? = null
    var idGrupo: Long? = null

    val alumnosState: StateFlow<UiState<List<Alumno>>> = reloadTrigger
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
            if (idCiclo == 0L) flowOf(UiState.Success(emptyList()))
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
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    val gruposState: StateFlow<UiState<List<GrupoDto>>> = reloadTrigger
        .debounce(100)
        .flatMapLatest {
            if (idCiclo == 0L || idCurso == null) flowOf(UiState.Success(emptyList()))
            else flow {
                println("Starting group fetch for curso=$idCurso, ciclo=$idCiclo, carrera=$idCarrera")
                emit(UiState.Loading)
                try {
                    val result = grupoRepository.gruposPorCursoCicloCarrera(idCurso!!, idCiclo, idCarrera)
                    println("Groups fetched: ${result.getOrNull()?.size ?: 0}, result=$result")
                    emit(result.toUiState())
                } catch (e: Exception) {
                    println("Group fetch error: ${e.message}")
                    emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
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
        this.idAlumno = idAlumno
        this.idCiclo = idCiclo
        this.idCarrera = idCarrera
        println("Params set: alumno=$idAlumno, ciclo=$idCiclo, carrera=$idCarrera")
        idCurso = null
        idGrupo = null
        reloadTrigger.tryEmit(Unit)
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
        if (this.idCurso != idCurso) {
            this.idCurso = idCurso
            this.idGrupo = null
            println("Curso set to: $idCurso")
            reloadTrigger.tryEmit(Unit)
        }
    }

    fun selectGrupo(idGrupo: Long) {
        this.idGrupo = idGrupo
        println("Grupo selected: $idGrupo")
    }

    fun confirmarMatricula() {
        viewModelScope.launch {
            _actionState.emit(UiState.Loading)
            try {
                idGrupo?.let { grupoId ->
                    val matricula = Matricula(idMatricula = 0, pkAlumno = idAlumno, pkGrupo = grupoId, nota = 0)
                    matriculaRepository.insertar(matricula).fold(
                        onSuccess = {
                            println("Matricula successful")
                            _actionState.emit(UiState.Success(Unit))
                        },
                        onFailure = { e ->
                            println("Matricula failed: ${e.message}")
                            _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
                        }
                    )
                } ?: _actionState.emit(UiState.Error("Seleccione un grupo", ErrorType.VALIDATION))
            } catch (e: Exception) {
                println("Matricula exception: ${e.message}")
                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
            }
        }
    }

    suspend fun getGrupoIdForMatricula(matricula: MatriculaAlumnoDto): Long? {
        try {
            val ciclosResult = cicloRepository.listar()
            return ciclosResult.fold(
                onSuccess = { ciclos ->
                    for (ciclo in ciclos) {
                        val cursosResult = grupoRepository.cursosPorCarreraYCiclo(idCarrera, ciclo.idCiclo)
                        val grupoId = cursosResult.fold(
                            onSuccess = { cursos ->
                                val curso = cursos.find { it.codigo == matricula.codigoCurso }
                                curso?.idCurso?.let { idCurso ->
                                    val gruposResult = grupoRepository.gruposPorCursoCicloCarrera(idCurso, ciclo.idCiclo, idCarrera)
                                    gruposResult.fold(
                                        onSuccess = { grupos ->
                                            grupos.find { it.numeroGrupo.toString() == matricula.numeroGrupo }?.idGrupo
                                        },
                                        onFailure = {
                                            println("Failed to fetch groups for ciclo=${ciclo.idCiclo}: ${it.message}")
                                            null
                                        }
                                    )
                                }
                            },
                            onFailure = {
                                println("Failed to fetch courses for ciclo=${ciclo.idCiclo}: ${it.message}")
                                null
                            }
                        )
                        if (grupoId != null) {
                            this.idCiclo = ciclo.idCiclo
                            println("Grupo encontrado en ciclo=${ciclo.idCiclo}, grupoId=$grupoId")
                            return grupoId
                        }
                    }
                    println("No se encontró grupo para matricula en ningún ciclo")
                    null
                },
                onFailure = {
                    println("Failed to fetch ciclos: ${it.message}")
                    null
                }
            )
        } catch (e: Exception) {
            println("Error fetching grupo: ${e.message}")
            return null
        }
    }

    private fun <T> Result<T>.toUiState(transform: (T) -> T = { it }): UiState<T> = fold(
        onSuccess = { UiState.Success(transform(it)) },
        onFailure = { e -> UiState.Error(e.toUserMessage(), mapErrorType(e)) }
    )

    private fun mapErrorType(throwable: Throwable): ErrorType = when {
        throwable is HttpException && throwable.code() in 400..499 -> ErrorType.VALIDATION
        throwable.message?.contains("dependencias", ignoreCase = true) == true -> ErrorType.DEPENDENCY
        throwable.message?.contains("ya está matriculado", ignoreCase = true) == true -> ErrorType.VALIDATION
        throwable.message?.contains("-2000", ignoreCase = true) == true -> ErrorType.VALIDATION
        else -> ErrorType.GENERAL
    }
}
