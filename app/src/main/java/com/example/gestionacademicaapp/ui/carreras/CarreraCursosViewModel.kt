package com.example.gestionacademicaapp.ui.carreras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.data.repository.CicloRepository
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.ui.carreras.model.CarreraCursoUI
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CarreraCursosViewModel @Inject constructor(
    private val carreraCursoRepository: CarreraCursoRepository,
    private val cursoRepository: CursoRepository,
    private val cicloRepository: CicloRepository
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val cursosFlow = flow {
        cursoRepository.listar().fold(
            onSuccess = { emit(it) },
            onFailure = { throw IllegalStateException(it.message) }
        )
    }

    private val ciclosFlow = flow {
        cicloRepository.listar().fold(
            onSuccess = { emit(it) },
            onFailure = { throw IllegalStateException(it.message) }
        )
    }

    private val carreraCursosFlow = flow {
        carreraCursoRepository.listar().fold(
            onSuccess = { emit(it) },
            onFailure = { throw IllegalStateException(it.message) }
        )
    }

    @OptIn(FlowPreview::class)
    fun fetchItems(carreraId: Long) = reloadTrigger.debounce(300).flatMapLatest {
        combine(cursosFlow, ciclosFlow, carreraCursosFlow) { cursos, ciclos, carreraCursos ->
            val cursosAsociados = carreraCursos
                .filter { it.pkCarrera == carreraId }
                .mapNotNull { carreraCurso ->
                    cursos.find { it.idCurso == carreraCurso.pkCurso }?.let { curso ->
                        CarreraCursoUI(
                            idCarreraCurso = carreraCurso.idCarreraCurso,
                            carreraId = carreraId,
                            curso = curso,
                            cicloId = carreraCurso.pkCiclo,
                            ciclo = ciclos.find { it.idCiclo == carreraCurso.pkCiclo }
                        )
                    }
                }
            UiState.Success(cursosAsociados)
        }.catch { emit(UiState.Success(emptyList(), message = it.toUserMessage())) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Success())
    val actionState: StateFlow<UiState<Unit>> = _actionState.asStateFlow()

    val cursosAndCiclos: StateFlow<Pair<List<Curso>, List<Ciclo>>> = combine(cursosFlow, ciclosFlow) { cursos, ciclos ->
        cursos to ciclos
    }.catch { emit(emptyList<Curso>() to emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Curso>() to emptyList())

    fun triggerReload() {
        viewModelScope.launch {
            reloadTrigger.emit(Unit)
        }
    }

    fun createItem(carreraCurso: CarreraCurso) {
        viewModelScope.launch {
            performAction("Curso agregado a la carrera") {
                validateCarreraCurso(carreraCurso)
                carreraCursoRepository.insertar(carreraCurso).getOrThrow()
            }
        }
    }

    fun updateItem(carreraCurso: CarreraCurso) {
        viewModelScope.launch {
            performAction("Orden del curso actualizado") {
                validateCarreraCurso(carreraCurso)
                carreraCursoRepository.modificar(carreraCurso).getOrThrow()
            }
        }
    }

    fun deleteItem(idCarrera: Long, idCurso: Long) {
        viewModelScope.launch {
            performAction("Curso eliminado de la carrera") {
                if (carreraCursoRepository.tieneGruposAsociados(idCarrera, idCurso).getOrDefault(false)) {
                    throw IllegalStateException("No se puede eliminar: tiene grupos asociados")
                }
                carreraCursoRepository.eliminar(idCarrera, idCurso).getOrThrow()
            }
        }
    }

    private fun validateCarreraCurso(carreraCurso: CarreraCurso) {
        if (carreraCurso.pkCarrera <= 0) throw IllegalArgumentException("Carrera inválida")
        if (carreraCurso.pkCurso <= 0) throw IllegalArgumentException("Curso inválido")
        if (carreraCurso.pkCiclo <= 0) throw IllegalArgumentException("Ciclo inválido")
    }

    private suspend fun performAction(successMessage: String, action: suspend () -> Unit) {
        _actionState.value = UiState.Loading
        try {
            action()
            _actionState.value = UiState.Success(message = successMessage)
            triggerReload()
        } catch (e: Exception) {
            _actionState.value = UiState.Error(e.toUserMessage(), mapErrorType(e))
        }
    }

    private fun mapErrorType(throwable: Throwable): ErrorType = when {
        throwable.message?.lowercase()?.contains("grupos asociados") == true -> ErrorType.DEPENDENCY
        throwable.message?.lowercase()?.contains("ya existe") == true ||
                throwable.message?.lowercase()?.contains("no se realizó") == true ||
                throwable.message?.lowercase()?.contains("no existe") == true -> ErrorType.VALIDATION
        else -> ErrorType.GENERAL
    }
}
