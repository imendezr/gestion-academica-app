package com.example.gestionacademicaapp.ui.ciclos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.ui.common.validators.CicloValidator
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.setupSearchView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CiclosFragment : Fragment() {

    private val viewModel: CiclosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CiclosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View

    private var allCiclos: List<Ciclo> = emptyList()
    private var editingPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ciclos, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCiclos)
        searchView = view.findViewById(R.id.searchViewCiclos)
        fab = view.findViewById(R.id.fabCiclos)
        progressBar = view.findViewById(R.id.progressBar)

        setupSearchView(searchView, getString(R.string.search_hint_anio)) { query ->
            filterList(query)
        }

        adapter = CiclosAdapter(
            onEdit = { ciclo ->
                editingPosition = allCiclos.indexOf(ciclo)
                mostrarDialogoCiclo(ciclo)
            },
            onDelete = { ciclo -> viewModel.deleteItem(ciclo.idCiclo) },
            onActivateCiclo = { ciclo -> viewModel.activateCiclo(ciclo.idCiclo) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && fab.isExtended) fab.shrink()
                else if (dy < -10 && !fab.isExtended) fab.extend()
            }
        })

        recyclerView.enableSwipeActions(
            onSwipeLeft = { pos ->
                val ciclo = adapter.getItemAt(pos)
                viewModel.deleteItem(ciclo.idCiclo)
                adapter.notifyItemChanged(pos)
            },
            onSwipeRight = { pos ->
                editingPosition = pos
                val ciclo = adapter.getItemAt(pos)
                mostrarDialogoCiclo(ciclo)
                adapter.notifyItemChanged(pos)
            }
        )

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            editingPosition = null
            mostrarDialogoCiclo(null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.ciclosState.collectLatest { state ->
                        updateUiState(state, isAction = false)
                    }
                }
                launch {
                    viewModel.actionState.collectLatest { state ->
                        updateUiState(state, isAction = true)
                    }
                }
            }
        }

        return view
    }

    private fun updateUiState(state: UiState<*>, isAction: Boolean) {
        when (state) {
            is UiState.Loading -> {
                progressBar.isVisible = true
                recyclerView.isVisible = !isAction && allCiclos.isNotEmpty()
                fab.isEnabled = !isAction
            }
            is UiState.Success -> {
                progressBar.isVisible = false
                fab.isEnabled = true
                if (!isAction) {
                    recyclerView.isVisible = true
                    if (state.data is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        allCiclos = state.data as List<Ciclo>
                    }
                    filterList(searchView.query.toString())
                }
                state.message?.let { message ->
                    val color = when {
                        message.contains("creado", true) || message.contains("actualizado", true) || message.contains("activado", true) -> R.color.colorAccent
                        message.contains("eliminado", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }
                    Notificador.show(requireView(), message, color, anchorView = fab)
                }
                if (isAction) {
                    recyclerView.isVisible = allCiclos.isNotEmpty()
                    filterList(searchView.query.toString())
                    editingPosition = null
                }
            }
            is UiState.Error -> {
                progressBar.isVisible = false
                recyclerView.isVisible = allCiclos.isNotEmpty()
                fab.isEnabled = true
                val message = when (state.type) {
                    ErrorType.DEPENDENCY -> state.message
                    ErrorType.VALIDATION -> "Error de validación: ${state.message}"
                    ErrorType.GENERAL -> state.message
                }
                Notificador.show(requireView(), message, R.color.colorError, anchorView = fab)
                if (isAction) {
                    filterList(searchView.query.toString())
                }
            }
        }
    }

    private fun filterList(query: String?) {
        val texto = query?.trim()?.lowercase() ?: ""
        val filtered = if (texto.isEmpty()) {
            allCiclos
        } else {
            allCiclos.filter { it.anio.toString().contains(texto) }
        }
        adapter.submitList(filtered)
    }

    private fun mostrarDialogoCiclo(ciclo: Ciclo?) {
        val validator = CicloValidator()
        val campos = mutableListOf(
            CampoFormulario(
                key = "anio",
                label = "Año",
                tipo = CampoTipo.NUMBER,
                obligatorio = true,
                obligatorioError = validator.anioRequiredError,
                rules = { value, _ -> validator.validateAnio(value) }
            ),
            CampoFormulario(
                key = "numero",
                label = "Número",
                tipo = CampoTipo.SPINNER,
                obligatorio = true,
                obligatorioError = validator.numeroRequiredError,
                opciones = listOf("1" to "1", "2" to "2"),
                rules = { value, _ -> validator.validateNumero(value) }
            ),
            CampoFormulario(
                key = "fechaInicio",
                label = "Fecha de Inicio",
                tipo = CampoTipo.DATE,
                obligatorio = true,
                obligatorioError = validator.fechaInicioRequiredError,
                rules = { value, values -> validator.validateFechaInicio(value, values["fechaFin"]) }
            ),
            CampoFormulario(
                key = "fechaFin",
                label = "Fecha de Fin",
                tipo = CampoTipo.DATE,
                obligatorio = true,
                obligatorioError = validator.fechaFinRequiredError,
                rules = { value, values -> validator.validateFechaFin(value, values["fechaInicio"]) }
            )
        )

        if (ciclo != null) {
            campos.add(
                CampoFormulario(
                    key = "estado",
                    label = "Estado",
                    tipo = CampoTipo.TEXT,
                    obligatorio = false,
                    editable = false
                )
            )
        }

        val datosIniciales = ciclo?.let {
            mapOf(
                "anio" to it.anio.toString(),
                "numero" to it.numero.toString(),
                "fechaInicio" to it.fechaInicio,
                "fechaFin" to it.fechaFin,
                "estado" to it.estado
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment.newInstance(
            titulo = if (ciclo == null) "Nuevo Ciclo" else "Editar Ciclo",
            campos = campos,
            datosIniciales = datosIniciales
        )

        dialog.setOnGuardarListener { datosMap ->
            val anio = datosMap["anio"]?.toLongOrNull() ?: 0L
            val numero = datosMap["numero"]?.toLongOrNull() ?: 0L
            val fechaInicio = datosMap["fechaInicio"] ?: ""
            val fechaFin = datosMap["fechaFin"] ?: ""
            val estado = ciclo?.estado ?: "Inactivo"

            val nuevoCiclo = Ciclo(
                idCiclo = ciclo?.idCiclo ?: 0,
                anio = anio,
                numero = numero,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                estado = estado
            )
            if (ciclo == null) viewModel.createItem(nuevoCiclo)
            else viewModel.updateItem(nuevoCiclo)
        }

        dialog.setOnCancelListener {
            editingPosition?.let { adapter.notifyItemChanged(it) }
        }

        dialog.show(parentFragmentManager, "DialogCiclo")
    }
}
