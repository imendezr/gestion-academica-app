package com.example.gestionacademicaapp.ui.carreras

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
import com.example.gestionacademicaapp.data.api.model.Carrera
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
class CarrerasFragment : Fragment() {

    private val viewModel: CarrerasViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CarrerasAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View

    private var originalItems: List<Carrera> = emptyList()
    private var currentEditIndex: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_carreras, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCarreras)
        searchView = view.findViewById(R.id.searchViewCarreras)
        fab = view.findViewById(R.id.fabCarreras)
        progressBar = view.findViewById(R.id.progressBar)

        setupSearchView(searchView, getString(R.string.search_hint_codigo_nombre)) { query ->
            filterList(query)
        }

        adapter = CarrerasAdapter(
            onEdit = { carrera -> mostrarDialogoCarrera(carrera) },
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
                val carrera = adapter.getItemAt(pos)
                viewModel.deleteItem(carrera.idCarrera)
                adapter.notifyItemChanged(pos)
            },
            onSwipeRight = { pos ->
                currentEditIndex = pos
                adapter.notifyItemChanged(pos)
                mostrarDialogoCarrera(adapter.getItemAt(pos))
            }
        )

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            currentEditIndex = null
            mostrarDialogoCarrera(null)
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
                    Notificador.show(requireView(), state.message, R.color.colorError)
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
                        state.data.contains("eliminada", true) -> R.color.colorError
                        state.data.contains("creada", true) ||
                                state.data.contains("actualizada", true) -> R.color.colorAccent
                        else -> R.color.colorPrimary
                    }
                    Notificador.show(requireView(), state.data, color, anchorView = fab)
                    filterList(searchView.query.toString()) // Solo filtra, la recarga ya ocurre
                    currentEditIndex = null
                }
                is SingleUiState.Error -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }

        return view
    }

    private fun filterList(query: String?) {
        val texto = query?.lowercase()?.trim() ?: ""
        val filtered = if (texto.isEmpty()) {
            originalItems
        } else {
            originalItems.filter {
                it.nombre.lowercase().contains(texto) ||
                        it.codigo.lowercase().contains(texto)
            }
        }
        adapter.submitList(filtered)
    }

    private fun mostrarDialogoCarrera(carrera: Carrera?) {
        val campos = listOf(
            CampoFormulario(
                key = "codigo",
                label = "Código",
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = "El código es requerido",
                editable = carrera == null,
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else if (value.length !in 3..10) "Debe tener entre 3 y 10 caracteres"
                    else null
                }
            ),
            CampoFormulario(
                key = "nombre",
                label = "Nombre",
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = "El nombre es requerido",
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else if (value.length < 5) "Debe tener al menos 5 caracteres"
                    else null
                }
            ),
            CampoFormulario(
                key = "titulo",
                label = "Título",
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = "El título es requerido",
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else if (value.length < 5) "Debe tener al menos 5 caracteres"
                    else null
                }
            )
        )

        val datosIniciales = carrera?.let {
            mapOf(
                "codigo" to it.codigo,
                "nombre" to it.nombre,
                "titulo" to it.titulo
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment.newInstance(
            titulo = if (carrera == null) "Nueva Carrera" else "Editar Carrera",
            campos = campos,
            datosIniciales = datosIniciales
        )

        dialog.setOnGuardarListener { datosMap ->
            val nuevaCarrera = Carrera(
                idCarrera = carrera?.idCarrera ?: 0,
                codigo = datosMap["codigo"] ?: "",
                nombre = datosMap["nombre"] ?: "",
                titulo = datosMap["titulo"] ?: ""
            )
            if (carrera == null) viewModel.createItem(nuevaCarrera)
            else viewModel.updateItem(nuevaCarrera)
        }

        dialog.setOnCancelListener { index ->
            if (index != null && index >= 0) adapter.notifyItemChanged(index)
        }

        dialog.show(parentFragmentManager, "DialogFormularioCarrera")
    }
}
