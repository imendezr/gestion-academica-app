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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.databinding.FragmentOfertaAcademicaBinding
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.setupSearchView
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
    private val adapter = OfertaAcademicaAdapter(
        onVerGrupos = { curso ->
            val action = OfertaAcademicaFragmentDirections.actionOfertaAcademicaFragmentToGruposOfertaFragment(
                cursoId = curso.idCurso,
                cursoNombre = curso.nombre
            )
            findNavController().navigate(action)
        },
        onEditGrupo = {},
        onDeleteGrupo = {}
    )
    private var isCarreraSpinnerInitialized = false
    private var isCicloSpinnerInitialized = false
    private var allItems: List<CursoDto> = emptyList()

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
        setupSearchView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
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

    private fun setupSearchView() {
        setupSearchView(
            searchView = binding.searchView,
            hint = getString(R.string.buscar_curso),
            onQueryTextChange = { query -> filterList(query) }
        )
    }

    private fun setupFab() {
        binding.fabBottom.isVisible = false
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cursos.collect { updateCursos(it) }
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
                    anchorView = binding.fabBottom,
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
                    anchorView = binding.fabBottom,
                    duracion = 2000
                )
            }
            is UiState.Loading -> Unit
        }
    }

    private fun updateCursos(state: UiState<List<CursoDto>>) {
        binding.progressBar.isVisible = state is UiState.Loading
        binding.recyclerView.isVisible = state is UiState.Success && allItems.isNotEmpty()
        when (state) {
            is UiState.Success -> {
                allItems = state.data ?: emptyList()
                filterList(binding.searchView.query.toString())
                if (allItems.isEmpty()) {
                    Notificador.show(
                        view = binding.root,
                        mensaje = getString(R.string.error_no_cursos),
                        colorResId = R.color.colorError,
                        anchorView = binding.fabBottom,
                        duracion = 2000
                    )
                }
            }
            is UiState.Error -> {
                allItems = emptyList()
                adapter.submitCursos(emptyList())
                binding.recyclerView.isVisible = false
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = binding.fabBottom,
                    duracion = 2000
                )
            }
            is UiState.Loading -> {
                allItems = emptyList()
                adapter.submitCursos(emptyList())
            }
        }
    }

    private fun filterList(query: String?) {
        val filtered = if (query.isNullOrBlank()) allItems else allItems.filter {
            it.nombre.contains(query, ignoreCase = true) || it.codigo.contains(query, ignoreCase = true)
        }
        adapter.submitCursos(filtered)
        binding.recyclerView.isVisible = filtered.isNotEmpty()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
