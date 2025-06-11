package com.example.gestionacademicaapp.ui.matricula

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(FlowPreview::class)
@ExperimentalCoroutinesApi
@HiltViewModel
class MatriculaDetailsViewModel @Inject constructor(
    private val matriculaRepository: MatriculaRepository,
    private val cicloRepository: CicloRepository,
    private val grupoRepository: GrupoRepository
) : ViewModel() {

    val reloadTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    var idAlumno: Long = 0L
        private set
    var idCarrera: Long = 0L
        private set
    var selectedCicloId: Long? = null
        private set

    val ciclos: StateFlow<UiState<List<Ciclo>>> = flow {
        emit(UiState.Loading)
        val result = cicloRepository.listar()
        result.onSuccess { ciclos ->
            emit(UiState.Success(ciclos))
            // Only set active cycle if selectedCicloId is null and no cycle was set by setParams
            if (selectedCicloId == null) {
                val activeCycle =
                    ciclos.firstOrNull { it.estado.equals("activo", ignoreCase = true) }
                activeCycle?.let { setCiclo(it.idCiclo) }
            }
        }.onFailure {
            emit(UiState.Error(it.toUserMessage(), mapErrorType(it)))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val matriculasState: StateFlow<UiState<List<MatriculaAlumnoDto>>> = reloadTrigger
        .debounce(50) // Add debounce to prevent rapid emissions
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                println("Iniciando obtención de matrículas para alumno=$idAlumno, ciclo=$selectedCicloId")
                try {
                    selectedCicloId?.let { cicloId ->
                        val matriculas =
                            matriculaRepository.listarPorAlumnoYCiclo(idAlumno, cicloId)
                        println("Matrículas obtenidas: ${matriculas.getOrNull()?.size ?: 0}, resultado=$matriculas")
                        emit(matriculas.toUiState())
                    } ?: emit(UiState.Error("Seleccione un ciclo", ErrorType.VALIDATION))
                } catch (e: Exception) {
                    println("Error al obtener matrículas: ${e.message}")
                    emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _actionState = MutableStateFlow<UiState<Unit>?>(null)
    val actionState: StateFlow<UiState<Unit>?> = _actionState.asStateFlow()

    fun setParams(idAlumno: Long, idCarrera: Long, idCiclo: Long? = null) {
        println("Estableciendo parámetros: idAlumno=$idAlumno, idCarrera=$idCarrera, idCiclo=$idCiclo")
        this.idAlumno = idAlumno
        this.idCarrera = idCarrera
        // Reset selectedCicloId to avoid stale state
        selectedCicloId = null
        idCiclo?.let { setCiclo(it, forceReload = true) }
            ?: reloadTrigger.tryEmit(Unit) // Trigger only if no cycle provided
    }

    fun setCiclo(idCiclo: Long, forceReload: Boolean = false) {
        if (selectedCicloId != idCiclo || forceReload) {
            selectedCicloId = idCiclo
            println("Ciclo establecido: idCiclo=$idCiclo, forceReload=$forceReload")
            reloadTrigger.tryEmit(Unit)
        } else {
            println("Ciclo no cambiado: idCiclo=$idCiclo, selectedCicloId=$selectedCicloId")
        }
    }

    // New: Fetch grupo by matriculaId for edit mode
    suspend fun getGrupoByMatriculaId(matriculaId: Long): Result<GrupoDto> {
        return grupoRepository.buscarGrupoPorMatricula(matriculaId).fold(
            onSuccess = { grupo -> Result.success(grupo) },
            onFailure = { e -> Result.failure(e) }
        )
    }

    fun deleteMatricula(idMatricula: Long) {
        viewModelScope.launch {
            _actionState.emit(UiState.Loading)
            try {
                val matriculaExists = matriculaRepository.buscarPorId(idMatricula).isSuccess
                if (!matriculaExists) {
                    _actionState.emit(
                        UiState.Error(
                            "Matrícula no encontrada",
                            ErrorType.VALIDATION
                        )
                    )
                    return@launch
                }
                matriculaRepository.eliminar(idMatricula).fold(
                    onSuccess = {
                        _actionState.emit(UiState.Success(Unit))
                        reloadTrigger.tryEmit(Unit)
                    },
                    onFailure = { e ->
                        _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
                    }
                )
            } catch (e: Exception) {
                _actionState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
            }
        }
    }

    private fun <T> Result<T>.toUiState(transform: (T) -> T = { it }): UiState<T> = fold(
        onSuccess = { UiState.Success(transform(it)) },
        onFailure = { e -> UiState.Error(e.toUserMessage(), mapErrorType(e)) }
    )

    private fun mapErrorType(throwable: Throwable): ErrorType = when {
        throwable is HttpException && throwable.code() in 400..499 -> ErrorType.VALIDATION
        throwable.message?.contains(
            "dependencias",
            ignoreCase = true
        ) == true -> ErrorType.DEPENDENCY

        else -> ErrorType.GENERAL
    }
}
