package com.example.gestionacademicaapp.ui.ciclos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.setupSearchView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CiclosFragment : Fragment() {

    private val viewModel: CiclosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CiclosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View

    private var originalItems: List<Ciclo> = emptyList()
    private var currentEditIndex: Int? = null

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
            onEdit = { ciclo -> mostrarDialogoCiclo(ciclo) },
            onDelete = { ciclo ->
                viewModel.deleteItem(ciclo.idCiclo)
            },
            onActivateCiclo = { ciclo ->
                viewModel.activateCiclo(ciclo.idCiclo)
            }
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
                currentEditIndex = pos
                adapter.notifyItemChanged(pos)
                mostrarDialogoCiclo(adapter.getItemAt(pos))
            }
        )

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            currentEditIndex = null
            mostrarDialogoCiclo(null)
        }

        viewModel.fetchItems().asLiveData().observe(viewLifecycleOwner) { state ->
            when (state) {
                is ListUiState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }
                is ListUiState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    originalItems = state.data
                    filterList(searchView.query.toString())
                }
                is ListUiState.Error -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = false
                    Notificador.show(view, state.message, R.color.colorError)
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SingleUiState.Loading -> {
                    progressBar.isVisible = true
                    fab.isEnabled = false
                }
                is SingleUiState.Success -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    val color = when {
                        state.data.contains("creado", true) ||
                                state.data.contains("actualizado", true) ||
                                state.data.contains("activado", true) -> R.color.colorAccent
                        state.data.contains("eliminado", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }
                    Notificador.show(view, state.data, color, anchorView = fab)
                    filterList(searchView.query.toString())
                    currentEditIndex = null
                }
                is SingleUiState.Error -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(view, state.message, R.color.colorError, anchorView = fab) // Asegurar que el mensaje de error sea visible
                }
            }
        }

        return view
    }

    private fun filterList(query: String?) {
        val texto = query?.trim() ?: ""
        val filtered = if (texto.isEmpty()) {
            originalItems
        } else {
            originalItems.filter {
                it.anio.toString().contains(texto)
            }
        }
        adapter.submitList(filtered)
    }

    private fun mostrarDialogoCiclo(ciclo: Ciclo?) {
        val campos = mutableListOf(
            CampoFormulario(
                key = "anio",
                label = "Año",
                tipo = CampoTipo.NUMBER,
                obligatorio = true,
                obligatorioError = "El año es requerido",
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else {
                        val year = value.toLongOrNull()
                        if (year == null || year !in 1900..2100) "Debe estar entre 1900 y 2100"
                        else null
                    }
                }
            ),
            CampoFormulario(
                key = "numero",
                label = "Número",
                tipo = CampoTipo.SPINNER,
                obligatorio = true,
                obligatorioError = "El número es requerido",
                opciones = listOf("1" to "1", "2" to "2")
            ),
            CampoFormulario(
                key = "fechaInicio",
                label = "Fecha de Inicio",
                tipo = CampoTipo.DATE,
                obligatorio = true,
                obligatorioError = "La fecha de inicio es requerida",
                rules = { value, values ->
                    if (value.isEmpty()) null
                    else {
                        val fechaFin = values["fechaFin"] ?: ""
                        if (fechaFin.isNotEmpty() && value > fechaFin) {
                            "La fecha de inicio debe ser anterior a la final"
                        } else null
                    }
                }
            ),
            CampoFormulario(
                key = "fechaFin",
                label = "Fecha de Fin",
                tipo = CampoTipo.DATE,
                obligatorio = true,
                obligatorioError = "La fecha de fin es requerida"
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

        dialog.setOnCancelListener { index ->
            if (index != null && index >= 0) adapter.notifyItemChanged(index)
        }

        dialog.show(parentFragmentManager, "DialogFormularioCiclo")
    }
}
