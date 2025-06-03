package com.example.gestionacademicaapp.ui.carreras

import android.util.Log
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

    init {
        Log.d("CarreraCursosViewModel", "ViewModel inicializado")
    }

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1).also { emit ->
        Log.d("CarreraCursosViewModel", "Emitiendo reloadTrigger inicial")
        emit.tryEmit(Unit)
    }

    private val cursosFlow = flow {
        Log.d("CarreraCursosViewModel", "Iniciando carga de cursos...")
        val result = cursoRepository.listar()
        Log.d("CarreraCursosViewModel", "Resultado de cursoRepository.listar: $result")
        result
            .onSuccess { emit(it) }
            .onFailure { throw IllegalStateException("Error al cargar cursos: ${it.message}") }
    }.catch {
        Log.e("CarreraCursosViewModel", "Error en cursosFlow: ${it.message}", it)
        throw it
    }

    private val ciclosFlow = flow {
        Log.d("CarreraCursosViewModel", "Iniciando carga de ciclos...")
        val result = cicloRepository.listar()
        Log.d("CarreraCursosViewModel", "Resultado de cicloRepository.listar: $result")
        result
            .onSuccess { emit(it) }
            .onFailure { throw IllegalStateException("Error al cargar ciclos: ${it.message}") }
    }.catch {
        Log.e("CarreraCursosViewModel", "Error en ciclosFlow: ${it.message}", it)
        throw it
    }

    private val carreraCursosFlow = flow {
        Log.d("CarreraCursosViewModel", "Iniciando carga de relaciones carrera-curso...")
        val result = carreraCursoRepository.listar()
        Log.d("CarreraCursosViewModel", "Resultado de carreraCursoRepository.listar: $result")
        result
            .onSuccess { emit(it) }
            .onFailure { throw IllegalStateException("Error al cargar relaciones carrera-curso: ${it.message}") }
    }.catch {
        Log.e("CarreraCursosViewModel", "Error en carreraCursosFlow: ${it.message}", it)
        throw it
    }

    fun fetchItems(carreraId: Long) = reloadTrigger.flatMapLatest {
        Log.d("CarreraCursosViewModel", "flatMapLatest ejecutado para carreraId=$carreraId")
        combine(cursosFlow, ciclosFlow, carreraCursosFlow) { cursos, ciclos, carreraCursosResponse ->
            Log.d("CarreraCursosViewModel", "Datos recibidos: cursos=${cursos.size}, ciclos=${ciclos.size}, carreraCursos=${carreraCursosResponse.size}")
            val cursosAsociados = carreraCursosResponse
                .filter { it.pkCarrera == carreraId }
                .mapNotNull { carreraCurso ->
                    val curso = cursos.find { it.idCurso == carreraCurso.pkCurso }
                    val ciclo = ciclos.find { it.idCiclo == carreraCurso.pkCiclo }
                    if (curso == null) {
                        Log.w("CarreraCursosViewModel", "Curso no encontrado: idCurso=${carreraCurso.pkCurso}")
                        null
                    } else {
                        CarreraCursoUI(
                            idCarreraCurso = carreraCurso.idCarreraCurso,
                            carreraId = carreraId,
                            curso = curso,
                            cicloId = carreraCurso.pkCiclo,
                            ciclo = ciclo
                        )
                    }
                }
                .distinctBy { it.curso.idCurso }
            Log.d("CarreraCursosViewModel", "Cursos asociados para carreraId=$carreraId: ${cursosAsociados.size}")
            if (cursosAsociados.isEmpty()) {
                if (carreraCursosResponse.any { it.pkCarrera == carreraId }) {
                    UiState.Error("No se encontraron cursos válidos para la carrera", ErrorType.GENERAL)
                } else {
                    UiState.Success(emptyList())
                }
            } else {
                UiState.Success(cursosAsociados)
            }
        }.catch {
            Log.e("CarreraCursosViewModel", "Error en combine: ${it.message}", it)
            emit(UiState.Error(it.toUserMessage(), ErrorType.GENERAL))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Success())
    val actionState: StateFlow<UiState<Unit>> get() = _actionState

    val cursosAndCiclos: StateFlow<Pair<List<Curso>, List<Ciclo>>> = combine(cursosFlow, ciclosFlow) { cursos, ciclos ->
        Log.d("CarreraCursosViewModel", "Cursos y ciclos combinados: cursos=${cursos.size}, ciclos=${ciclos.size}")
        Pair(cursos, ciclos)
    }.catch {
        Log.e("CarreraCursosViewModel", "Error en cursosAndCiclos: ${it.message}", it)
        emit(Pair(emptyList(), emptyList()))
    }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(emptyList(), emptyList()))

    fun triggerReload() {
        viewModelScope.launch {
            Log.d("CarreraCursosViewModel", "Forzando recarga de datos")
            reloadTrigger.emit(Unit)
        }
    }

    fun createItem(carreraCurso: CarreraCurso) {
        viewModelScope.launch {
            performAction {
                validateCarreraCurso(carreraCurso)
                Log.d("CarreraCursosViewModel", "Insertando carreraCurso: $carreraCurso")
                carreraCursoRepository.insertar(carreraCurso)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Curso agregado a la carrera")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    fun updateItem(carreraCurso: CarreraCurso) {
        viewModelScope.launch {
            performAction {
                validateCarreraCurso(carreraCurso)
                Log.d("CarreraCursosViewModel", "Actualizando carreraCurso: $carreraCurso")
                carreraCursoRepository.modificar(carreraCurso)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Orden del curso actualizado")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    fun deleteItem(idCarrera: Long, idCurso: Long) {
        viewModelScope.launch {
            performAction {
                Log.d("CarreraCursosViewModel", "Verificando grupos asociados para idCarrera=$idCarrera, idCurso=$idCurso")
                val tieneGrupos = carreraCursoRepository.tieneGruposAsociados(idCarrera, idCurso).getOrNull() ?: false
                if (tieneGrupos) {
                    throw IllegalStateException("No se puede eliminar: la relación carrera-curso tiene grupos asociados")
                }
                Log.d("CarreraCursosViewModel", "Eliminando carreraCurso: idCarrera=$idCarrera, idCurso=$idCurso")
                carreraCursoRepository.eliminar(idCarrera, idCurso)
                    .onSuccess {
                        _actionState.value = UiState.Success(message = "Curso eliminado de la carrera")
                        triggerReload()
                    }
                    .onFailure { throw it }
            }
        }
    }

    private fun validateCarreraCurso(carreraCurso: CarreraCurso) {
        Log.d("CarreraCursosViewModel", "Validando carreraCurso: $carreraCurso")
        if (carreraCurso.pkCarrera <= 0) throw IllegalArgumentException("La carrera es inválida")
        if (carreraCurso.pkCurso <= 0) throw IllegalArgumentException("El curso es inválido")
        if (carreraCurso.pkCiclo <= 0) throw IllegalArgumentException("El ciclo es inválido")
    }

    private suspend fun performAction(action: suspend () -> Unit) {
        _actionState.value = UiState.Loading
        try {
            action()
        } catch (e: Exception) {
            Log.e("CarreraCursosViewModel", "Error en performAction: ${e.message}", e)
            _actionState.value = UiState.Error(e.toUserMessage(), mapErrorType(e))
        }
    }

    private fun mapErrorType(throwable: Throwable): ErrorType {
        val message = throwable.toUserMessage().lowercase()
        return when {
            message.contains("grupos asociados") -> ErrorType.DEPENDENCY
            message.contains("ya existe") || message.contains("no se realizó") ||
                    message.contains("no existe") -> ErrorType.VALIDATION
            else -> ErrorType.GENERAL
        }
    }
}
