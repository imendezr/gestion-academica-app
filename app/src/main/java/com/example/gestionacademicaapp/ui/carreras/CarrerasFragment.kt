package com.example.gestionacademicaapp.ui.carreras

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
import com.example.gestionacademicaapp.data.api.model.Carrera
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
class CarrerasFragment : Fragment() {

    private val viewModel: CarrerasViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CarrerasAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View
    private var currentSearchQuery: String? = null

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

        // Configurar SearchView
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.requestFocus()

        // Configurar RecyclerView y Adapter
        adapter = CarrerasAdapter(
            onEdit = { carrera, _ -> mostrarDialogoCarrera(carrera) },
            onDelete = { carrera, _ -> viewModel.deleteCarrera(carrera.idCarrera) },
            onViewCursos = { carrera ->
                val fragment = CarreraCursosFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("carrera", carrera)
                    }
                }
                fragment.show(parentFragmentManager, "CarreraCursosFragment")
            }
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
            mostrarDialogoCarrera(null)
        }

        // Configurar deslizamiento para eliminar/editar
        recyclerView.enableSwipeActions(
            onSwipeLeft = { position ->
                adapter.onSwipeDelete(position)
            },
            onSwipeRight = { position ->
                val carrera = adapter.getCarreraAt(position)
                adapter.triggerEdit(carrera, position)
            }
        )

        // Observar estados del ViewModel
        viewModel.carrerasState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ListUiState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }

                is ListUiState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    adapter.updateCarreras(state.data)
                    currentSearchQuery?.let { adapter.filter.filter(it) }
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
                        state.data.contains("creada", true) ||
                                state.data.contains("actualizada", true) -> R.color.colorAccent

                        state.data.contains("eliminada", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }

                    Notificador.show(
                        view = requireView(),
                        mensaje = state.data,
                        colorResId = color,
                        anchorView = fab
                    )
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

    private fun mostrarDialogoCarrera(carrera: Carrera?) {
        val campos = listOf(
            CampoFormulario(
                "codigo", "Código", "texto", obligatorio = true,
                editable = carrera == null
            ),
            CampoFormulario("nombre", "Nombre", "texto", obligatorio = true),
            CampoFormulario("titulo", "Título", "texto", obligatorio = true)
        )

        val datosIniciales = carrera?.let {
            mapOf(
                "codigo" to it.codigo,
                "nombre" to it.nombre,
                "titulo" to it.titulo
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment(
            titulo = if (carrera == null) "Nueva Carrera" else "Editar Carrera",
            campos = campos,
            datosIniciales = datosIniciales,
            onGuardar = { datosMap ->
                val nuevaCarrera = Carrera(
                    idCarrera = carrera?.idCarrera ?: 0,
                    codigo = datosMap["codigo"] ?: "",
                    nombre = datosMap["nombre"] ?: "",
                    titulo = datosMap["titulo"] ?: ""
                )
                if (carrera == null) {
                    viewModel.createCarrera(nuevaCarrera)
                } else {
                    viewModel.updateCarrera(nuevaCarrera)
                }
            },
            onCancel = {
                adapter.restoreFilteredList()
                currentSearchQuery?.let { adapter.filter.filter(it) }
            }
        )

        dialog.show(parentFragmentManager, "DialogFormularioCarrera")
    }
}
