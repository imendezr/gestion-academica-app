package com.example.gestionacademicaapp.ui.matricula

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.data.repository.CicloRepository
import com.example.gestionacademicaapp.data.repository.MatriculaRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

@ExperimentalCoroutinesApi
@HiltViewModel
class MatriculaDetailsViewModel @Inject constructor(
    private val matriculaRepository: MatriculaRepository,
    private val cicloRepository: CicloRepository // Add CicloRepository){}
) : ViewModel() {

    val reloadTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    var idAlumno: Long = 0L
        private set
    var idCarrera: Long = 0L
        private set
    var selectedCicloId: Long? = null // Track selected cycle

    val ciclos: StateFlow<UiState<List<Ciclo>>> = flow {
        emit(UiState.Loading)
        val result = cicloRepository.listar()
        result.onSuccess { ciclos ->
            emit(UiState.Success(ciclos))
            val activeCycle = ciclos.firstOrNull { it.estado.equals("activo", ignoreCase = true) }
            if (activeCycle != null && selectedCicloId == null) {
                setCiclo(activeCycle.idCiclo)
            }
        }.onFailure {
            emit(UiState.Error(it.toUserMessage(), mapErrorType(it)))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val matriculasState: StateFlow<UiState<List<MatriculaAlumnoDto>>> = reloadTrigger
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                println("Iniciando obtención de matrículas para alumno=$idAlumno, ciclo=$selectedCicloId")
                try {
                    selectedCicloId?.let { cicloId ->
                        val matriculas = matriculaRepository.listarPorAlumnoYCiclo(idAlumno, cicloId)
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

    init {
        reloadTrigger.tryEmit(Unit)
    }

    fun setParams(idAlumno: Long, idCarrera: Long, idCiclo: Long? = null) {
        println("Estableciendo parámetros: idAlumno=$idAlumno, idCarrera=$idCarrera, idCiclo=$idCiclo")
        this.idAlumno = idAlumno
        this.idCarrera = idCarrera
        idCiclo?.let { setCiclo(it) }
        reloadTrigger.tryEmit(Unit)
    }

    fun setCiclo(idCiclo: Long) {
        if (selectedCicloId != idCiclo) {
            selectedCicloId = idCiclo
            println("Ciclo establecido: $idCiclo")
            reloadTrigger.tryEmit(Unit)
        }
    }

    fun deleteMatricula(idMatricula: Long) {
        viewModelScope.launch {
            _actionState.emit(UiState.Loading)
            try {
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
        throwable.message?.contains("dependencias", ignoreCase = true) == true -> ErrorType.DEPENDENCY
        else -> ErrorType.GENERAL
    }
}
