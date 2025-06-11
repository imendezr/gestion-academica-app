package com.example.gestionacademicaapp.ui.oferta_academica

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.databinding.FragmentGruposOfertaBinding
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ErrorType
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
class GruposOfertaFragment : Fragment() {
    private val viewModel: OfertaAcademicaViewModel by activityViewModels()
    private var _binding: FragmentGruposOfertaBinding? = null
    private val binding get() = _binding!!
    private val adapter = GrupoAdapter(
        onEditGrupo = { grupo -> mostrarFormulario(grupo) },
        onDeleteGrupo = { grupo -> viewModel.deleteGrupo(grupo) }
    )
    private var itemTouchHelper: ItemTouchHelper? = null
    private var swipedPosition: Int? = null
    private val args: GruposOfertaFragmentArgs by navArgs()
    private var isFirstLoad = true // Nueva bandera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setCarrera(args.idCarrera)
        viewModel.setCiclo(args.idCiclo)
        viewModel.setCurso(args.cursoId, args.cursoNombre)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGruposOfertaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupToolbar()
        observeViewModel()
        updateUi()
    }

    private fun setupRecyclerView() {
        val localAdapter = adapter
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = localAdapter
            itemTouchHelper = enableSwipeActions(
                onSwipeLeft = { position ->
                    swipedPosition = position
                    localAdapter.getItemAt(position).let { viewModel.deleteGrupo(it) }
                },
                onSwipeRight = { position ->
                    swipedPosition = position
                    mostrarFormulario(localAdapter.getItemAt(position))
                }
            )
        }
    }

    private fun setupFab() {
        binding.fabBottom.setOnClickListener { mostrarFormulario(null) }
    }

    private fun setupToolbar() {
        binding.txtTitulo.text = getString(R.string.grupos_de_curso, args.cursoNombre)
        findNavController().currentDestination?.let { destination ->
            if (destination.id == R.id.gruposOfertaFragment) {
                requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.grupos.collect { updateGrupos(it) } }
                launch { viewModel.actionState.collect { it?.let { updateActionState(it) } } }
                launch { viewModel.profesoresState.collect { updateProfesores(it) } }
            }
        }
    }

    private fun updateProfesores(state: UiState<List<Profesor>>) {
        if (state is UiState.Error) {
            Notificador.show(
                view = binding.root,
                mensaje = getString(R.string.error_no_profesores),
                colorResId = R.color.colorError,
                anchorView = binding.fabBottom,
                duracion = 2000
            )
        }
    }

    override fun onDestroyView() {
        itemTouchHelper = null
        _binding = null
        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(false)
        super.onDestroyView()
    }

    private fun updateGrupos(state: UiState<List<GrupoDto>>) {
        binding.progressBar.isVisible = state is UiState.Loading
        binding.recyclerView.isVisible = state is UiState.Success && (state.data?.isNotEmpty() == true)
        when (state) {
            is UiState.Success -> {
                val grupos = state.data ?: emptyList()
                adapter.submitList(grupos)
                if (grupos.isEmpty() && !binding.progressBar.isVisible && !isFirstLoad) {
                    Notificador.show(
                        view = binding.root,
                        mensaje = getString(R.string.error_no_grupos),
                        colorResId = R.color.colorError,
                        anchorView = binding.fabBottom,
                        duracion = 2000
                    )
                }
                isFirstLoad = false // Marcar que la primera carga ha terminado
            }
            is UiState.Error -> {
                adapter.submitList(emptyList())
                binding.recyclerView.isVisible = false
                Notificador.show(
                    view = binding.root,
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = binding.fabBottom,
                    duracion = 2000
                )
                isFirstLoad = false // Marcar que la primera carga ha terminado
            }
            is UiState.Loading -> adapter.submitList(emptyList())
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
                            "UPDATED" -> R.string.grupo_actualizado_exito
                            "DELETED" -> R.string.grupo_eliminado_exito
                            else -> R.string.operacion_exitosa
                        }
                    ),
                    colorResId = R.color.colorAccent,
                    anchorView = binding.fabBottom,
                    duracion = 2000
                )
            }

            is UiState.Error -> {
                swipedPosition?.let { pos ->
                    adapter.notifyItemChanged(pos)
                    binding.recyclerView.clearSwipe(pos, itemTouchHelper)
                    Notificador.show(
                        view = binding.root,
                        mensaje = when (state.type) {
                            ErrorType.DEPENDENCY -> getString(R.string.error_grupo_dependencia)
                            else -> state.message
                        },
                        colorResId = R.color.colorError,
                        anchorView = binding.fabBottom,
                        duracion = 2000
                    )
                }
                swipedPosition = null
            }

            is UiState.Loading -> {}
        }
    }

    private fun mostrarFormulario(grupo: GrupoDto?) {
        val isEditing = grupo != null
        val datosIniciales = if (isEditing) {
            mapOf(
                "numeroGrupo" to grupo.numeroGrupo.toString(),
                "horario" to grupo.horario,
                "profesor" to grupo.idProfesor.toString()
            )
        } else {
            emptyMap()
        }

        val campos = viewModel.getFormFields()
        val dialogo = DialogFormularioFragment.newInstance(
            titulo = getString(if (isEditing) R.string.editar_grupo else R.string.crear_grupo),
            campos = campos,
            datosIniciales = datosIniciales
        )
        dialogo.setOnGuardarListener { datos ->
            try {
                viewModel.saveGrupo(
                    idGrupo = grupo?.idGrupo,
                    numeroGrupo = datos["numeroGrupo"]?.toLongOrNull() ?: 0L,
                    horario = datos["horario"] ?: "",
                    idProfesor = datos["profesor"]?.toLongOrNull() ?: 0L
                )
            } catch (_: Exception) {
                Notificador.show(
                    view = binding.root,
                    mensaje = getString(R.string.error_validacion_datos),
                    colorResId = R.color.colorError,
                    anchorView = binding.fabBottom,
                    duracion = 2000
                )
            }
        }
        dialogo.setOnCancelListener {
            swipedPosition?.let { pos ->
                adapter.notifyItemChanged(pos)
                binding.recyclerView.clearSwipe(pos, itemTouchHelper)
                swipedPosition = null
            }
        }
        dialogo.show(childFragmentManager, "DialogFormularioFragment")
    }

    private fun updateUi() {
        binding.fabBottom.isVisible = true
    }
}
