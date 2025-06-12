package com.example.gestionacademicaapp.ui.notas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.api.model.dto.GrupoProfesorDto
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.data.repository.AlumnoRepository
import com.example.gestionacademicaapp.data.repository.GrupoRepository
import com.example.gestionacademicaapp.data.repository.MatriculaRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.ResourceProvider
import com.example.gestionacademicaapp.utils.SessionManager
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

@HiltViewModel
class NotasViewModel @Inject constructor(
    private val grupoRepository: GrupoRepository,
    private val matriculaRepository: MatriculaRepository,
    private val alumnoRepository: AlumnoRepository,
    private val resourceProvider: ResourceProvider,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    private var idGrupo: Long? = null
    private var cedulaProfesor: String? = null
    private val _actionState = MutableStateFlow<UiState<Unit>?>(null)
    val actionState: StateFlow<UiState<Unit>?> = _actionState.asStateFlow()

    val grupos: StateFlow<UiState<List<GrupoProfesorDto>>> = flow {
        emit(UiState.Loading)
        cedulaProfesor?.let { cedula ->
            emit(grupoRepository.gruposPorProfesorCicloActivo(cedula).toUiState())
        } ?: emit(UiState.Error(getString(R.string.error_no_profesor), ErrorType.GENERAL))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val matriculas: StateFlow<UiState<List<MatriculaAlumnoDto>>> = reloadTrigger
        .debounce(100)
        .flatMapLatest {
            if (idGrupo == null) flowOf(UiState.Success(emptyList()))
            else flow {
                emit(UiState.Loading)
                matriculaRepository.listarPorGrupo(idGrupo!!).fold(
                    onSuccess = { emit(UiState.Success(it)) },
                    onFailure = { e -> emit(UiState.Error(e.toUserMessage(), mapErrorType(e))) }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    init {
        loadProfesor()
        reloadTrigger.tryEmit(Unit)
    }

    private fun loadProfesor() {
        viewModelScope.launch {
            val usuario = sessionManager.getUsuario()
            if (usuario != null) {
                cedulaProfesor = usuario.cedula
            } else {
                _actionState.emit(
                    UiState.Error(
                        getString(R.string.error_no_profesor),
                        ErrorType.GENERAL
                    )
                )
            }
        }
    }

    fun setGrupo(idGrupo: Long) {
        this.idGrupo = idGrupo
        reloadTrigger.tryEmit(Unit)
    }

    fun updateNota(idMatricula: Long, nota: Long) {
        viewModelScope.launch {
            _actionState.emit(UiState.Loading)
            try {
                if (nota !in 0..100) {
                    _actionState.emit(
                        UiState.Error(
                            getString(R.string.error_nota_rango),
                            ErrorType.VALIDATION
                        )
                    )
                    return@launch
                }
                if (idGrupo == null) {
                    _actionState.emit(
                        UiState.Error(
                            getString(R.string.error_no_grupo_seleccionado),
                            ErrorType.VALIDATION
                        )
                    )
                    return@launch
                }
                matriculaRepository.buscarPorId(idMatricula).fold(
                    onSuccess = { matricula ->
                        val updatedMatricula = Matricula(
                            idMatricula = idMatricula,
                            pkAlumno = matricula.pkAlumno,
                            pkGrupo = idGrupo!!,
                            nota = nota
                        )
                        matriculaRepository.modificar(updatedMatricula).fold(
                            onSuccess = {
                                _actionState.emit(UiState.Success(Unit))
                                reloadTrigger.tryEmit(Unit)
                            },
                            onFailure = { e ->
                                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
                            }
                        )
                    },
                    onFailure = { e ->
                        _actionState.emit(
                            UiState.Error(
                                getString(R.string.error_matricula_no_encontrada),
                                ErrorType.GENERAL
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
            }
        }
    }

    suspend fun getStudentName(idMatricula: Long): String? {
        return matriculaRepository.buscarPorId(idMatricula).fold(
            onSuccess = { matricula ->
                alumnoRepository.buscarPorId(matricula.pkAlumno).fold(
                    onSuccess = { alumno -> alumno.nombre },
                    onFailure = { null }
                )
            },
            onFailure = { null }
        )
    }

    private fun <T> Result<T>.toUiState(): UiState<T> = fold(
        { UiState.Success(it) },
        { e -> UiState.Error(e.toUserMessage(), mapErrorType(e)) }
    )

    private fun mapErrorType(throwable: Throwable): ErrorType = when {
        throwable is HttpException && throwable.code() in 400..499 -> ErrorType.VALIDATION
        throwable.message?.contains(
            "dependencias",
            ignoreCase = true
        ) == true -> ErrorType.DEPENDENCY

        else -> ErrorType.GENERAL
    }

    private fun getString(resId: Int, vararg args: Any): String {
        return resourceProvider.getString(resId, *args)
    }
}
