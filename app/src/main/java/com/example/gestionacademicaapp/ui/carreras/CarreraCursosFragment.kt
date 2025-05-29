package com.example.gestionacademicaapp.ui.carreras

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.ui.carreras.model.CarreraCursoUI
import com.example.gestionacademicaapp.ui.ciclos.CiclosViewModel
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.ui.cursos.CursosViewModel
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CarreraCursosFragment : DialogFragment() {

    // Inyectar el repositorio
    @Inject
    lateinit var carreraCursoRepository: CarreraCursoRepository

    private val viewModel: CarrerasViewModel by viewModels()
    private val cursosViewModel: CursosViewModel by viewModels()
    private val ciclosViewModel: CiclosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CarreraCursosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View
    private lateinit var tvTitle: TextView

    private val carrera: Carrera by lazy {
        arguments?.getParcelable("carrera") ?: throw IllegalArgumentException("Carrera is required")
    }

    private val cursosDisponibles = mutableListOf<Curso>()
    private val ciclosDisponibles = mutableListOf<Ciclo>()
    private val carreraCursos = mutableListOf<CarreraCursoUI>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_carrera_cursos, null)

        recyclerView = view.findViewById(R.id.recyclerViewCarreraCursos)
        fab = view.findViewById(R.id.fabAddCurso)
        progressBar = view.findViewById(R.id.progressBar)
        tvTitle = view.findViewById(R.id.tvTitle)

        tvTitle.text = "Cursos de ${carrera.nombre}"

        adapter = CarreraCursosAdapter(
            onDelete = { carreraCurso ->
                viewModel.deleteCarreraCurso(carrera.idCarrera, carreraCurso.curso.idCurso)
            },
            onReorder = { carreraCurso, newCiclo ->
                val updatedCarreraCurso = CarreraCurso(
                    idCarreraCurso = carreraCurso.idCarreraCurso,
                    pkCarrera = carrera.idCarrera,
                    pkCurso = carreraCurso.curso.idCurso,
                    pkCiclo = newCiclo.idCiclo
                )
                viewModel.updateCarreraCurso(updatedCarreraCurso)
            },
            onReorderRequest = { carreraCurso ->
                mostrarDialogoReordenarCurso(carreraCurso)
            },
            ciclosDisponibles = ciclosDisponibles
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            mostrarDialogoAgregarCurso()
        }

        cargarDatosIniciales()

        viewModel.actionState.observe(this) { state ->
            when (state) {
                is SingleUiState.Loading -> {
                    progressBar.isVisible = true
                    fab.isEnabled = false
                }

                is SingleUiState.Success -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(
                        view = requireView(),
                        mensaje = state.data,
                        colorResId = R.color.colorAccent,
                        anchorView = fab
                    )
                    cargarCarreraCursos()
                }

                is SingleUiState.Error -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setNegativeButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private fun mostrarDialogoReordenarCurso(carreraCurso: CarreraCursoUI) {
        val dialog = DialogFormularioFragment(
            titulo = "Reordenar ${carreraCurso.curso.nombre}",
            campos = listOf(
                CampoFormulario(
                    key = "idCiclo",
                    label = "Nuevo Ciclo",
                    tipo = "select",
                    obligatorio = true,
                    opciones = ciclosDisponibles.map { it.idCiclo.toString() to "${it.anio} - ${it.numero}" }
                )
            ),
            datosIniciales = mapOf("idCiclo" to carreraCurso.cicloId.toString()),
            onGuardar = { datosMap ->
                val nuevoIdCiclo =
                    datosMap["idCiclo"]?.toLongOrNull() ?: return@DialogFormularioFragment
                val nuevoCiclo = ciclosDisponibles.find { it.idCiclo == nuevoIdCiclo }
                if (nuevoCiclo != null) {
                    viewModel.updateCarreraCurso(
                        CarreraCurso(
                            idCarreraCurso = carreraCurso.idCarreraCurso,
                            pkCarrera = carrera.idCarrera,
                            pkCurso = carreraCurso.curso.idCurso,
                            pkCiclo = nuevoCiclo.idCiclo
                        )
                    )
                }
            }
        )
        dialog.show(parentFragmentManager, "DialogReordenarCurso")
    }

    private fun cargarDatosIniciales() {
        // Cargar cursos disponibles
        cursosViewModel.cursosState.observe(this) { state ->
            when (state) {
                is ListUiState.Success -> {
                    cursosDisponibles.clear()
                    cursosDisponibles.addAll(state.data)
                    cargarCarreraCursos()
                }

                is ListUiState.Error -> {
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }

                else -> {}
            }
        }
        cursosViewModel.fetchCursos()

        // Cargar ciclos disponibles
        ciclosViewModel.ciclosState.observe(this) { state ->
            when (state) {
                is ListUiState.Success -> {
                    ciclosDisponibles.clear()
                    ciclosDisponibles.addAll(state.data)
                    cargarCarreraCursos()
                }

                is ListUiState.Error -> {
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }

                else -> {}
            }
        }
        ciclosViewModel.fetchCiclos()
    }

    private fun cargarCarreraCursos() {
        lifecycleScope.launch {
            if (ciclosDisponibles.isEmpty() || cursosDisponibles.isEmpty()) return@launch
            progressBar.isVisible = true

            val carreraCursosResponse = carreraCursoRepository.listar().getOrNull() ?: emptyList()
            val cursosAsociados = mutableListOf<CarreraCursoUI>()
            carreraCursosResponse
                .filter { it.pkCarrera == carrera.idCarrera }
                .forEach { carreraCurso ->
                    val curso = cursosDisponibles.find { it.idCurso == carreraCurso.pkCurso }
                    val ciclo = ciclosDisponibles.find { it.idCiclo == carreraCurso.pkCiclo }
                    if (curso != null) {
                        cursosAsociados.add(
                            CarreraCursoUI(
                                idCarreraCurso = carreraCurso.idCarreraCurso,
                                carreraId = carrera.idCarrera,
                                curso = curso,
                                cicloId = carreraCurso.pkCiclo,
                                ciclo = ciclo
                            )
                        )
                    }
                }
            carreraCursos.clear()
            carreraCursos.addAll(cursosAsociados)
            adapter.updateCarreraCursos(carreraCursos)
            progressBar.isVisible = false
        }
    }

    private fun mostrarDialogoAgregarCurso() {
        val cursosNoAsociados = cursosDisponibles.filter { curso ->
            !carreraCursos.any { it.curso.idCurso == curso.idCurso }
        }
        if (cursosNoAsociados.isEmpty()) {
            Notificador.show(
                requireView(),
                "No hay cursos disponibles para agregar",
                R.color.colorError
            )
            return
        }

        val campos = listOf(
            CampoFormulario(
                key = "idCurso",
                label = "Curso",
                tipo = "select",
                obligatorio = true,
                opciones = cursosNoAsociados.map { it.idCurso.toString() to it.nombre }
            ),
            CampoFormulario(
                key = "idCiclo",
                label = "Ciclo",
                tipo = "select",
                obligatorio = true,
                opciones = ciclosDisponibles.map { it.idCiclo.toString() to "${it.anio} - ${it.numero}" }
            )
        )

        val dialog = DialogFormularioFragment(
            titulo = "Agregar Curso a ${carrera.nombre}",
            campos = campos,
            onGuardar = { datosMap ->
                val idCurso = datosMap["idCurso"]?.toLongOrNull() ?: return@DialogFormularioFragment
                val idCiclo = datosMap["idCiclo"]?.toLongOrNull() ?: return@DialogFormularioFragment
                val nuevoCarreraCurso = CarreraCurso(
                    idCarreraCurso = 0,
                    pkCarrera = carrera.idCarrera,
                    pkCurso = idCurso,
                    pkCiclo = idCiclo
                )
                viewModel.createCarreraCurso(nuevoCarreraCurso)
            }
        )
        dialog.show(parentFragmentManager, "DialogAgregarCurso")
    }
}
