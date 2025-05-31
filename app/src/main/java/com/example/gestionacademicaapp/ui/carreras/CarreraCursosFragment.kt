package com.example.gestionacademicaapp.ui.carreras

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CarreraCursosFragment : DialogFragment() {

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
    private lateinit var dialogView: View

    @Suppress("DEPRECATION")
    private val carrera: Carrera by lazy {
        val result = arguments?.getParcelable<Carrera>("carrera")
        requireNotNull(result) { "Carrera is required" }
    }

    private val cursosDisponibles = mutableListOf<Curso>()
    private val ciclosDisponibles = mutableListOf<Ciclo>()
    private val carreraCursos = mutableListOf<CarreraCursoUI>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val container = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        dialogView = layoutInflater.inflate(R.layout.fragment_carrera_cursos, container, false)

        recyclerView = dialogView.findViewById(R.id.recyclerViewCarreraCursos)
        fab = dialogView.findViewById(R.id.fabAddCurso)
        progressBar = dialogView.findViewById(R.id.progressBar)
        tvTitle = dialogView.findViewById(R.id.tvTitle)

        tvTitle.text = getString(R.string.titulo_cursos_de, carrera.nombre)

        adapter = CarreraCursosAdapter(
            onDelete = { carreraCurso ->
                lifecycleScope.launch {
                    try {
                        viewModel.deleteCarreraCurso(carrera.idCarrera, carreraCurso.curso.idCurso)
                    } catch (e: Exception) {
                        Notificador.show(
                            view = dialogView,
                            mensaje = "Error al eliminar: ${e.message}",
                            colorResId = R.color.colorError
                        )
                    }
                }
            },
            onReorderRequest = { carreraCurso -> mostrarDialogoReordenarCurso(carreraCurso) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Habilitar acciones de swipe
        recyclerView.enableSwipeActions(
            onSwipeLeft = { position ->
                adapter.onSwipeDelete(position)
                // Restaurar visualmente el ítem si la eliminación falla
                recyclerView.postDelayed({
                    if (!recyclerView.isComputingLayout) {
                        adapter.notifyItemChanged(position)
                    }
                }, 300)
            },
            onSwipeRight = { position ->
                adapter.onSwipeReorder(position)
            }
        )

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
                        view = dialogView,
                        mensaje = state.data,
                        colorResId = R.color.colorAccent,
                        anchorView = fab
                    )
                    cargarCarreraCursos()
                }

                is SingleUiState.Error -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(
                        view = dialogView,
                        mensaje = state.message,
                        colorResId = R.color.colorError
                    )
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create().also { dialog ->
                dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_window)

                dialogView.findViewById<TextView>(R.id.btnCerrar).setOnClickListener {
                    dialog.dismiss()
                }
            }
    }

    private fun mostrarDialogoReordenarCurso(carreraCurso: CarreraCursoUI) {
        val cursoIndex =
            carreraCursos.indexOfFirst { it.idCarreraCurso == carreraCurso.idCarreraCurso }

        val dialog = DialogFormularioFragment.newInstance(
            titulo = "Reordenar ${carreraCurso.curso.nombre}",
            campos = listOf(
                CampoFormulario(
                    key = "idCiclo",
                    label = "Nuevo Ciclo",
                    tipo = "spinner",
                    obligatorio = true,
                    opciones = ciclosDisponibles.map {
                        it.idCiclo.toString() to "${it.anio} - ${it.numero}"
                    }
                )
            ),
            datosIniciales = mapOf("idCiclo" to carreraCurso.cicloId.toString())
        )

        dialog.setOnGuardarListener { datosMap ->
            val nuevoIdCiclo = datosMap["idCiclo"]?.toLongOrNull()
            if (nuevoIdCiclo != null) {
                val nuevoCiclo = ciclosDisponibles.find { it.idCiclo == nuevoIdCiclo }
                if (nuevoCiclo != null) {
                    val updatedCarreraCurso = CarreraCurso(
                        idCarreraCurso = carreraCurso.idCarreraCurso,
                        pkCarrera = carrera.idCarrera,
                        pkCurso = carreraCurso.curso.idCurso,
                        pkCiclo = nuevoCiclo.idCiclo
                    )
                    viewModel.updateCarreraCurso(updatedCarreraCurso)
                } else {
                    Notificador.show(dialogView, "Ciclo no encontrado", R.color.colorError)
                }
            }
        }

        dialog.setOnCancelListener {
            if (cursoIndex != -1) {
                adapter.notifyItemChanged(cursoIndex)
            }
        }

        dialog.show(parentFragmentManager, "DialogReordenarCurso")
    }

    private fun cargarDatosIniciales() {
        cursosViewModel.cursosState.observe(this) { state ->
            when (state) {
                is ListUiState.Success -> {
                    cursosDisponibles.clear()
                    cursosDisponibles.addAll(state.data)
                    cargarCarreraCursos()
                }

                is ListUiState.Error -> {
                    Notificador.show(dialogView, state.message, R.color.colorError)
                }

                else -> {}
            }
        }
        cursosViewModel.fetchCursos()

        ciclosViewModel.ciclosState.observe(this) { state ->
            when (state) {
                is ListUiState.Success -> {
                    ciclosDisponibles.clear()
                    ciclosDisponibles.addAll(state.data)
                    cargarCarreraCursos()
                }

                is ListUiState.Error -> {
                    Notificador.show(dialogView, state.message, R.color.colorError)
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

            val carreraCursosResponse = try {
                carreraCursoRepository.listar().getOrNull() ?: emptyList()
            } catch (e: Exception) {
                Notificador.show(
                    dialogView,
                    "Error al cargar cursos: ${e.message}",
                    R.color.colorError
                )
                emptyList()
            }

            val cursosAsociados = carreraCursosResponse
                .filter { it.pkCarrera == carrera.idCarrera }
                .mapNotNull { carreraCurso ->
                    val curso = cursosDisponibles.find { it.idCurso == carreraCurso.pkCurso }
                    val ciclo = ciclosDisponibles.find { it.idCiclo == carreraCurso.pkCiclo }
                    curso?.let {
                        CarreraCursoUI(
                            idCarreraCurso = carreraCurso.idCarreraCurso,
                            carreraId = carrera.idCarrera,
                            curso = curso,
                            cicloId = carreraCurso.pkCiclo,
                            ciclo = ciclo
                        )
                    }
                }

            val cursosAsociadosUnicos = cursosAsociados.distinctBy { it.curso.idCurso }

            carreraCursos.clear()
            carreraCursos.addAll(cursosAsociadosUnicos)
            adapter.submitList(cursosAsociadosUnicos)

            progressBar.isVisible = false
        }
    }

    private fun mostrarDialogoAgregarCurso() {
        val cursosNoAsociados = cursosDisponibles.filter { curso ->
            !carreraCursos.any { it.curso.idCurso == curso.idCurso }
        }

        if (cursosNoAsociados.isEmpty()) {
            Notificador.show(
                dialogView,
                "No hay cursos disponibles para agregar",
                R.color.colorError
            )
            return
        }

        val campos = listOf(
            CampoFormulario(
                key = "idCurso",
                label = "Curso",
                tipo = "spinner",
                obligatorio = true,
                opciones = cursosNoAsociados.map {
                    it.idCurso.toString() to it.nombre
                }
            ),
            CampoFormulario(
                key = "idCiclo",
                label = "Ciclo",
                tipo = "spinner",
                obligatorio = true,
                opciones = ciclosDisponibles.map {
                    it.idCiclo.toString() to "${it.anio} - ${it.numero}"
                }
            )
        )

        val dialog = DialogFormularioFragment.newInstance(
            titulo = "Agregar Curso a ${carrera.nombre}",
            campos = campos
        )

        dialog.setOnGuardarListener { datosMap ->
            val idCurso = datosMap["idCurso"]?.toLongOrNull()
            val idCiclo = datosMap["idCiclo"]?.toLongOrNull()
            if (idCurso != null && idCiclo != null) {
                val nuevoCarreraCurso = CarreraCurso(
                    idCarreraCurso = 0,
                    pkCarrera = carrera.idCarrera,
                    pkCurso = idCurso,
                    pkCiclo = idCiclo
                )
                viewModel.createCarreraCurso(nuevoCarreraCurso)
            } else {
                Notificador.show(dialogView, "Datos inválidos", R.color.colorError)
            }
        }

        dialog.show(parentFragmentManager, "DialogAgregarCurso")
    }
}
