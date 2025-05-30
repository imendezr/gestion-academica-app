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
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
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

    private var currentSearchQuery: String? = null
    private var cursosOriginal: List<Curso> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cursos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCursos)
        searchView = view.findViewById(R.id.searchViewCursos)
        fab = view.findViewById(R.id.fabCursos)
        progressBar = view.findViewById(R.id.progressBar)

        // SearchView
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = getString(R.string.search_hint_codigo_nombre)

        adapter = CursosAdapter(
            onEdit = { curso -> mostrarDialogoCurso(curso) },
            onDelete = { curso -> viewModel.deleteCurso(curso.idCurso) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText
                filtrarLista(newText)
                return true
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && fab.isExtended) fab.shrink()
                else if (dy < -10 && !fab.isExtended) fab.extend()
            }
        })

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            mostrarDialogoCurso(null)
        }

        recyclerView.enableSwipeActions(
            onSwipeLeft = { pos -> adapter.onSwipeDelete(pos) },
            onSwipeRight = { pos -> mostrarDialogoCurso(adapter.getCursoAt(pos)) }
        )

        viewModel.cursosState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ListUiState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }

                is ListUiState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    cursosOriginal = state.data
                    filtrarLista(currentSearchQuery)
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
                        state.data.contains("creado", true) ||
                                state.data.contains("actualizado", true) -> R.color.colorAccent

                        state.data.contains("eliminado", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }
                    Notificador.show(requireView(), state.data, color, anchorView = fab)
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

    private fun filtrarLista(query: String?) {
        val texto = query?.lowercase()?.trim() ?: ""
        if (texto.isEmpty()) {
            adapter.submitList(cursosOriginal)
        } else {
            val filtrados = cursosOriginal.filter {
                it.nombre.lowercase().contains(texto) ||
                        it.codigo.lowercase().contains(texto)
            }
            adapter.submitList(filtrados)
        }
    }

    private fun mostrarDialogoCurso(curso: Curso?) {
        val campos = listOf(
            CampoFormulario(
                "codigo",
                "Código",
                "texto",
                obligatorio = true,
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

        val cursoIndex =
            curso?.let { cursosOriginal.indexOfFirst { c -> c.idCurso == it.idCurso } } ?: -1

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

                if (curso == null) viewModel.createCurso(nuevoCurso)
                else viewModel.updateCurso(nuevoCurso)
            },
            onCancel = {
                if (cursoIndex != -1) {
                    adapter.notifyItemChanged(cursoIndex)
                }
            }
        )

        dialog.show(parentFragmentManager, "DialogFormularioCurso")
    }
}
