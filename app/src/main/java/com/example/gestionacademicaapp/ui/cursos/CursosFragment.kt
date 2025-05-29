package com.example.gestionacademicaapp.ui.cursos

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
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CursosFragment : Fragment() {

    private val viewModel: CursosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CursosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View
    private var currentSearchQuery: String? = null // Para almacenar la consulta actual

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cursos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCursos)
        searchView = view.findViewById(R.id.searchViewCursos)
        fab = view.findViewById(R.id.fabCursos)
        progressBar = view.findViewById(R.id.progressBar)

        // Configurar SearchView
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.requestFocus()

        // Configurar RecyclerView y Adapter
        adapter = CursosAdapter(
            onEdit = { curso, _ -> mostrarDialogoCurso(curso) },
            onDelete = { curso, _ -> viewModel.deleteCurso(curso.idCurso) }
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
            mostrarDialogoCurso(null)
        }

        // Configurar deslizamiento para eliminar/editar usando la función de extensión
        recyclerView.enableSwipeActions(
            onSwipeLeft = { position ->
                adapter.onSwipeDelete(position)
            },
            onSwipeRight = { position ->
                val curso = adapter.getCursoAt(position)
                adapter.triggerEdit(curso, position)
            }
        )

        // Observar estados del ViewModel
        viewModel.cursosState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CursosState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }

                is CursosState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    adapter.updateCursos(state.cursos)
                    // Reaplicar el filtro si hay una consulta activa
                    currentSearchQuery?.let { adapter.filter.filter(it) }
                }

                is CursosState.Error -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = false
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ActionState.Loading -> {
                    progressBar.isVisible = true
                    fab.isEnabled = false
                }

                is ActionState.Success -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true

                    val color = when {
                        state.message.contains("creado", true) ||
                                state.message.contains("actualizado", true) -> R.color.colorAccent
                        state.message.contains("eliminado", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }

                    Notificador.show(
                        view = requireView(),
                        mensaje = state.message,
                        colorResId = color,
                        anchorView = fab
                    )
                }

                is ActionState.Error -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }

        return view
    }

    private fun mostrarDialogoCurso(curso: Curso?) {
        val campos = listOf(
            CampoFormulario(
                "codigo", "Código", "texto", obligatorio = true,
                editable = curso == null
            ),
            CampoFormulario("nombre", "Nombre", "texto", obligatorio = true),
            CampoFormulario("creditos", "Créditos", "number", obligatorio = true),
            CampoFormulario("horasSemanales", "Horas Semanales", "number", obligatorio = true)
        )

        val datosIniciales = curso?.let {
            mapOf(
                "codigo" to it.codigo,
                "nombre" to it.nombre,
                "creditos" to it.creditos.toString(),
                "horasSemanales" to it.horasSemanales.toString()
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment(
            titulo = if (curso == null) "Nuevo Curso" else "Editar Curso",
            campos = campos,
            datosIniciales = datosIniciales,
            onGuardar = { datosMap ->
                val nuevoCurso = Curso(
                    idCurso = curso?.idCurso ?: 0,
                    codigo = datosMap["codigo"] ?: "",
                    nombre = datosMap["nombre"] ?: "",
                    creditos = datosMap["creditos"]?.toLongOrNull() ?: 0,
                    horasSemanales = datosMap["horasSemanales"]?.toLongOrNull() ?: 0
                )

                if (curso == null) {
                    viewModel.createCurso(nuevoCurso)
                } else {
                    viewModel.updateCurso(nuevoCurso)
                }
            },
            onCancel = {
                adapter.restoreFilteredList()
                currentSearchQuery?.let { adapter.filter.filter(it) }
            }
        )

        dialog.show(parentFragmentManager, "DialogFormularioCurso")
    }
}
