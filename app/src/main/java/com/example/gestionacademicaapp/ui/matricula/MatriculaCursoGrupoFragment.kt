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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.databinding.FragmentMatriculaCursoGrupoBinding
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MatriculaCursoGrupoFragment : Fragment() {

    private val viewModel: MatriculaViewModel by viewModels()
    private var _binding: FragmentMatriculaCursoGrupoBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MatriculaCursoGrupoAdapter
    private var allItems: List<GrupoDto> = emptyList()
    private var isSpinnerInitialized = false
    private var hasFetchedGroups = false
    private var hasFetchedCourses = false
    private var isEditing = false
    private var idGrupo: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatriculaCursoGrupoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(MatriculaCursoGrupoFragmentArgs.fromBundle(requireArguments())) {
            isEditing = idGrupo != 0L
            this@MatriculaCursoGrupoFragment.idGrupo = idGrupo
            viewModel.setParams(idAlumno, idCiclo, idCarrera)
        }
        setupRecyclerView()
        setupSpinner()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MatriculaCursoGrupoAdapter { grupo ->
            viewModel.selectGrupo(grupo.idGrupo)
            adapter.updateSelection(grupo.idGrupo)
            updateFabIcon(viewModel.idGrupo) // Update FAB icon on selection
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MatriculaCursoGrupoFragment.adapter
        }
        if (isEditing) {
            adapter.updateSelection(idGrupo)
            updateFabIcon(viewModel.idGrupo) // Update FAB icon for editing mode
        }
    }

    private fun setupSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cursos.collect { state ->
                    updateCursos(state)
                }
            }
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            viewModel.confirmarMatricula()
        }
        updateFabIcon(viewModel.idGrupo) // Initial FAB icon setup
    }

    private fun updateFabIcon(idGrupo: Long?) {
        val iconResId = if (idGrupo != null && idGrupo != 0L) {
            R.drawable.ic_active
        } else {
            R.drawable.ic_activate
        }
        binding.fab.setIconResource(iconResId)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.gruposState.collect { updateGruposState(it) }
                }
                launch {
                    viewModel.actionState.collect { state ->
                        state?.let { updateActionState(it) }
                    }
                }
            }
        }
    }

    private fun updateGruposState(state: UiState<List<GrupoDto>>) {
        binding.progressBar.isVisible = state is UiState.Loading
        binding.recyclerView.isVisible = state is UiState.Success && (state.data?.isNotEmpty() ?: false)
        when (state) {
            is UiState.Success -> {
                allItems = state.data ?: emptyList()
                adapter.submitList(allItems)
                hasFetchedGroups = true
                updateFabIcon(viewModel.idGrupo) // Update FAB icon when groups load
                if (allItems.isEmpty()) {
                    state.message?.let {
                        Notificador.show(
                            view = binding.root,
                            mensaje = it,
                            colorResId = R.color.colorError,
                            anchorView = binding.fab,
                            duracion = 2000
                        )
                    }
                }
            }
            is UiState.Error -> {
                hasFetchedGroups = true
                allItems = emptyList()
                adapter.submitList(emptyList())
                binding.recyclerView.isVisible = false
                updateFabIcon(viewModel.idGrupo) // Reset FAB icon on error
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = binding.fab,
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
                Notificador.show(
                    view = binding.root,
                    mensaje = getString(if (isEditing) R.string.matricula_editada_exitosa else R.string.matricula_exitosa),
                    colorResId = R.color.colorAccent,
                    anchorView = binding.fab,
                    duracion = 2000
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000)
                    if (isActive) {
                        findNavController().navigateUp()
                    }
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
                updateFabIcon(viewModel.idGrupo) // Update FAB icon on error
            }
            is UiState.Loading -> Unit
        }
    }

    private fun updateCursos(state: UiState<List<CursoDto>>) {
        when (state) {
            is UiState.Success -> {
                hasFetchedCourses = true
                val courses = state.data ?: emptyList()
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    courses.map { it.nombre }
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                binding.spinnerCurso.adapter = adapter
                binding.spinnerCurso.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        courses.getOrNull(position)?.let { curso ->
                            viewModel.setCurso(curso.idCurso)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {
                        viewModel.setCurso(0L)
                    }
                }
                if (courses.isNotEmpty()) {
                    binding.spinnerCurso.setSelection(0)
                    if (!isSpinnerInitialized) {
                        isSpinnerInitialized = true
                        viewModel.setCurso(courses[0].idCurso)
                    }
                } else if (hasFetchedCourses) {
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
                hasFetchedCourses = true
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
