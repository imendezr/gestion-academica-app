package com.example.gestionacademicaapp.ui.carreras

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.ui.carreras.model.CarreraCursoUI
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class CarreraCursosFragment : DialogFragment() {

    @Inject
    lateinit var carreraCursoRepository: CarreraCursoRepository

    private val viewModel: CarreraCursosViewModel by viewModels()

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
    private var currentEditIndex: Int? = null
    private var currentReorderIndex: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val container = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        dialogView = layoutInflater.inflate(R.layout.fragment_carrera_cursos, container, false)

        recyclerView = dialogView.findViewById(R.id.recyclerViewCarreraCursos)
        fab = dialogView.findViewById(R.id.fabAddCurso)
        progressBar = dialogView.findViewById(R.id.progressBar)
        tvTitle = dialogView.findViewById(R.id.tvTitle)

        tvTitle.text = getString(R.string.titulo_cursos_de, carrera.nombre)

        adapter = CarreraCursosAdapter(
            onEdit = { carreraCurso -> mostrarDialogoEditarCurso(carreraCurso) },
            onDelete = { carreraCurso ->
                viewModel.deleteItem(carrera.idCarrera, carreraCurso.curso.idCurso)
            },
            onReorderCarreraCurso = { carreraCurso -> mostrarDialogoReordenarCurso(carreraCurso) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        recyclerView.enableSwipeActions(
            onSwipeLeft = { pos ->
                val carreraCurso = adapter.getItemAt(pos)
                viewModel.deleteItem(carrera.idCarrera, carreraCurso.curso.idCurso)
                adapter.notifyItemChanged(pos)
            },
            onSwipeRight = { pos ->
                currentEditIndex = pos
                adapter.notifyItemChanged(pos)
                mostrarDialogoEditarCurso(adapter.getItemAt(pos))
            }
        )

        fab.setOnClickListener {
            currentEditIndex = null
            mostrarDialogoAgregarCurso()
        }

        // Observar cursos y ciclos disponibles
        viewModel.loadCursosAndCiclos().observe(this) { (cursos, ciclos) ->
            cursosDisponibles.clear()
            cursosDisponibles.addAll(cursos)
            ciclosDisponibles.clear()
            ciclosDisponibles.addAll(ciclos)
        }

        viewModel.fetchItems(carrera.idCarrera).asLiveData().observe(this) { state ->
            when (state) {
                is ListUiState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }
                is ListUiState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    carreraCursos.clear()
                    carreraCursos.addAll(state.data)
                    adapter.submitList(state.data)
                }
                is ListUiState.Error -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = false
                    Notificador.show(dialogView, state.message, R.color.colorError)
                }
            }
        }

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
                    currentEditIndex = null
                    currentReorderIndex = null
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

    private fun mostrarDialogoEditarCurso(carreraCurso: CarreraCursoUI) {
        if (cursosDisponibles.isEmpty() || ciclosDisponibles.isEmpty()) {
            Notificador.show(dialogView, "No hay cursos o ciclos disponibles", R.color.colorError)
            return
        }

        val campos = listOf(
            CampoFormulario(
                key = "idCurso",
                label = "Curso",
                tipo = CampoTipo.SPINNER,
                obligatorio = true,
                obligatorioError = "El curso es requerido",
                opciones = cursosDisponibles.map { it.idCurso.toString() to it.nombre },
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else if (cursosDisponibles.none { it.idCurso.toString() == value }) {
                        "Curso no válido"
                    } else null
                }
            ),
            CampoFormulario(
                key = "idCiclo",
                label = "Ciclo",
                tipo = CampoTipo.SPINNER,
                obligatorio = true,
                obligatorioError = "El ciclo es requerido",
                opciones = ciclosDisponibles.map { it.idCiclo.toString() to "${it.anio} - ${it.numero}" },
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else if (ciclosDisponibles.none { it.idCiclo.toString() == value }) {
                        "Ciclo no válido"
                    } else null
                }
            )
        )

        val datosIniciales = mapOf(
            "idCurso" to carreraCurso.curso.idCurso.toString(),
            "idCiclo" to carreraCurso.cicloId.toString()
        )

        val dialog = DialogFormularioFragment.newInstance(
            titulo = "Editar Curso en ${carrera.nombre}",
            campos = campos,
            datosIniciales = datosIniciales
        )

        dialog.setOnGuardarListener { datosMap ->
            val idCurso = datosMap["idCurso"]?.toLongOrNull()
            val idCiclo = datosMap["idCiclo"]?.toLongOrNull()
            if (idCurso != null && idCiclo != null) {
                val updatedCarreraCurso = CarreraCurso(
                    idCarreraCurso = carreraCurso.idCarreraCurso,
                    pkCarrera = carrera.idCarrera,
                    pkCurso = idCurso,
                    pkCiclo = idCiclo
                )
                viewModel.updateItem(updatedCarreraCurso)
            } else {
                Notificador.show(dialogView, "Datos inválidos", R.color.colorError)
            }
        }

        dialog.setOnCancelListener { index ->
            if (index != null && index >= 0) adapter.notifyItemChanged(index)
        }

        dialog.show(parentFragmentManager, "DialogEditarCurso")
    }

    private fun mostrarDialogoReordenarCurso(carreraCurso: CarreraCursoUI) {
        if (ciclosDisponibles.isEmpty()) {
            Notificador.show(dialogView, "No hay ciclos disponibles", R.color.colorError)
            return
        }

        currentReorderIndex = currentEditIndex
        val dialog = DialogFormularioFragment.newInstance(
            titulo = "Reordenar ${carreraCurso.curso.nombre}",
            campos = listOf(
                CampoFormulario(
                    key = "idCiclo",
                    label = "Nuevo Ciclo",
                    tipo = CampoTipo.SPINNER,
                    obligatorio = true,
                    obligatorioError = "El ciclo es requerido",
                    opciones = ciclosDisponibles.map { it.idCiclo.toString() to "${it.anio} - ${it.numero}" },
                    rules = { value, _ ->
                        if (value.isEmpty()) null
                        else if (ciclosDisponibles.none { it.idCiclo.toString() == value }) {
                            "Ciclo no válido"
                        } else null
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
                    viewModel.updateItem(updatedCarreraCurso)
                } else {
                    Notificador.show(dialogView, "Ciclo no encontrado", R.color.colorError)
                }
            }
        }

        dialog.setOnCancelListener { index ->
            if (index != null && index >= 0) adapter.notifyItemChanged(index)
        }

        dialog.show(parentFragmentManager, "DialogReordenarCurso")
    }

    private fun mostrarDialogoAgregarCurso() {
        if (cursosDisponibles.isEmpty() || ciclosDisponibles.isEmpty()) {
            Notificador.show(dialogView, "No hay cursos o ciclos disponibles", R.color.colorError)
            return
        }

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
                tipo = CampoTipo.SPINNER,
                obligatorio = true,
                obligatorioError = "El curso es requerido",
                opciones = cursosNoAsociados.map { it.idCurso.toString() to it.nombre },
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else if (cursosNoAsociados.none { it.idCurso.toString() == value }) {
                        "Curso no válido"
                    } else null
                }
            ),
            CampoFormulario(
                key = "idCiclo",
                label = "Ciclo",
                tipo = CampoTipo.SPINNER,
                obligatorio = true,
                obligatorioError = "El ciclo es requerido",
                opciones = ciclosDisponibles.map { it.idCiclo.toString() to "${it.anio} - ${it.numero}" },
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else if (ciclosDisponibles.none { it.idCiclo.toString() == value }) {
                        "Ciclo no válido"
                    } else null
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
                viewModel.createItem(nuevoCarreraCurso)
            } else {
                Notificador.show(dialogView, "Datos inválidos", R.color.colorError)
            }
        }

        dialog.setOnCancelListener { _ ->
            // No se necesita índice al agregar un nuevo curso
        }

        dialog.show(parentFragmentManager, "DialogAgregarCurso")
    }
}
