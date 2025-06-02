package com.example.gestionacademicaapp.ui.cursos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
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

        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = getString(R.string.search_hint_codigo_nombre)

        adapter = CursosAdapter(
            onEdit = { curso -> mostrarDialogoCurso(curso) }
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
            onSwipeLeft = { pos ->
                val curso = adapter.getCursoAt(pos)
                viewModel.deleteCurso(curso.idCurso)
                adapter.notifyItemChanged(pos) // Restaurar inmediatamente
            },
            onSwipeRight = { pos ->
                adapter.notifyItemChanged(pos) // Restaurar inmediatamente
                mostrarDialogoCurso(adapter.getCursoAt(pos))
            }
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
                    viewModel.fetchCursos()
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
                key = "codigo",
                label = "Código",
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = "El código es requerido",
                editable = curso == null,
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
                key = "creditos",
                label = "Créditos",
                tipo = CampoTipo.NUMBER,
                obligatorio = true,
                obligatorioError = "Los créditos son requeridos",
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else {
                        val creditos = value.toLongOrNull()
                        if (creditos == null || creditos !in 1..10) "Debe estar entre 1 y 10"
                        else null
                    }
                }
            ),
            CampoFormulario(
                key = "horasSemanales",
                label = "Horas Semanales",
                tipo = CampoTipo.NUMBER,
                obligatorio = true,
                obligatorioError = "Las horas semanales son requeridas",
                rules = { value, _ ->
                    if (value.isEmpty()) null
                    else {
                        val horas = value.toLongOrNull()
                        if (horas == null || horas !in 1..40) "Debe estar entre 1 y 40"
                        else null
                    }
                }
            )
        )

        val datosIniciales = curso?.let {
            mapOf(
                "codigo" to it.codigo,
                "nombre" to it.nombre,
                "creditos" to it.creditos.toString(),
                "horasSemanales" to it.horasSemanales.toString()
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment.newInstance(
            titulo = if (curso == null) "Nuevo Curso" else "Editar Curso",
            campos = campos,
            datosIniciales = datosIniciales
        )

        dialog.setOnGuardarListener { datosMap ->
            val nuevoCurso = Curso(
                idCurso = curso?.idCurso ?: 0,
                codigo = datosMap["codigo"] ?: "",
                nombre = datosMap["nombre"] ?: "",
                creditos = datosMap["creditos"]?.toLongOrNull() ?: 0,
                horasSemanales = datosMap["horasSemanales"]?.toLongOrNull() ?: 0
            )
            if (curso == null) viewModel.createCurso(nuevoCurso)
            else viewModel.updateCurso(nuevoCurso)
        }

        dialog.show(parentFragmentManager, "DialogFormularioCurso")
    }
}
