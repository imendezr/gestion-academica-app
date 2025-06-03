package com.example.gestionacademicaapp.ui.carreras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.databinding.FragmentCarrerasBinding
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.clearSwipe
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.setupSearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CarrerasFragment : Fragment() {

    private val viewModel: CarrerasViewModel by viewModels()
    private var _binding: FragmentCarrerasBinding? = null
    private val binding get() = _binding!!
    private val adapter: CarrerasAdapter by lazy {
        CarrerasAdapter(
            onEdit = { carrera ->
                editingPosition = allItems.indexOf(carrera)
                showCarreraDialog(carrera)
            },
            onDelete = { carrera ->
                viewModel.deleteItem(carrera.idCarrera)
            },
            onViewCursosCarrera = { carrera ->
                CarreraCursosFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("carrera", carrera)
                    }
                }.show(parentFragmentManager, "CarreraCursosFragment")
            }
        )
    }
    private var allItems: List<Carrera> = emptyList()
    private var editingPosition: Int? = null
    private var swipedPosition: Int? = null // Track swiped item for error handling
    private var itemTouchHelper: ItemTouchHelper? = null // Store ItemTouchHelper for clearing swipe state

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarrerasBinding.inflate(inflater, container, false)

        setupSearchView(binding.searchViewCarreras, getString(R.string.search_hint_codigo_nombre)) { query ->
            filterList(query)
        }

        // Capture the adapter in a local val to ensure immutability
        val localAdapter = adapter

        binding.recyclerViewCarreras.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = localAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    with(binding.fabCarreras) {
                        if (dy > 10 && isExtended) shrink()
                        else if (dy < -10 && !isExtended) extend()
                    }
                }
            })
            // Store the ItemTouchHelper instance
            itemTouchHelper = enableSwipeActions(
                onSwipeLeft = { pos ->
                    swipedPosition = pos // Store position before deletion
                    localAdapter.getItemAt(pos).let { viewModel.deleteItem(it.idCarrera) }
                },
                onSwipeRight = { pos ->
                    editingPosition = pos
                    showCarreraDialog(localAdapter.getItemAt(pos))
                }
            )
        }

        binding.fabCarreras.setOnClickListener {
            binding.searchViewCarreras.clearFocus()
            editingPosition = null
            showCarreraDialog(null)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.fetchItems().collectLatest { updateUiState(it, isFetch = true) } }
                launch { viewModel.actionState.collectLatest { updateUiState(it, isFetch = false) } }
            }
        }
    }

    private fun updateUiState(state: UiState<*>, isFetch: Boolean) {
        binding.progressBar.isVisible = state is UiState.Loading
        binding.fabCarreras.isEnabled = state !is UiState.Loading
        binding.recyclerViewCarreras.isVisible = !isFetch || allItems.isNotEmpty()

        when (state) {
            is UiState.Success -> {
                if (isFetch && state.data is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    allItems = (state.data as? List<Carrera>) ?: emptyList() // Safe cast, fetchItems returns List<Carrera>
                    filterList()
                }
                state.message?.let { message ->
                    val color = when {
                        message.contains("creada", true) || message.contains("actualizada", true) -> R.color.colorAccent
                        message.contains("eliminada", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }
                    showNotification(message, color)
                }
                if (!isFetch) {
                    binding.recyclerViewCarreras.isVisible = allItems.isNotEmpty()
                    filterList()
                    editingPosition = null
                    swipedPosition = null // Clear swiped position on success
                }
            }
            is UiState.Error -> {
                showNotification(getErrorMessage(state), R.color.colorError)
                if (!isFetch) {
                    filterList()
                    editingPosition?.let { pos -> adapter.notifyItemChanged(pos) }
                    swipedPosition?.let { pos ->
                        adapter.notifyItemChanged(pos) // Refresh item to update visuals
                        // Reset swipe state using stored ItemTouchHelper
                        binding.recyclerViewCarreras.clearSwipe(pos, itemTouchHelper)
                        swipedPosition = null // Clear after handling
                    }
                }
            }
            else -> {}
        }
    }

    private fun getErrorMessage(state: UiState.Error): String = when (state.type) {
        ErrorType.DEPENDENCY -> when {
            state.message.contains("cursos asociados") -> getString(R.string.error_carrera_cursos_asignadas)
            state.message.contains("alumnos inscritos") -> getString(R.string.error_carrera_alumnos_inscritos)
            else -> state.message
        }
        ErrorType.VALIDATION -> when {
            state.message.contains("duplicado") -> getString(R.string.error_carrera_codigo_duplicado)
            state.message.contains("no existe") -> getString(R.string.error_carrera_no_existe)
            else -> getString(R.string.error_formulario_desc, state.message)
        }
        ErrorType.GENERAL -> state.message
    }

    private fun filterList(query: String? = binding.searchViewCarreras.query?.toString()) {
        val filteredItems = query?.trim()?.lowercase()?.let { text ->
            if (text.isEmpty()) allItems else allItems.filter {
                it.nombre.lowercase().contains(text) || it.codigo.lowercase().contains(text)
            }
        } ?: allItems
        adapter.submitList(filteredItems)
        binding.recyclerViewCarreras.isVisible = filteredItems.isNotEmpty()
    }

    private fun showCarreraDialog(carrera: Carrera?) {
        val validator = CarreraValidator()
        val campos = listOf(
            CampoFormulario(
                key = "codigo",
                label = getString(R.string.label_codigo),
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = validator.codigoRequiredError,
                rules = { value, _ -> validator.validateCodigo(value) }
            ),
            CampoFormulario(
                key = "nombre",
                label = getString(R.string.label_nombre),
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = validator.nombreRequiredError,
                rules = { value, _ -> validator.validateNombre(value) }
            ),
            CampoFormulario(
                key = "titulo",
                label = getString(R.string.label_titulo),
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = validator.tituloRequiredError,
                rules = { value, _ -> validator.validateTitulo(value) }
            )
        )

        val datosIniciales = carrera?.let {
            mapOf(
                "codigo" to it.codigo,
                "nombre" to it.nombre,
                "titulo" to it.titulo
            )
        } ?: emptyMap()

        DialogFormularioFragment.newInstance(
            titulo = getString(if (carrera == null) R.string.titulo_nueva_carrera else R.string.titulo_editar_carrera),
            campos = campos,
            datosIniciales = datosIniciales
        ).apply {
            setOnGuardarListener { data ->
                val newItem = Carrera(
                    idCarrera = carrera?.idCarrera ?: 0,
                    codigo = data["codigo"] ?: "",
                    nombre = data["nombre"] ?: "",
                    titulo = data["titulo"] ?: ""
                )
                if (carrera == null) viewModel.createItem(newItem) else viewModel.updateItem(newItem)
            }
            setOnCancelListener { editingPosition?.let { adapter.notifyItemChanged(it) } }
        }.show(parentFragmentManager, "DialogFormularioCarrera")
    }

    private fun showNotification(message: String, color: Int) {
        Notificador.show(requireView(), message, color, anchorView = binding.fabCarreras)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemTouchHelper = null // Clear to prevent memory leaks
    }
}
