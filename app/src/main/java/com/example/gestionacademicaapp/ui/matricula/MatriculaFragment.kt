package com.example.gestionacademicaapp.ui.matricula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.databinding.FragmentMatriculaBinding
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MatriculaFragment : Fragment() {

    private val viewModel: MatriculaViewModel by viewModels()
    private var _binding: FragmentMatriculaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MatriculaAdapter
    private var allItems: List<Alumno> = emptyList()
    private var isSpinnerInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatriculaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val localAdapter = MatriculaAdapter(
            onClick = { alumno ->
                viewModel.selectedCicloId?.let { cicloId ->
                    viewModel.setParams(alumno.idAlumno, cicloId, alumno.pkCarrera)
                    findNavController().navigate(
                        MatriculaFragmentDirections.actionMatriculaFragmentToMatriculaDetailsFragment(
                            idAlumno = alumno.idAlumno,
                            idCiclo = cicloId,
                            idCarrera = alumno.pkCarrera
                        )
                    )
                } ?: Notificador.show(
                    view = binding.root,
                    mensaje = getString(R.string.error_seleccione_ciclo),
                    colorResId = R.color.colorError,
                    anchorView = null
                )
            },
            onMatricular = { alumno ->
                viewModel.selectedCicloId?.let { cicloId ->
                    viewModel.setParams(alumno.idAlumno, cicloId, alumno.pkCarrera)
                    findNavController().navigate(
                        MatriculaFragmentDirections.actionMatriculaFragmentToMatriculaCursoGrupoFragment(
                            idAlumno = alumno.idAlumno,
                            idCiclo = cicloId,
                            idCarrera = alumno.pkCarrera
                        )
                    )
                } ?: Notificador.show(
                    view = binding.root,
                    mensaje = getString(R.string.error_seleccione_ciclo),
                    colorResId = R.color.colorError,
                    anchorView = null
                )
            }
        )
        adapter = localAdapter
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = localAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        observeState(viewModel.ciclos) { updateCiclos(it) }
        observeState(viewModel.alumnosState) { updateAlumnosState(it) }
    }

    private fun <T> observeState(flow: Flow<T>, block: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { block(it) }
            }
        }
    }

    private fun updateCiclos(state: UiState<List<Ciclo>>) {
        when (state) {
            is UiState.Success -> {
                val ciclos = state.data ?: emptyList()
                // Fix: Show error if no cycles are available
                if (ciclos.isEmpty()) {
                    Notificador.show(
                        view = binding.root,
                        mensaje = getString(R.string.error_no_ciclos),
                        colorResId = R.color.colorError,
                        anchorView = null
                    )
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    ciclos.map { "${it.anio}-${it.numero}" }
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                binding.spinnerCiclo.adapter = adapter
                binding.spinnerCiclo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (!isSpinnerInitialized) {
                            isSpinnerInitialized = true
                            return
                        }
                        ciclos.getOrNull(position)?.let { ciclo ->
                            viewModel.setCiclo(ciclo.idCiclo)
                            println("Cycle selected in spinner: ${ciclo.idCiclo}") // Fix: Add logging
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) = Unit
                }
                if (ciclos.isNotEmpty() && !isSpinnerInitialized) {
                    binding.spinnerCiclo.setSelection(0)
                    isSpinnerInitialized = true
                }
            }
            is UiState.Error -> {
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = null
                )
            }
            is UiState.Loading -> Unit
        }
    }

    private fun updateAlumnosState(state: UiState<List<Alumno>>) {
        binding.progressBar.isVisible = state is UiState.Loading
        binding.recyclerView.isVisible = state !is UiState.Loading && allItems.isNotEmpty()
        when (state) {
            is UiState.Success -> {
                allItems = state.data ?: emptyList()
                filterList(binding.searchView.query.toString())
            }
            is UiState.Error -> {
                allItems = emptyList()
                adapter.submitList(emptyList())
                binding.recyclerView.isVisible = false
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = null
                )
            }
            is UiState.Loading -> {
                allItems = emptyList()
                adapter.submitList(emptyList())
            }
        }
    }

    private fun filterList(query: String?) {
        val filtered = if (query.isNullOrBlank()) allItems else allItems.filter {
            it.nombre.contains(query, ignoreCase = true) || it.cedula.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
        binding.recyclerView.isVisible = filtered.isNotEmpty()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
