package com.example.gestionacademicaapp.ui.ciclos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.isVisible
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
    private var currentSearchQuery: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ciclos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCiclos)
        searchView = view.findViewById(R.id.searchViewCiclos)
        fab = view.findViewById(R.id.fabCiclos)
        progressBar = view.findViewById(R.id.progressBar)

        // Configurar SearchView
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.requestFocus()
        searchView.queryHint = "Buscar por año"

        // Configurar RecyclerView y Adapter
        adapter = CiclosAdapter(
            onEdit = { ciclo, _ -> mostrarDialogoCiclo(ciclo) },
            onDelete = { ciclo, _ -> viewModel.deleteCiclo(ciclo.idCiclo) },
            onActivate = { ciclo -> viewModel.activateCiclo(ciclo.idCiclo) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Configurar búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText
                adapter.filter.filter(newText)
                return false
            }
        })

        // Configurar FAB
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && fab.isExtended) fab.shrink()
                else if (dy < -10 && !fab.isExtended) fab.extend()
            }
        })

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            mostrarDialogoCiclo(null)
        }

        // Configurar deslizamiento para eliminar/editar
        recyclerView.enableSwipeActions(
            onSwipeLeft = { position ->
                adapter.onSwipeDelete(position)
            },
            onSwipeRight = { position ->
                val ciclo = adapter.getCicloAt(position)
                adapter.triggerEdit(ciclo, position)
            }
        )

        // Observar estados del ViewModel
        viewModel.ciclosState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ListUiState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }

                is ListUiState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    adapter.updateCiclos(state.data)
                    currentSearchQuery?.let { adapter.filter.filter(it) }
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
                    Notificador.show(
                        view = view,
                        mensaje = state.data,
                        colorResId = color,
                        anchorView = fab
                    )
                }

                is SingleUiState.Error -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(view, state.message, R.color.colorError)
                }
            }
        }

        return view
    }

    private fun mostrarDialogoCiclo(ciclo: Ciclo?) {
        val campos = listOf(
            CampoFormulario(
                key = "anio",
                label = "Año",
                tipo = "number",
                obligatorio = true,
                editable = true,
                rules = { v -> if (v.isNotEmpty()) null else "El año es requerido" }
            ),
            CampoFormulario(
                key = "numero",
                label = "Número",
                tipo = "number",
                obligatorio = true,
                editable = true,
                rules = { v -> if (v.isNotEmpty()) null else "El número de ciclo es requerido" }
            ),
            CampoFormulario(
                key = "fechaInicio",
                label = "Fecha de Inicio",
                tipo = "date",
                obligatorio = true,
                rules = { v -> if (v.isNotEmpty()) null else "La fecha de inicio es requerida" }
            ),
            CampoFormulario(
                key = "fechaFin",
                label = "Fecha de Fin",
                tipo = "date",
                obligatorio = true,
                rules = { v -> if (v.isNotEmpty()) null else "La fecha final es requerida" }
            )
        )

        val datosIniciales = ciclo?.let {
            mapOf(
                "anio" to it.anio.toString(),
                "numero" to it.numero.toString(),
                "fechaInicio" to it.fechaInicio,
                "fechaFin" to it.fechaFin
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment(
            titulo = if (ciclo == null) "Nuevo Ciclo" else "Editar Ciclo",
            campos = campos,
            datosIniciales = datosIniciales,
            onGuardar = { datosMap ->
                val anio = datosMap["anio"]?.toLongOrNull() ?: 0
                val numero = datosMap["numero"]?.toLongOrNull() ?: 0
                val fechaInicio = datosMap["fechaInicio"] ?: ""
                val fechaFin = datosMap["fechaFin"] ?: ""
                val estado = ciclo?.estado ?: "Inactivo"

                // Validación básica de fechas
                if (fechaInicio > fechaFin) {
                    view?.let {
                        Notificador.show(
                            it,
                            "La fecha de inicio debe ser anterior a la fecha final",
                            R.color.colorError
                        )
                    }
                    return@DialogFormularioFragment
                }

                val nuevoCiclo = Ciclo(
                    idCiclo = ciclo?.idCiclo ?: 0,
                    anio = anio,
                    numero = numero,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    estado = estado
                )

                if (ciclo == null) {
                    viewModel.createCiclo(nuevoCiclo)
                } else {
                    viewModel.updateCiclo(nuevoCiclo)
                }
            },
            onCancel = {
                adapter.restoreFilteredList()
                currentSearchQuery?.let { adapter.filter.filter(it) }
            }
        )

        dialog.show(parentFragmentManager, "DialogFormularioCiclo")
    }
}
