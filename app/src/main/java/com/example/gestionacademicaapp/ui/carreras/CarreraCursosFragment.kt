package com.example.gestionacademicaapp.ui.carreras

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private val carrera: Carrera by lazy {
        Log.d("CarreraCursosFragment", "Inicializando argumento carrera")
        val bundle = arguments
        if (bundle == null) {
            Log.e("CarreraCursosFragment", "Arguments are null")
            throw IllegalArgumentException("Arguments are required")
        }
        val carrera = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable("carrera", Carrera::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable("carrera") as? Carrera
        }
        Log.d("CarreraCursosFragment", "Carrera obtenida: $carrera")
        carrera ?: throw IllegalArgumentException("Carrera is required")
    }

    private var allCarreraCursos: List<CarreraCursoUI> = emptyList()
    private var editingPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CarreraCursosFragment", "onCreate llamado")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d("CarreraCursosFragment", "onCreateDialog llamado")
        val parent = FrameLayout(requireContext())
        dialogView = layoutInflater.inflate(R.layout.fragment_carrera_cursos, parent, false)

        recyclerView = dialogView.findViewById(R.id.recyclerViewCarreraCursos)
        fab = dialogView.findViewById(R.id.fabAddCurso)
        progressBar = dialogView.findViewById(R.id.progressBar)
        tvTitle = dialogView.findViewById(R.id.tvTitle)

        Log.d("CarreraCursosFragment", "Vistas inicializadas: recyclerView=$recyclerView, fab=$fab, progressBar=$progressBar, tvTitle=$tvTitle")
        tvTitle.text = getString(R.string.titulo_cursos_de, carrera.nombre)

        adapter = CarreraCursosAdapter(
            onEdit = { carreraCurso ->
                editingPosition = allCarreraCursos.indexOf(carreraCurso)
                Log.d("CarreraCursosFragment", "Editar curso: $carreraCurso")
                mostrarDialogoEditarCurso(carreraCurso)
            },
            onDelete = { carreraCurso ->
                Log.d("CarreraCursosFragment", "Eliminar curso: $carreraCurso")
                viewModel.deleteItem(carrera.idCarrera, carreraCurso.curso.idCurso)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        recyclerView.enableSwipeActions(
            onSwipeLeft = { pos ->
                val carreraCurso = adapter.getItemAt(pos)
                Log.d("CarreraCursosFragment", "Swipe izquierdo para eliminar: $carreraCurso, posición=$pos")
                viewModel.deleteItem(carrera.idCarrera, carreraCurso.curso.idCurso)
            },
            onSwipeRight = { pos ->
                editingPosition = pos
                val carreraCurso = adapter.getItemAt(pos)
                Log.d("CarreraCursosFragment", "Swipe derecho para editar: $carreraCurso, posición=$pos")
                mostrarDialogoEditarCurso(carreraCurso)
                adapter.notifyItemChanged(pos)
            }
        )

        dialogView.findViewById<TextView>(R.id.btnCerrar).setOnClickListener {
            Log.d("CarreraCursosFragment", "Botón cerrar clicado")
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.bg_dialog_window)
                Log.d("CarreraCursosFragment", "Diálogo creado")
            }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage") // Safe: Nested launches are required to collect two flows concurrently in a DialogFragment
    override fun onStart() {
        super.onStart()
        Log.d("CarreraCursosFragment", "onStart llamado, carreraId=${carrera.idCarrera}")

        // Configurar el listener del FAB
        fab.setOnClickListener {
            if (isAdded) {
                Log.d("CarreraCursosFragment", "FAB clicado para agregar curso")
                editingPosition = null
                mostrarDialogoAgregarCurso()
            } else {
                Log.w("CarreraCursosFragment", "FAB clicado pero fragmento no adjuntado")
            }
        }

        // Iniciar colección de estados con repeatOnLifecycle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d("CarreraCursosFragment", "Iniciando colección de estados en STARTED")
                launch {
                    viewModel.fetchItems(carrera.idCarrera).collectLatest { state ->
                        Log.d("CarreraCursosFragment", "Estado recibido: $state")
                        updateUiState(state, isAction = false)
                    }
                }
                launch {
                    viewModel.actionState.collectLatest { state ->
                        Log.d("CarreraCursosFragment", "Estado de acción recibido: $state")
                        updateUiState(state, isAction = true)
                    }
                }
            }
        }

        // Forzar recarga inicial
        Log.d("CarreraCursosFragment", "Forzando recarga inicial para carreraId=${carrera.idCarrera}")
        viewModel.triggerReload()
    }

    private fun updateUiState(state: UiState<*>, isAction: Boolean) {
        Log.d("CarreraCursosFragment", "Actualizando UI con estado: $state, isAction=$isAction")
        progressBar.isVisible = state is UiState.Loading
        fab.isEnabled = state !is UiState.Loading
        recyclerView.isVisible = !isAction && allCarreraCursos.isNotEmpty()

        when (state) {
            is UiState.Success -> {
                if (!isAction && state.data is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    allCarreraCursos = state.data as List<CarreraCursoUI>
                    Log.d("CarreraCursosFragment", "Lista actualizada: ${allCarreraCursos.size} cursos")
                    recyclerView.isVisible = allCarreraCursos.isNotEmpty()
                    adapter.submitList(allCarreraCursos)
                }
                state.message?.let { message ->
                    val color = when {
                        message.contains("agregado", true) || message.contains("actualizado", true) -> R.color.colorAccent
                        message.contains("eliminado", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }
                    Log.d("CarreraCursosFragment", "Mostrando notificación: $message")
                    Notificador.show(dialogView, message, color, anchorView = fab)
                }
                if (isAction) {
                    recyclerView.isVisible = allCarreraCursos.isNotEmpty()
                    adapter.submitList(allCarreraCursos)
                    editingPosition = null
                    // Removed: reorderingPosition = null
                }
            }
            is UiState.Error -> {
                val message = when (state.type) {
                    ErrorType.DEPENDENCY -> getString(R.string.error_grupos_asociados)
                    ErrorType.VALIDATION -> when {
                        state.message.contains("ya existe") -> getString(R.string.error_curso_ya_asociado)
                        state.message.contains("no se realizó") ||
                                state.message.contains("no existe") -> getString(R.string.error_relacion_no_existe)
                        else -> getString(R.string.error_formulario_desc, state.message)
                    }
                    ErrorType.GENERAL -> state.message
                }
                Log.e("CarreraCursosFragment", "Error: $message")
                Notificador.show(dialogView, message, R.color.colorError, anchorView = fab)
                if (isAction) {
                    adapter.submitList(allCarreraCursos)
                }
            }
            is UiState.Loading -> {
                Log.d("CarreraCursosFragment", "Cargando datos...")
            }
        }
    }

    private fun mostrarDialogoEditarCurso(carreraCurso: CarreraCursoUI) {
        if (!isAdded) {
            Log.w("CarreraCursosFragment", "No se puede mostrar diálogo: fragmento no adjuntado")
            return
        }
        lifecycleScope.launch {
            try {
                Log.d("CarreraCursosFragment", "Intentando mostrar diálogo para editar curso")
                val (_, ciclosDisponibles) = viewModel.cursosAndCiclos.first { (_, ciclos) ->
                    ciclos.isNotEmpty()
                }
                Log.d("CarreraCursosFragment", "Ciclos disponibles: ${ciclosDisponibles.size}")

                if (ciclosDisponibles.isEmpty()) {
                    val message = getString(R.string.error_formulario_desc, "No hay ciclos disponibles")
                    Log.e("CarreraCursosFragment", "Error: $message")
                    Notificador.show(dialogView, message, R.color.colorError, anchorView = fab)
                    viewModel.triggerReload()
                    return@launch
                }

                val validator = CarreraCursoValidator(emptyList(), ciclosDisponibles, allCarreraCursos, carrera.idCarrera)
                val campos = listOf(
                    CampoFormulario(
                        key = "idCiclo",
                        label = getString(R.string.label_ciclo_titulo), // Changed to new resource
                        tipo = CampoTipo.SPINNER,
                        obligatorio = true,
                        obligatorioError = validator.cicloRequiredError,
                        opciones = ciclosDisponibles.map {
                            it.idCiclo.toString() to String.format(
                                requireContext().getString(R.string.label_ciclo),
                                "${it.anio}-${it.numero}"
                            )
                        },
                        rules = { value, _ -> validator.validateCiclo(value) }
                    )
                )

                val datosIniciales = mapOf(
                    "idCiclo" to carreraCurso.cicloId.toString()
                )

                val dialog = DialogFormularioFragment.newInstance(
                    titulo = getString(R.string.titulo_editar_ciclo_curso, carreraCurso.curso.nombre),
                    campos = campos,
                    datosIniciales = datosIniciales
                )
                dialog.setOnGuardarListener { datosMap ->
                    Log.d("CarreraCursosFragment", "Guardando ciclo editado: datosMap=$datosMap")
                    datosMap["idCiclo"]?.toLongOrNull()?.let { idCiclo ->
                        val updatedCarreraCurso = CarreraCurso(
                            idCarreraCurso = carreraCurso.idCarreraCurso,
                            pkCarrera = carrera.idCarrera,
                            pkCurso = carreraCurso.curso.idCurso,
                            pkCiclo = idCiclo
                        )
                        viewModel.updateItem(updatedCarreraCurso)
                    }
                }
                dialog.setOnCancelListener {
                    editingPosition?.let { adapter.notifyItemChanged(it) }
                    Log.d("CarreraCursosFragment", "Diálogo de editar curso cancelado")
                }
                showDialogSafely(dialog, "DialogEditarCurso")
            } catch (e: Exception) {
                Log.e("CarreraCursosFragment", "Error al mostrar diálogo de edición: ${e.message}", e)
                Notificador.show(dialogView, mensaje = "Error al abrir el formulario", R.color.colorError, anchorView = fab)
            }
        }
    }

    private fun mostrarDialogoAgregarCurso() {
        if (!isAdded) {
            Log.w("CarreraCursosFragment", "No se puede mostrar diálogo: fragmento no adjuntado")
            return
        }
        lifecycleScope.launch {
            try {
                Log.d("CarreraCursosFragment", "Intentando mostrar diálogo para agregar curso")
                val (cursosDisponibles, ciclosDisponibles) = viewModel.cursosAndCiclos.first { (cursos, ciclos) ->
                    cursos.isNotEmpty() && ciclos.isNotEmpty()
                }
                Log.d("CarreraCursosFragment", "Cursos disponibles: ${cursosDisponibles.size}, Ciclos disponibles: ${ciclosDisponibles.size}")

                if (cursosDisponibles.isEmpty() || ciclosDisponibles.isEmpty()) {
                    val message = getString(R.string.error_formulario_desc, "No hay cursos o ciclos disponibles")
                    Log.e("CarreraCursosFragment", "Error: $message")
                    Notificador.show(dialogView, message, R.color.colorError, anchorView = fab)
                    viewModel.triggerReload()
                    return@launch
                }

                val cursosNoAsociados = cursosDisponibles.filter { curso ->
                    !allCarreraCursos.any { it.curso.idCurso == curso.idCurso }
                }
                Log.d("CarreraCursosFragment", "Cursos no asociados: ${cursosNoAsociados.size}")

                if (cursosNoAsociados.isEmpty()) {
                    val message = getString(R.string.error_curso_no_disponible)
                    Log.e("CarreraCursosFragment", "Error: $message")
                    Notificador.show(dialogView, message, R.color.colorError, anchorView = fab)
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
                        label = getString(R.string.label_ciclo_titulo), // Changed to new resource
                        tipo = CampoTipo.SPINNER,
                        obligatorio = true,
                        obligatorioError = validator.cicloRequiredError,
                        opciones = ciclosDisponibles.map {
                            it.idCiclo.toString() to String.format(
                                requireContext().getString(R.string.label_ciclo),
                                "${it.anio}-${it.numero}"
                            )
                        },
                        rules = { value, _ -> validator.validateCiclo(value) }
                    )
                )

                val dialog = DialogFormularioFragment.newInstance(
                    titulo = getString(R.string.titulo_agregar_curso, carrera.nombre),
                    campos = campos
                )
                dialog.setOnGuardarListener { datosMap ->
                    Log.d("CarreraCursosFragment", "Guardando curso: datosMap=$datosMap")
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
                    }
                }
                dialog.setOnCancelListener {
                    Log.d("CarreraCursosFragment", "Diálogo de agregar curso cancelado")
                }
                showDialogSafely(dialog, "DialogAgregarCurso")
            } catch (e: Exception) {
                Log.e("CarreraCursosFragment", "Error al mostrar diálogo: ${e.message}", e)
                Notificador.show(dialogView, mensaje = "Error al abrir el formulario", R.color.colorError, anchorView = fab)
            }
        }
    }

    private fun showDialogSafely(dialogFragment: DialogFragment, tag: String) {
        if (isAdded && !requireActivity().isFinishing) {
            dialogFragment.show(requireActivity().supportFragmentManager, tag)
            Log.d("CarreraCursosFragment", "$tag mostrado correctamente")
        } else {
            Log.w("CarreraCursosFragment", "No se puede mostrar $tag: fragmento no adjuntado o activity finishing")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("CarreraCursosFragment", "onResume llamado")
    }

    override fun onStop() {
        super.onStop()
        Log.d("CarreraCursosFragment", "onStop llamado")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("CarreraCursosFragment", "onDestroyView llamado")
    }
}
