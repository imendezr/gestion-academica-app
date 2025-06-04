package com.example.gestionacademicaapp.ui.profesores

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import kotlin.text.toLongOrNull

@AndroidEntryPoint
class ProfesoresFragment : Fragment() {

    private val viewModel: ProfesoresViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: ProfesoresAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View
    private var currentSearchQuery: String? = null // Para almacenar la consulta actual
    private var allProfesores: List<Profesor> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profesores, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewProfesores)
        searchView = view.findViewById(R.id.searchViewProfesores)
        searchView.queryHint = getString(R.string.search_hint_codigo_nombre)
        fab = view.findViewById(R.id.fabProfesores)
        progressBar = view.findViewById(R.id.progressBar)


        // Configurar RecyclerView y Adapter
        adapter = ProfesoresAdapter(
            onEdit = { profesor, _ -> mostrarDialogoProfesor(profesor) },
            onDelete = { profesor, _ ->
                viewModel.deleteProfesor(profesor.idProfesor) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Configurar SearchView
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.requestFocus()

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
            mostrarDialogoProfesor(null)
        }

        // Configurar deslizamiento para eliminar/editar usando la función de extensión
        recyclerView.enableSwipeActions(
            onSwipeLeft = { position ->
                adapter.onSwipeDelete(position)
            },
            onSwipeRight = { position ->
                val profesor = adapter.getProfesorAt(position)
                adapter.triggerEdit(profesor, position)
            }
        )
// Observar estados del ViewModel
        viewModel.profesoresState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ListUiState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }

                is ListUiState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    adapter.updateProfesores(state.data)
                    // Reaplicar el filtro si hay una consulta activa
                    currentSearchQuery?.let { adapter.filter.filter(it) }
                }

                is ListUiState.Error -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = false
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }
        // Observar feedback de acciones (crear/editar/eliminar)

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
                                state.data.contains("actualizado", true) -> R.color.colorAccent
                        state.data.contains("eliminado", true) -> R.color.colorError
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
    private fun mostrarDialogoProfesor(profesor: Profesor?) {
        val profesorIndex = profesor?.let {
            allProfesores.indexOfFirst { it.idProfesor == profesor.idProfesor }
        } ?: -1
        val campos = listOf(
            CampoFormulario(
                "cedula", "Cedula", "texto", obligatorio = true,
                editable = profesor == null
            ),
            CampoFormulario("nombre", "Nombre", "texto", obligatorio = true),
            CampoFormulario("telefono", "Telefono", "texto", obligatorio = true),
            CampoFormulario("email", "Email", "texto", obligatorio = true)
        )

        val datosIniciales = profesor?.let {
            mapOf(
                "cedula" to it.cedula,
                "nombre" to it.nombre,
                "telefono" to it.telefono,
                "email" to it.email
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment(
            titulo = if (profesor == null) "Nuevo Profesor" else "Editar Profesor",
            campos = campos,
            datosIniciales = datosIniciales,
            onGuardar = { datosMap ->
                val nuevoProfesor = Profesor(
                    idProfesor = profesor?.idProfesor ?: 0,
                    cedula = datosMap["cedula"] ?: "",
                    nombre = datosMap["nombre"] ?: "",
                    telefono = datosMap["telefono"]?: "",
                    email = datosMap["email"] ?: ""
                )

                if (profesor == null) {
                    viewModel.createProfesor(nuevoProfesor)
                } else {
                    viewModel.updateProfesor(nuevoProfesor)
                }
            },
            onCancel = {
                if (profesorIndex != -1) {
                    adapter.notifyItemChanged(profesorIndex)
                }
            }
        )

        dialog.show(parentFragmentManager, "DialogFormularioProfesor")
    }
}
