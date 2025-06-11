package com.example.gestionacademicaapp.ui.matricula

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.databinding.FragmentMatriculaDetailsBinding
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.clearSwipe
import com.example.gestionacademicaapp.utils.enableSwipeActions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MatriculaDetailsFragment : Fragment() {

    private val viewModel: MatriculaDetailsViewModel by viewModels()
    private val matriculaViewModel: MatriculaViewModel by viewModels()
    private var _binding: FragmentMatriculaDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MatriculaDetailsAdapter
    private var allItems: List<MatriculaAlumnoDto> = emptyList()
    private var swipedPosition: Int? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var isSpinnerInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatriculaDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(MatriculaDetailsFragmentArgs.fromBundle(requireArguments())) {
            viewModel.setParams(idAlumno, idCarrera, idCiclo)
            matriculaViewModel.setParams(idAlumno, idCiclo, idCarrera)
        }
        setupRecyclerView()
        setupSpinner()
        setupFab()
        observeViewModel()
        // Forzar el fetch inicial después de la configuración
        viewModel.reloadTrigger.tryEmit(Unit)
    }

    private fun setupRecyclerView() {
        val localAdapter = MatriculaDetailsAdapter(
            onEdit = { matricula ->
                findNavController().navigate(
                    MatriculaDetailsFragmentDirections.actionMatriculaDetailsFragmentToMatriculaCursoGrupoFragment(
                        idAlumno = viewModel.idAlumno,
                        idCiclo = viewModel.selectedCicloId ?: 0L,
                        idCarrera = viewModel.idCarrera,
                        idGrupo = 0L, // Not needed for edit mode
                        idMatricula = matricula.idMatricula // Pass matricula ID directly
                    )
                )
            }
        )
        adapter = localAdapter
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = localAdapter
            itemTouchHelper = enableSwipeActions(
                onSwipeLeft = { position ->
                    swipedPosition = position
                    val matricula = localAdapter.getItemAt(position)
                    viewModel.deleteMatricula(matricula.idMatricula)
                },
                onSwipeRight = { position ->
                    swipedPosition = position
                    val matricula = localAdapter.getItemAt(position)
                    findNavController().navigate(
                        MatriculaDetailsFragmentDirections.actionMatriculaDetailsFragmentToMatriculaCursoGrupoFragment(
                            idAlumno = viewModel.idAlumno,
                            idCiclo = viewModel.selectedCicloId ?: 0L,
                            idCarrera = viewModel.idCarrera,
                            idGrupo = 0L, // Not needed for edit mode
                            idMatricula = matricula.idMatricula // Pass matricula ID directly
                        )
                    )
                }
            )
        }
    }

    private fun setupSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ciclos.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            val ciclos = state.data ?: emptyList()
                            if (ciclos.isEmpty()) {
                                Notificador.show(
                                    view = binding.root,
                                    mensaje = getString(R.string.error_no_ciclos),
                                    colorResId = R.color.colorError,
                                    anchorView = binding.fab,
                                    duracion = 2000
                                )
                            }
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                ciclos.map { "${it.anio}-${it.numero}" }
                            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                            binding.spinnerCiclo.adapter = adapter
                            binding.spinnerCiclo.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                    ) {
                                        ciclos.getOrNull(position)?.let { ciclo ->
                                            viewModel.setCiclo(ciclo.idCiclo, forceReload = true)
                                            println("Spinner selected ciclo: ${ciclo.idCiclo}")
                                        }
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>) = Unit
                                }
                            // Seleccionar ciclo inicial solo si no está inicializado
                            if (!isSpinnerInitialized && ciclos.isNotEmpty()) {
                                val selectedIndex =
                                    ciclos.indexOfFirst { it.idCiclo == viewModel.selectedCicloId }
                                val indexToSet = if (selectedIndex >= 0) selectedIndex else 0
                                binding.spinnerCiclo.setSelection(indexToSet, false)
                                isSpinnerInitialized = true
                                println("Initial spinner selection: index=$indexToSet, cicloId=${viewModel.selectedCicloId}")
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
            }
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            viewModel.selectedCicloId?.let { cicloId ->
                findNavController().navigate(
                    MatriculaDetailsFragmentDirections.actionMatriculaDetailsFragmentToMatriculaCursoGrupoFragment(
                        idAlumno = viewModel.idAlumno,
                        idCiclo = cicloId,
                        idCarrera = viewModel.idCarrera,
                        idGrupo = 0L
                    )
                )
            } ?: Notificador.show(
                view = binding.root,
                mensaje = getString(R.string.error_seleccione_ciclo),
                colorResId = R.color.colorError,
                anchorView = binding.fab,
                duracion = 2000
            )
        }
    }

    private fun observeViewModel() {
        observeState(viewModel.matriculasState) { updateMatriculasState(it) }
        observeState(viewModel.actionState) { it?.let { updateActionState(it) } }
        // Agregar observación de matriculaUpdated
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                matriculaViewModel.matriculaUpdated.collect {
                    println("Matricula updated, triggering reload")
                    viewModel.reloadTrigger.tryEmit(Unit)
                }
            }
        }
    }

    private fun <T> observeState(flow: Flow<T>, block: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { block(it) }
            }
        }
    }

    private fun updateMatriculasState(state: UiState<List<MatriculaAlumnoDto>>) {
        binding.progressBar.isVisible = state is UiState.Loading
        when (state) {
            is UiState.Success -> {
                allItems = state.data ?: emptyList()
                adapter.submitList(allItems)
                binding.recyclerView.isVisible = allItems.isNotEmpty()
                if (allItems.isEmpty() && isSpinnerInitialized) {
                    Notificador.show(
                        view = binding.root,
                        mensaje = getString(R.string.no_matriculas_ciclo),
                        colorResId = R.color.colorTextLink,
                        anchorView = binding.fab,
                        duracion = 2000
                    )
                }
            }

            is UiState.Error -> {
                allItems = emptyList()
                adapter.submitList(emptyList())
                binding.recyclerView.isVisible = false
                if (isSpinnerInitialized) {
                    Notificador.show(
                        view = binding.root,
                        mensaje = when (state.type) {
                            ErrorType.VALIDATION -> getString(R.string.error_no_matriculas_ciclo)
                            else -> state.message
                        },
                        colorResId = R.color.colorError,
                        anchorView = binding.fab,
                        duracion = 2000
                    )
                }
            }

            is UiState.Loading -> {
                allItems = emptyList()
                adapter.submitList(emptyList())
                binding.recyclerView.isVisible = false
            }
        }
    }

    private fun updateActionState(state: UiState<Unit>) {
        when (state) {
            is UiState.Success -> {
                Notificador.show(
                    view = binding.root,
                    mensaje = getString(R.string.matricula_eliminada_exitosa),
                    colorResId = R.color.colorAccent,
                    anchorView = binding.fab,
                    duracion = 2000
                )
                swipedPosition?.let { position ->
                    adapter.notifyItemChanged(position)
                    binding.recyclerView.clearSwipe(position, itemTouchHelper)
                    swipedPosition = null
                } ?: println("Warning: swipedPosition is null on success state")
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(2000)
                    viewModel.reloadTrigger.tryEmit(Unit)
                }
            }
            is UiState.Error -> {
                Notificador.show(
                    view = binding.root,
                    mensaje = when (state.type) {
                        ErrorType.DEPENDENCY -> getString(R.string.error_matricula_dependencia)
                        else -> state.message
                    },
                    colorResId = R.color.colorError,
                    anchorView = binding.fab,
                    duracion = 2000
                )
                swipedPosition?.let { position ->
                    adapter.notifyItemChanged(position)
                    binding.recyclerView.clearSwipe(position, itemTouchHelper)
                    swipedPosition = null
                } ?: println("Warning: swipedPosition is null on error state")
            }
            is UiState.Loading -> Unit
        }
    }

    override fun onDestroyView() {
        itemTouchHelper = null
        _binding = null
        super.onDestroyView()
    }
}
