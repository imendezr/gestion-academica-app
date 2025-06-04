package com.example.gestionacademicaapp.ui.oferta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.databinding.FragmentOfertaAcademicaBinding
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.clearSwipe
import com.example.gestionacademicaapp.utils.enableSwipeActions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OfertaAcademicaFragment : Fragment() {

    private val viewModel: OfertaAcademicaViewModel by viewModels()
    private var _binding: FragmentOfertaAcademicaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OfertaAcademicaAdapter
    private var isCarreraSpinnerInitialized = false
    private var isCicloSpinnerInitialized = false
    private var vistaActual: String = "cursos" // "cursos" or "grupos"
    private var itemTouchHelper: ItemTouchHelper? = null
    private var swipedPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfertaAcademicaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSpinners()
        setupFab()
        setupVolverButton()
        observeViewModel()
        updateUi()
    }

    private fun setupRecyclerView() {
        adapter = OfertaAcademicaAdapter(
            onVerGrupos = { curso ->
                viewModel.setCurso(curso.idCurso, curso.nombre)
                vistaActual = "grupos"
                updateUi()
            },
            onEditGrupo = { grupo -> mostrarFormulario(grupo) },
            onDeleteGrupo = { grupo ->
                swipedPosition = adapter.getItemPosition(grupo)
                viewModel.deleteGrupo(grupo)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@OfertaAcademicaFragment.adapter
        }
    }

    private fun setupSpinners() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.carreras.collect { updateCarreras(it) } }
                launch { viewModel.ciclos.collect { updateCiclos(it) } }
            }
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener { mostrarFormulario(null) }
        binding.fab.contentDescription = getString(R.string.fab_agregar)
    }

    private fun setupVolverButton() {
        binding.btnVolver.setOnClickListener {
            vistaActual = "cursos"
            updateUi()
        }
        binding.btnVolver.contentDescription = getString(R.string.volver)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.cursos.collect { updateCursos(it) } }
                launch { viewModel.grupos.collect { updateGrupos(it) } }
                launch { viewModel.actionState.collect { it?.let { updateActionState(it) } } }
            }
        }
    }

    private fun updateCarreras(state: UiState<List<Carrera>>) {
        when (state) {
            is UiState.Success -> {
                val carreras = state.data ?: emptyList()
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    carreras.map { it.nombre }
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                binding.spinnerCarrera.adapter = adapter
                binding.spinnerCarrera.contentDescription = getString(R.string.seleccionar_carrera)
                binding.spinnerCarrera.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (!isCarreraSpinnerInitialized) {
                                isCarreraSpinnerInitialized = true
                                return
                            }
                            carreras.getOrNull(position)?.let { carrera ->
                                viewModel.setCarrera(carrera.idCarrera)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) = Unit
                    }
                if (carreras.isNotEmpty() && !isCarreraSpinnerInitialized) {
                    binding.spinnerCarrera.setSelection(0)
                    isCarreraSpinnerInitialized = true
                }
            }

            is UiState.Error -> {
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = binding.fab,
                    duracion = 2000
                )
            }

            is UiState.Loading -> Unit
        }
    }

    private fun updateCiclos(state: UiState<List<Ciclo>>) {
        when (state) {
            is UiState.Success -> {
                val ciclos = state.data ?: emptyList()
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    ciclos.map { "${it.numero}-${it.anio}" }
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                binding.spinnerCiclo.adapter = adapter
                binding.spinnerCiclo.contentDescription = getString(R.string.seleccionar_ciclo)
                binding.spinnerCiclo.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (!isCicloSpinnerInitialized) {
                                isCicloSpinnerInitialized = true
                                return
                            }
                            ciclos.getOrNull(position)?.let { ciclo ->
                                viewModel.setCiclo(ciclo.idCiclo)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) = Unit
                    }
                if (ciclos.isNotEmpty() && !isCicloSpinnerInitialized) {
                    binding.spinnerCiclo.setSelection(0)
                    isCicloSpinnerInitialized = true
                }
            }

            is UiState.Error -> {
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = binding.fab,
                    duracion = 2000
                )
            }

            is UiState.Loading -> Unit
        }
    }

    private fun updateCursos(state: UiState<List<CursoDto>>) {
        if (vistaActual != "cursos") return
        binding.progressBar.isVisible = state is UiState.Loading
        binding.recyclerView.isVisible = state is UiState.Success && (state.data?.isNotEmpty() ?: false)
        when (state) {
            is UiState.Success -> {
                val cursos = state.data ?: emptyList()
                adapter.submitCursos(cursos)
                if (cursos.isEmpty()) {
                    Notificador.show(
                        view = binding.root,
                        mensaje = getString(R.string.error_no_cursos),
                        colorResId = R.color.colorError,
                        anchorView = binding.fab,
                        duracion = 2000
                    )
                }
            }

            is UiState.Error -> {
                adapter.submitCursos(emptyList())
                binding.recyclerView.isVisible = false
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = binding.fab,
                    duracion = 2000
                )
            }

            is UiState.Loading -> adapter.submitCursos(emptyList())
        }
    }

    private fun updateGrupos(state: UiState<List<GrupoDto>>) {
        if (vistaActual != "grupos") return
        binding.progressBar.isVisible = state is UiState.Loading
        binding.recyclerView.isVisible = state is UiState.Success && (state.data?.isNotEmpty() ?: false)
        when (state) {
            is UiState.Success -> {
                val grupos = state.data ?: emptyList()
                adapter.submitGrupos(grupos)
                android.util.Log.d("OfertaAcademicaFragment", "Updating grupos: $grupos") // Debug log
                if (grupos.isEmpty()) {
                    Notificador.show(
                        view = binding.root,
                        mensaje = getString(R.string.error_no_grupos),
                        colorResId = R.color.colorError,
                        anchorView = binding.fab,
                        duracion = 2000
                    )
                }
            }
            is UiState.Error -> {
                adapter.submitGrupos(emptyList())
                binding.recyclerView.isVisible = false
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = binding.fab,
                    duracion = 2000
                )
            }
            is UiState.Loading -> adapter.submitGrupos(emptyList())
        }
    }

    private fun updateActionState(state: UiState<Unit>) {
        when (state) {
            is UiState.Success -> {
                swipedPosition = null
                Notificador.show(
                    view = binding.root,
                    mensaje = getString(
                        when (state.message) {
                            "CREATED" -> R.string.grupo_creado_exito
                            "UPDATED" -> R.string.grupo_actualizado_exitoso
                            "DELETED" -> R.string.grupo_eliminado_exitoso
                            else -> R.string.operacion_exitosa
                        }
                    ),
                    colorResId = R.color.colorAccent,
                    anchorView = binding.fab,
                    duracion = 2000
                )
            }

            is UiState.Error -> {
                swipedPosition?.let { pos ->
                    adapter.notifyItemChanged(pos)
                    binding.recyclerView.clearSwipe(pos, itemTouchHelper)
                    swipedPosition = null
                }
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = binding.fab,
                    duracion = 2000
                )
            }

            is UiState.Loading -> Unit
        }
    }

    private fun mostrarFormulario(grupo: GrupoDto?) {
        if (vistaActual != "grupos") return
        val isEditing = grupo != null
        val datosIniciales = if (isEditing) mapOf(
            "numeroGrupo" to grupo!!.numeroGrupo.toString(),
            "horario" to grupo.horario,
            "profesor" to grupo.idProfesor.toString()
        ) else emptyMap()

        val dialog = DialogFormularioFragment.newInstance(
            titulo = getString(if (isEditing) R.string.editar_grupo else R.string.crear_grupo),
            campos = viewModel.getFormFields(),
            datosIniciales = datosIniciales
        )
        dialog.setOnGuardarListener { datos ->
            viewModel.saveGrupo(
                idGrupo = grupo?.idGrupo,
                numeroGrupo = datos["numeroGrupo"]?.toLongOrNull() ?: 0L,
                horario = datos["horario"] ?: "",
                idProfesor = datos["profesor"]?.toLongOrNull() ?: 0L
            )
        }
        dialog.setOnCancelListener { _ -> }
        dialog.show(childFragmentManager, "DialogFormularioFragment")
    }

    private fun updateUi() {
        binding.fab.isVisible = vistaActual == "grupos"
        binding.btnVolver.isVisible = vistaActual == "grupos"
        binding.txtTitulo.text = getString(
            if (vistaActual == "cursos") R.string.oferta_academica else R.string.grupos_de_curso,
            viewModel.cursoNombre ?: ""
        )
        // Conditionally apply swipe actions for grupos only
        itemTouchHelper = if (vistaActual == "grupos") {
            binding.recyclerView.enableSwipeActions(
                onSwipeLeft = { position ->
                    swipedPosition = position
                    (adapter.getItemAt(position) as? GrupoDto)?.let { viewModel.deleteGrupo(it) }
                },
                onSwipeRight = { position ->
                    (adapter.getItemAt(position) as? GrupoDto)?.let { mostrarFormulario(it) }
                },
                leftBackgroundColor = R.color.colorError,
                rightBackgroundColor = R.color.colorPrimary
            )
        } else {
            null
        }
        if (vistaActual == "cursos") {
            viewModel.reloadCursos()
        } else {
            viewModel.reloadGrupos()
        }
    }

    override fun onDestroyView() {
        itemTouchHelper = null
        _binding = null
        super.onDestroyView()
    }
}