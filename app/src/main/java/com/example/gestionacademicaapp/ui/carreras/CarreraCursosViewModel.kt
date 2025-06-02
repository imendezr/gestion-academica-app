package com.example.gestionacademicaapp.ui.carreras

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.data.repository.CicloRepository
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.ui.carreras.model.CarreraCursoUI
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class) // Anotación para flatMapLatest
class CarreraCursosViewModel @Inject constructor(
    private val carreraCursoRepository: CarreraCursoRepository,
    private val cursoRepository: CursoRepository,
    private val cicloRepository: CicloRepository
) : ViewModel() {

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    private val reloadTrigger = MutableStateFlow(0)

    private val cursosFlow = flow {
        emit(cursoRepository.listar().getOrNull() ?: emptyList())
    }

    private val ciclosFlow = flow {
        emit(cicloRepository.listar().getOrNull() ?: emptyList())
    }

    private val carreraCursosFlow = flow {
        emit(carreraCursoRepository.listar().getOrNull() ?: emptyList())
    }

    fun fetchItems(carreraId: Long) = reloadTrigger.flatMapLatest {
        combine(cursosFlow, ciclosFlow, carreraCursosFlow) { cursos, ciclos, carreraCursosResponse ->
            val cursosAsociados = carreraCursosResponse
                .filter { it.pkCarrera == carreraId }
                .mapNotNull { carreraCurso ->
                    val curso = cursos.find { it.idCurso == carreraCurso.pkCurso }
                    val ciclo = ciclos.find { it.idCiclo == carreraCurso.pkCiclo }
                    curso?.let {
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
            ListUiState.Success(cursosAsociados)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ListUiState.Loading)

    fun loadCursosAndCiclos(): LiveData<Pair<List<Curso>, List<Ciclo>>> {
        return combine(cursosFlow, ciclosFlow) { cursos, ciclos ->
            Pair(cursos, ciclos)
        }.asLiveData()
    }

    private fun triggerReload() {
        reloadTrigger.value += 1
    }

    fun createItem(carreraCurso: CarreraCurso) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraCursoRepository.insertar(carreraCurso)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Curso agregado a la carrera")
                    triggerReload()
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }

    fun updateItem(carreraCurso: CarreraCurso) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraCursoRepository.modificar(carreraCurso)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Orden del curso actualizado")
                    triggerReload()
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }

    fun deleteItem(idCarrera: Long, idCurso: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            carreraCursoRepository.eliminar(idCarrera, idCurso)
                .onSuccess {
                    _actionState.value = SingleUiState.Success("Curso eliminado de la carrera")
                    triggerReload()
                }
                .onFailure { _actionState.value = SingleUiState.Error(it.toUserMessage()) }
        }
    }
}
