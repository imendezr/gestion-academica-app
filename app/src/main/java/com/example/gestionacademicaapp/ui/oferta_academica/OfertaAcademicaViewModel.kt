package com.example.gestionacademicaapp.ui.oferta_academica

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
import com.example.gestionacademicaapp.ui.common.validators.GrupoValidator
import com.example.gestionacademicaapp.utils.ResourceProvider
import com.example.gestionacademicaapp.utils.mapErrorType
import com.example.gestionacademicaapp.utils.toUiState
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

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class OfertaAcademicaViewModel @Inject constructor(
    private val carreraRepository: CarreraRepository,
    private val cicloRepository: CicloRepository,
    private val grupoRepository: GrupoRepository,
    private val profesorRepository: ProfesorRepository,
    private val resourceProvider: ResourceProvider,
    private val grupoValidator: GrupoValidator
) : ViewModel() {

    private val courseReloadTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    private val groupReloadTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    var idCarrera: Long? = null
        private set
    var idCiclo: Long? = null
        private set
    var idCurso: Long? = null
        private set
    private var cursoNombre: String? = null
    private val _actionState = MutableStateFlow<UiState<Unit>?>(null)
    val actionState: StateFlow<UiState<Unit>?> = _actionState.asStateFlow()

    val carreras: StateFlow<UiState<List<Carrera>>> = flow {
        emit(UiState.Loading)
        emit(carreraRepository.listar().toUiState())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val ciclos: StateFlow<UiState<List<Ciclo>>> = flow {
        emit(UiState.Loading)
        emit(cicloRepository.listar().toUiState())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val profesoresState: StateFlow<UiState<List<Profesor>>> = flow {
        emit(UiState.Loading)
        emit(profesorRepository.listar().toUiState())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val cursos: StateFlow<UiState<List<CursoDto>>> = courseReloadTrigger
        .debounce(100)
        .flatMapLatest {
            if (idCarrera == null || idCiclo == null) flowOf(UiState.Success(emptyList()))
            else flow {
                emit(UiState.Loading)
                emit(grupoRepository.cursosPorCarreraYCiclo(idCarrera!!, idCiclo!!).toUiState())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading) // Cambiado a UiState.Loading

    val grupos: StateFlow<UiState<List<GrupoDto>>> = groupReloadTrigger
        .debounce(100)
        .flatMapLatest {
            if (idCarrera == null || idCiclo == null || idCurso == null) {
                flowOf(UiState.Success(emptyList()))
            } else {
                flow {
                    emit(UiState.Loading)
                    emit(
                        grupoRepository.gruposPorCursoCicloCarrera(
                            idCurso!!,
                            idCiclo!!,
                            idCarrera!!
                        ).toUiState()
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading) // Cambiado a UiState.Loading

    init {
        courseReloadTrigger.tryEmit(Unit)
    }

    fun setCarrera(idCarrera: Long) {
        if (this.idCarrera != idCarrera) {
            this.idCarrera = idCarrera
            this.idCurso = null
            this.cursoNombre = null
            courseReloadTrigger.tryEmit(Unit)
        }
    }

    fun setCiclo(idCiclo: Long) {
        if (this.idCiclo != idCiclo) {
            this.idCiclo = idCiclo
            this.idCurso = null
            this.cursoNombre = null
            courseReloadTrigger.tryEmit(Unit)
        }
    }

    fun setCurso(idCurso: Long, nombre: String) {
        if (this.idCurso != idCurso) {
            this.idCurso = idCurso
            this.cursoNombre = nombre
            groupReloadTrigger.tryEmit(Unit)
        }
    }

    fun saveGrupo(idGrupo: Long?, numeroGrupo: Long, horario: String, idProfesor: Long) {
        viewModelScope.launch {
            _actionState.emit(UiState.Loading)
            try {
                val errors = grupoValidator.validate(numeroGrupo, horario, idProfesor)
                if (errors.isNotEmpty()) {
                    _actionState.emit(
                        UiState.Error(
                            errors.joinToString(", "),
                            ErrorType.VALIDATION
                        )
                    )
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
                            groupReloadTrigger.tryEmit(Unit)
                        },
                        onFailure = { e ->
                            _actionState.emit(
                                UiState.Error(
                                    e.toUserMessage(),
                                    mapErrorType(e)
                                )
                            )
                        }
                    )
                } else {
                    grupoRepository.modificar(grupo).fold(
                        onSuccess = {
                            _actionState.emit(UiState.Success(Unit, "UPDATED"))
                            groupReloadTrigger.tryEmit(Unit)
                        },
                        onFailure = { e ->
                            _actionState.emit(
                                UiState.Error(
                                    e.toUserMessage(),
                                    mapErrorType(e)
                                )
                            )
                        }
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
                        groupReloadTrigger.tryEmit(Unit)
                    },
                    onFailure = { e ->
                        _actionState.emit(
                            UiState.Error(
                                e.toUserMessage(),
                                mapErrorType(e)
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
            }
        }
    }

    fun getFormFields(): List<CampoFormulario> {
        val profesorOptions = when (val state = profesoresState.value) {
            is UiState.Success -> state.data?.map { Pair(it.idProfesor.toString(), it.nombre) }
                ?: listOf(Pair("0", "Sin profesores disponibles"))

            else -> listOf(Pair("0", "Sin profesores disponibles"))
        }
        return listOf(
            CampoFormulario(
                key = "numeroGrupo",
                label = resourceProvider.getString(R.string.numero_grupo),
                tipo = CampoTipo.NUMBER,
                rules = { value, _ -> grupoValidator.validateNumeroGrupo(value) },
                obligatorio = true,
                obligatorioError = GrupoValidator.ERROR_NUMERO_GRUPO_REQUERIDO
            ),
            CampoFormulario(
                key = "horario",
                label = resourceProvider.getString(R.string.horario),
                tipo = CampoTipo.TEXT,
                rules = { value, _ -> grupoValidator.validateHorario(value) },
                obligatorio = true,
                obligatorioError = GrupoValidator.ERROR_HORARIO_REQUERIDO
            ),
            CampoFormulario(
                key = "profesor",
                label = resourceProvider.getString(R.string.profesor),
                tipo = CampoTipo.SPINNER,
                opciones = profesorOptions,
                rules = { value, _ -> grupoValidator.validateProfesor(value) },
                obligatorio = true,
                obligatorioError = GrupoValidator.ERROR_PROFESOR_REQUERIDO
            )
        )
    }
}
