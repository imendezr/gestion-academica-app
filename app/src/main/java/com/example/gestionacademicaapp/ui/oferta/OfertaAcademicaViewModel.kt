package com.example.gestionacademicaapp.ui.oferta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Grupo
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.repository.CarreraRepository
import com.example.gestionacademicaapp.data.repository.CicloRepository
import com.example.gestionacademicaapp.data.repository.GrupoRepository
import com.example.gestionacademicaapp.data.repository.ProfesorRepository
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.ResourceProvider
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
class OfertaAcademicaViewModel @Inject constructor(
    private val carreraRepository: CarreraRepository,
    private val cicloRepository: CicloRepository,
    private val grupoRepository: GrupoRepository,
    private val profesorRepository: ProfesorRepository,
    private val resourceProvider: ResourceProvider // Added ResourceProvider
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    private var idCarrera: Long? = null
    private var idCiclo: Long? = null
    private var idCurso: Long? = null
    var cursoNombre: String? = null
    private var profesores: List<Profesor> = emptyList()

    val carreras: StateFlow<UiState<List<Carrera>>> = flow {
        emit(UiState.Loading)
        emit(carreraRepository.listar().toUiState())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val ciclos: StateFlow<UiState<List<Ciclo>>> = flow {
        emit(UiState.Loading)
        emit(cicloRepository.listar().toUiState())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val cursos: StateFlow<UiState<List<CursoDto>>> = reloadTrigger
        .debounce(100)
        .flatMapLatest {
            if (idCarrera == null || idCiclo == null) flowOf(UiState.Success(emptyList()))
            else flow {
                emit(UiState.Loading)
                emit(grupoRepository.cursosPorCarreraYCiclo(idCarrera!!, idCiclo!!).toUiState())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    val grupos: StateFlow<UiState<List<GrupoDto>>> = reloadTrigger
        .debounce(100)
        .flatMapLatest {
            if (idCarrera == null || idCiclo == null || idCurso == null) flowOf(UiState.Success(emptyList()))
            else flow {
                emit(UiState.Loading)
                emit(grupoRepository.gruposPorCursoCicloCarrera(idCurso!!, idCiclo!!, idCarrera!!).toUiState())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Success(emptyList()))

    private val _actionState = MutableStateFlow<UiState<Unit>?>(null)
    val actionState: StateFlow<UiState<Unit>?> = _actionState.asStateFlow()

    init {
        reloadTrigger.tryEmit(Unit)
        loadProfesores()
    }

    private fun loadProfesores() {
        viewModelScope.launch {
            profesorRepository.listar().fold(
                onSuccess = { profesores = it },
                onFailure = { /* Handle silently for now */ }
            )
        }
    }

    fun setCarrera(idCarrera: Long) {
        this.idCarrera = idCarrera
        reloadTrigger.tryEmit(Unit)
    }

    fun setCiclo(idCiclo: Long) {
        this.idCiclo = idCiclo
        reloadTrigger.tryEmit(Unit)
    }

    fun setCurso(idCurso: Long, nombre: String) {
        this.idCurso = idCurso
        this.cursoNombre = nombre
        reloadTrigger.tryEmit(Unit)
    }

    fun reloadCursos() {
        reloadTrigger.tryEmit(Unit)
    }

    fun reloadGrupos() {
        reloadTrigger.tryEmit(Unit)
    }

    fun saveGrupo(idGrupo: Long?, numeroGrupo: Long, horario: String, idProfesor: Long) {
        viewModelScope.launch {
            _actionState.emit(UiState.Loading)
            try {
                val grupoValidator = GrupoValidator()
                val errors = grupoValidator.validate(numeroGrupo, horario, idProfesor)
                if (errors.isNotEmpty()) {
                    _actionState.emit(UiState.Error(errors.joinToString(", "), ErrorType.VALIDATION))
                    return@launch
                }

                val grupo = Grupo(
                    idGrupo = idGrupo ?: 0L,
                    idCarreraCurso = idCurso ?: 0L,
                    numeroGrupo = numeroGrupo,
                    horario = horario,
                    idProfesor = idProfesor
                )

                if (idGrupo == null) {
                    grupoRepository.insertar(grupo).fold(
                        onSuccess = {
                            _actionState.emit(UiState.Success(Unit, "CREATED"))
                            reloadTrigger.tryEmit(Unit) // Trigger refresh
                        },
                        onFailure = { e -> _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e))) }
                    )
                } else {
                    grupoRepository.modificar(grupo).fold(
                        onSuccess = {
                            _actionState.emit(UiState.Success(Unit, "UPDATED"))
                            reloadTrigger.tryEmit(Unit) // Trigger refresh
                        },
                        onFailure = { e -> _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e))) }
                    )
                }
            } catch (e: Exception) {
                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
            }
        }
    }

    fun deleteGrupo(grupo: GrupoDto) {
        viewModelScope.launch {
            _actionState.emit(UiState.Loading)
            try {
                grupoRepository.eliminar(grupo.idGrupo).fold(
                    onSuccess = {
                        _actionState.emit(UiState.Success(Unit, "DELETED"))
                        reloadTrigger.tryEmit(Unit) // Trigger refresh
                    },
                    onFailure = { e -> _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e))) }
                )
            } catch (e: Exception) {
                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
            }
        }
    }

    fun getFormFields(): List<CampoFormulario> {
        return listOf(
            CampoFormulario(
                key = "numeroGrupo",
                label = getString(R.string.numero_grupo),
                tipo = CampoTipo.NUMBER,
                rules = { value, _ -> GrupoValidator().validateNumeroGrupo(value) },
                obligatorio = true
            ),
            CampoFormulario(
                key = "horario",
                label = getString(R.string.horario),
                tipo = CampoTipo.TEXT,
                rules = { value, _ -> GrupoValidator().validateHorario(value) },
                obligatorio = true
            ),
            CampoFormulario(
                key = "profesor",
                label = getString(R.string.profesor),
                tipo = CampoTipo.SPINNER,
                opciones = profesores.map { Pair(it.idProfesor.toString(), it.nombre) },
                rules = { value, _ -> GrupoValidator().validateProfesor(value) },
                obligatorio = true
            )
        )
    }

    private fun <T> Result<T>.toUiState(): UiState<T> = fold(
        onSuccess = { UiState.Success(it) },
        onFailure = { e -> UiState.Error(e.toUserMessage(), mapErrorType(e)) }
    )

    private fun mapErrorType(throwable: Throwable): ErrorType = when {
        throwable is HttpException && throwable.code() in 400..499 -> ErrorType.VALIDATION
        throwable.message?.contains("dependencias", ignoreCase = true) == true -> ErrorType.DEPENDENCY
        else -> ErrorType.GENERAL
    }

    private fun getString(resId: Int, vararg args: Any): String {
        return resourceProvider.getString(resId, *args) // Updated to use ResourceProvider
    }
}
