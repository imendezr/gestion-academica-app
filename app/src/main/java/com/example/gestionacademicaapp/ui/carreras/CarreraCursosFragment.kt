package com.example.gestionacademicaapp.ui.carreras

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.ui.carreras.model.CarreraCursoUI
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.clearSwipe
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    private var itemTouchHelper: ItemTouchHelper? = null
    private var swipedPosition: Int? = null

    private val carrera: Carrera by lazy {
        arguments?.getParcelableCompat<Carrera>("carrera")
            ?: throw IllegalArgumentException("Carrera is required")
    }

    private var allCarreraCursos: List<CarreraCursoUI> = emptyList()
    private var editingPosition: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val parent = FrameLayout(requireContext())
        dialogView = layoutInflater.inflate(R.layout.fragment_carrera_cursos, parent, false)

        recyclerView = dialogView.findViewById(R.id.recyclerViewCarreraCursos)
        fab = dialogView.findViewById(R.id.fabAddCurso)
        progressBar = dialogView.findViewById(R.id.progressBar)
        tvTitle = dialogView.findViewById(R.id.tvTitle)

        tvTitle.text = getString(R.string.titulo_cursos_de, carrera.nombre)

        adapter = CarreraCursosAdapter(
            onEdit = { carreraCurso ->
                editingPosition = allCarreraCursos.indexOf(carreraCurso)
                mostrarDialogoEditarCurso(carreraCurso)
            },
            onDelete = { carreraCurso ->
                viewModel.deleteItem(carrera.idCarrera, carreraCurso.curso.idCurso)
            }
        )

        val localAdapter = adapter
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = localAdapter
            itemTouchHelper = enableSwipeActions(
                onSwipeLeft = { pos ->
                    swipedPosition = pos
                    localAdapter.getItemAt(pos).let {
                        viewModel.deleteItem(carrera.idCarrera, it.curso.idCurso)
                    }
                },
                onSwipeRight = { pos ->
                    editingPosition = pos
                    mostrarDialogoEditarCurso(localAdapter.getItemAt(pos))
                }
            )
        }

        dialogView.findViewById<TextView>(R.id.btnCerrar).setOnClickListener { dismiss() }

        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
            .apply { window?.setBackgroundDrawableResource(R.drawable.bg_dialog_window) }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage")
    override fun onStart() {
        super.onStart()
        fab.setOnClickListener {
            if (isAdded) {
                editingPosition = null
                mostrarDialogoAgregarCurso()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.fetchItems(carrera.idCarrera).collectLatest { updateUiState(it, isAction = false) } }
                launch { viewModel.actionState.collectLatest { updateUiState(it, isAction = true) } }
            }
        }
        viewModel.triggerReload()
    }

    private fun updateUiState(state: UiState<*>, isAction: Boolean) {
        progressBar.isVisible = state is UiState.Loading
        fab.isEnabled = state !is UiState.Loading
        recyclerView.isVisible = allCarreraCursos.isNotEmpty() || state is UiState.Success

        when (state) {
            is UiState.Success -> {
                if (!isAction && state.data is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    allCarreraCursos = state.data as List<CarreraCursoUI>
                    recyclerView.isVisible = true
                    adapter.submitList(allCarreraCursos)
                }
                state.message?.let { message ->
                    val color = when {
                        message.contains("agregado", ignoreCase = true) || message.contains("actualizado", ignoreCase = true) -> R.color.colorAccent
                        else -> R.color.colorError
                    }
                    Notificador.show(dialogView, message, color, anchorView = fab)
                }
                if (isAction) {
                    adapter.submitList(allCarreraCursos)
                    editingPosition = null
                    swipedPosition = null
                }
            }
            is UiState.Error -> {
                val message = when (state.type) {
                    ErrorType.DEPENDENCY -> getString(R.string.error_grupos_asociados)
                    ErrorType.VALIDATION -> when {
                        state.message.contains("ya existe", ignoreCase = true) -> getString(R.string.error_curso_ya_asociado)
                        state.message.contains("no se realizó", ignoreCase = true) || state.message.contains("no existe", ignoreCase = true) -> getString(R.string.error_relacion_no_existe)
                        else -> getString(R.string.error_formulario_desc, state.message)
                    }
                    ErrorType.GENERAL -> state.message
                }
                Notificador.show(dialogView, message, R.color.colorError, anchorView = fab)
                if (isAction) {
                    adapter.submitList(allCarreraCursos)
                    editingPosition?.let { pos -> adapter.notifyItemChanged(pos) }
                    swipedPosition?.let { pos ->
                        adapter.notifyItemChanged(pos)
                        recyclerView.clearSwipe(pos, itemTouchHelper)
                        swipedPosition = null
                    }
                    viewModel.triggerReload()
                }
            }
            is UiState.Loading -> {}
        }
    }

    private fun mostrarDialogoEditarCurso(carreraCurso: CarreraCursoUI) {
        if (!isAdded) return
        lifecycleScope.launch {
            try {
                val (_, ciclosDisponibles) = viewModel.cursosAndCiclos.first { it.second.isNotEmpty() }
                if (ciclosDisponibles.isEmpty()) {
                    Notificador.show(dialogView, getString(R.string.error_formulario_desc, "No hay ciclos disponibles"), R.color.colorError, anchorView = fab)
                    viewModel.triggerReload()
                    return@launch
                }

                val validator = CarreraCursoValidator(emptyList(), ciclosDisponibles, allCarreraCursos, carrera.idCarrera)
                val campos = listOf(
                    CampoFormulario(
                        key = "idCiclo",
                        label = getString(R.string.label_ciclo_titulo),
                        tipo = CampoTipo.SPINNER,
                        obligatorio = true,
                        obligatorioError = validator.cicloRequiredError,
                        opciones = ciclosDisponibles.map { it.idCiclo.toString() to getString(R.string.label_ciclo, "${it.anio}-${it.numero}") },
                        rules = { value, _ -> validator.validateCiclo(value) }
                    )
                )

                DialogFormularioFragment.newInstance(
                    titulo = getString(R.string.titulo_editar_ciclo_curso, carreraCurso.curso.nombre),
                    campos = campos,
                    datosIniciales = mapOf("idCiclo" to carreraCurso.cicloId.toString())
                ).apply {
                    setOnGuardarListener { datosMap ->
                        datosMap["idCiclo"]?.toLongOrNull()?.let { idCiclo ->
                            viewModel.updateItem(
                                CarreraCurso(
                                    idCarreraCurso = carreraCurso.idCarreraCurso,
                                    pkCarrera = carrera.idCarrera,
                                    pkCurso = carreraCurso.curso.idCurso,
                                    pkCiclo = idCiclo
                                )
                            )
                        }
                    }
                    setOnCancelListener { editingPosition?.let { adapter.notifyItemChanged(it) } }
                    showDialogSafely(this, "DialogEditarCurso")
                }
            } catch (e: Exception) {
                Notificador.show(dialogView, "Error al abrir el formulario", R.color.colorError, anchorView = fab)
            }
        }
    }

    private fun mostrarDialogoAgregarCurso() {
        if (!isAdded) return
        lifecycleScope.launch {
            try {
                val (cursosDisponibles, ciclosDisponibles) = viewModel.cursosAndCiclos.first { it.first.isNotEmpty() && it.second.isNotEmpty() }
                if (cursosDisponibles.isEmpty() || ciclosDisponibles.isEmpty()) {
                    Notificador.show(dialogView, getString(R.string.error_formulario_desc, "No hay cursos o ciclos disponibles"), R.color.colorError, anchorView = fab)
                    viewModel.triggerReload()
                    return@launch
                }

                val cursosNoAsociados = cursosDisponibles.filter { curso ->
                    !allCarreraCursos.any { it.curso.idCurso == curso.idCurso }
                }
                if (cursosNoAsociados.isEmpty()) {
                    Notificador.show(dialogView, getString(R.string.error_curso_no_disponible), R.color.colorError, anchorView = fab)
                    return@launch
                }

                val validator = CarreraCursoValidator(cursosNoAsociados, ciclosDisponibles, allCarreraCursos, carrera.idCarrera)
                val campos = listOf(
                    CampoFormulario(
                        key = "idCurso",
                        label = getString(R.string.label_curso),
                        tipo = CampoTipo.SPINNER,
                        obligatorio = true,
                        obligatorioError = validator.cursoRequiredError,
                        opciones = cursosNoAsociados.map { it.idCurso.toString() to it.nombre },
                        rules = { value, _ -> validator.validateCurso(value) }
                    ),
                    CampoFormulario(
                        key = "idCiclo",
                        label = getString(R.string.label_ciclo_titulo),
                        tipo = CampoTipo.SPINNER,
                        obligatorio = true,
                        obligatorioError = validator.cicloRequiredError,
                        opciones = ciclosDisponibles.map { it.idCiclo.toString() to getString(R.string.label_ciclo, "${it.anio}-${it.numero}") },
                        rules = { value, _ -> validator.validateCiclo(value) }
                    )
                )

                DialogFormularioFragment.newInstance(
                    titulo = getString(R.string.titulo_agregar_curso, carrera.nombre),
                    campos = campos
                ).apply {
                    setOnGuardarListener { datosMap ->
                        val idCurso = datosMap["idCurso"]?.toLongOrNull()
                        val idCiclo = datosMap["idCiclo"]?.toLongOrNull()
                        if (idCurso != null && idCiclo != null) {
                            viewModel.createItem(
                                CarreraCurso(
                                    idCarreraCurso = 0,
                                    pkCarrera = carrera.idCarrera,
                                    pkCurso = idCurso,
                                    pkCiclo = idCiclo
                                )
                            )
                        }
                    }
                    showDialogSafely(this, "DialogAgregarCurso")
                }
            } catch (e: Exception) {
                Notificador.show(dialogView, "Error al abrir el formulario", R.color.colorError, anchorView = fab)
            }
        }
    }

    private fun showDialogSafely(dialogFragment: DialogFragment, tag: String) {
        if (isAdded && !requireActivity().isFinishing) {
            dialogFragment.show(requireActivity().supportFragmentManager, tag)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemTouchHelper = null
    }

    // Extension for backward-compatible Parcelable retrieval
    private inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelable(key) as? T
        }
}
