package com.example.gestionacademicaapp.ui.notas

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
import com.example.gestionacademicaapp.data.api.model.dto.GrupoProfesorDto
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.databinding.FragmentNotasBinding
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.clearSwipe
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.setupSearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotasFragment : Fragment() {

    private val viewModel: NotasViewModel by viewModels()
    private var _binding: FragmentNotasBinding? = null
    private val binding get() = _binding!!
    private val adapter = NotasAdapter { matricula -> mostrarDialogoNota(matricula) }
    private var allItems: List<MatriculaAlumnoDto> = emptyList()
    private var itemTouchHelper: ItemTouchHelper? = null
    private var swipedPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSpinner()
        setupSearchView()
        setupToolbar()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotasFragment.adapter
            val localAdapter = this@NotasFragment.adapter // Avoid smart cast issues
            itemTouchHelper = enableSwipeActions(
                onSwipeLeft = {}, // Empty lambda to disable left swipe
                onSwipeRight = { position ->
                    swipedPosition = position
                    localAdapter.getItemAt(position)?.let { mostrarDialogoNota(it) }
                }
            )
        }
    }

    private fun setupSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.grupos.collect { updateGruposSpinner(it) }
            }
        }
    }

    private fun setupSearchView() {
        setupSearchView(
            searchView = binding.searchView,
            hint = getString(R.string.buscar_alumno),
            onQueryTextChange = { query -> filterList(query) }
        )
    }

    private fun setupToolbar() {
        findNavController().currentDestination?.let { destination ->
            if (destination.id == R.id.nav_notas) {
                requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.matriculas.collect { updateMatriculas(it) } }
                launch { viewModel.actionState.collect { it?.let { updateActionState(it) } } }
            }
        }
    }

    private fun updateGruposSpinner(state: UiState<List<GrupoProfesorDto>>) {
        when (state) {
            is UiState.Success -> {
                val grupos = state.data ?: emptyList()
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    grupos.map { "${it.nombreCurso} - Grupo ${it.numeroGrupo}" }
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                binding.spinnerGrupo.adapter = adapter
                binding.spinnerGrupo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        grupos.getOrNull(position)?.let { grupo ->
                            viewModel.setGrupo(grupo.idGrupo)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) = Unit
                }
                if (grupos.isNotEmpty()) {
                    binding.spinnerGrupo.setSelection(0)
                }
            }
            is UiState.Error -> {
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = null,
                    duracion = 2000
                )
            }
            is UiState.Loading -> Unit
        }
    }

    private fun updateMatriculas(state: UiState<List<MatriculaAlumnoDto>>) {
        binding.progressBar.isVisible = state is UiState.Loading
        when (state) {
            is UiState.Success -> {
                allItems = state.data ?: emptyList()
                filterList(binding.searchView.query.toString())
                binding.recyclerView.isVisible = allItems.isNotEmpty()
                if (allItems.isEmpty() && !binding.progressBar.isVisible) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(2000)
                        Notificador.show(
                            view = binding.root,
                            mensaje = getString(R.string.error_no_alumnos),
                            colorResId = R.color.colorError,
                            anchorView = null,
                            duracion = 2000
                        )
                    }
                }
            }
            is UiState.Error -> {
                allItems = emptyList()
                adapter.submitList(emptyList())
                binding.recyclerView.isVisible = false
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = null,
                    duracion = 2000
                )
            }
            is UiState.Loading -> {
                allItems = emptyList()
                adapter.submitList(emptyList())
            }
        }
    }

    private fun updateActionState(state: UiState<Unit>) {
        when (state) {
            is UiState.Success -> {
                swipedPosition = null
                Notificador.show(
                    view = binding.root,
                    mensaje = getString(R.string.nota_success),
                    colorResId = R.color.colorAccent,
                    anchorView = null,
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
                    anchorView = null,
                    duracion = 2000
                )
            }
            is UiState.Loading -> Unit
        }
    }

    private fun filterList(query: String?) {
        val filtered = if (query.isNullOrBlank()) allItems else allItems.filter {
            it.nombreCurso.contains(query, ignoreCase = true) || it.nombreProfesor.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
        binding.recyclerView.isVisible = filtered.isNotEmpty()
    }

    private fun mostrarDialogoNota(matricula: MatriculaAlumnoDto) {
        val campos = listOf(
            CampoFormulario(
                key = "nota",
                label = getString(R.string.nota),
                tipo = CampoTipo.NUMBER,
                rules = { value, _ -> validateNota(value) },
                obligatorio = true,
                obligatorioError = getString(R.string.error_nota_requerida)
            )
        )

        val dialog = DialogFormularioFragment.newInstance(
            titulo = getString(R.string.registrar_nota),
            campos = campos,
            datosIniciales = mapOf("nota" to matricula.nota.toString())
        )
        dialog.setOnGuardarListener { datos ->
            val nota = datos["nota"]?.toDoubleOrNull() ?: 0.0
            viewModel.updateNota(matricula.idMatricula, nota.toLong())
        }
        dialog.setOnCancelListener {
            swipedPosition?.let { pos ->
                adapter.notifyItemChanged(pos)
                binding.recyclerView.clearSwipe(pos, itemTouchHelper)
                swipedPosition = null
            }
        }
        dialog.show(childFragmentManager, "DialogFormularioNota")
    }

    private fun validateNota(value: String?): String? {
        if (value.isNullOrBlank()) return getString(R.string.error_nota_requerida)
        val nota = value.toDoubleOrNull() ?: return getString(R.string.error_nota_invalida)
        return if (nota in 0.0..100.0) null else getString(R.string.error_nota_rango)
    }

    override fun onDestroyView() {
        itemTouchHelper = null
        _binding = null
        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(false)
        super.onDestroyView()
    }
}
