package com.example.gestionacademicaapp.ui.cursos

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
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.ui.common.validators.CursoValidator
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.setupSearchView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CursosFragment : Fragment() {

    private val viewModel: CursosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CursosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View

    private var allCursos: List<Curso> = emptyList()
    private var editingPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cursos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCursos)
        searchView = view.findViewById(R.id.searchViewCursos)
        fab = view.findViewById(R.id.fabCursos)
        progressBar = view.findViewById(R.id.progressBar)

        setupSearchView(searchView, getString(R.string.search_hint_codigo_nombre)) { query ->
            filterList(query)
        }

        adapter = CursosAdapter(
            onEdit = { curso -> mostrarDialogoCurso(curso) },
            onDelete = { curso -> viewModel.deleteItem(curso.idCurso) }
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
                val curso = adapter.getItemAt(pos)
                viewModel.deleteItem(curso.idCurso)
                adapter.notifyItemChanged(pos)
            },
            onSwipeRight = { pos ->
                editingPosition = pos
                adapter.notifyItemChanged(pos)
                mostrarDialogoCurso(adapter.getItemAt(pos))
            }
        )

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            editingPosition = null
            mostrarDialogoCurso(null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.cursosState.collectLatest { state ->
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
                recyclerView.isVisible = !isAction && allCursos.isNotEmpty()
                fab.isEnabled = !isAction
            }
            is UiState.Success -> {
                progressBar.isVisible = false
                fab.isEnabled = true
                if (!isAction) {
                    recyclerView.isVisible = true
                    if (state.data is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        allCursos = state.data as List<Curso>
                    }
                    filterList(searchView.query.toString())
                }
                state.message?.let { message ->
                    val color = when {
                        message.contains("creado", true) || message.contains("actualizado", true) -> R.color.colorAccent
                        message.contains("eliminado", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }
                    Notificador.show(requireView(), message, color, anchorView = fab)
                }
                if (isAction) {
                    recyclerView.isVisible = allCursos.isNotEmpty()
                    filterList(searchView.query.toString())
                    editingPosition = null
                }
            }
            is UiState.Error -> {
                progressBar.isVisible = false
                // Mantener recyclerView visible si hay datos, incluso tras un error de acción
                recyclerView.isVisible = allCursos.isNotEmpty()
                fab.isEnabled = true
                val message = when (state.type) {
                    ErrorType.DEPENDENCY -> "No se puede eliminar: el curso está asignado a una carrera."
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
        val texto = query?.lowercase()?.trim() ?: ""
        val filtered = if (texto.isEmpty()) {
            allCursos
        } else {
            allCursos.filter {
                it.nombre.lowercase().contains(texto) || it.codigo.lowercase().contains(texto)
            }
        }
        adapter.submitList(filtered)
    }

    private fun mostrarDialogoCurso(curso: Curso?) {
        val validator = CursoValidator()
        val campos = listOf(
            CampoFormulario(
                key = "codigo",
                label = "Código",
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = validator.codigoRequiredError,
                editable = curso == null,
                rules = { value, _ -> validator.validateCodigo(value) }
            ),
            CampoFormulario(
                key = "nombre",
                label = "Nombre",
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = validator.nombreRequiredError,
                rules = { value, _ -> validator.validateNombre(value) }
            ),
            CampoFormulario(
                key = "creditos",
                label = "Créditos",
                tipo = CampoTipo.NUMBER,
                obligatorio = true,
                obligatorioError = validator.creditosRequiredError,
                rules = { value, _ -> validator.validateCreditos(value) }
            ),
            CampoFormulario(
                key = "horasSemanales",
                label = "Horas Semanales",
                tipo = CampoTipo.NUMBER,
                obligatorio = true,
                obligatorioError = validator.horasSemanalesRequiredError,
                rules = { value, _ -> validator.validateHorasSemanales(value) }
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
                codigo = requireNotNull(datosMap["codigo"]) { "Código requerido" },
                nombre = requireNotNull(datosMap["nombre"]) { "Nombre requerido" },
                creditos = datosMap["creditos"]?.toLongOrNull() ?: 0,
                horasSemanales = datosMap["horasSemanales"]?.toLongOrNull() ?: 0
            )
            if (curso == null) viewModel.createItem(nuevoCurso)
            else viewModel.updateItem(nuevoCurso)
        }

        dialog.setOnCancelListener {
            editingPosition?.let { adapter.notifyItemChanged(it) }
        }

        dialog.show(parentFragmentManager, "DialogFormularioCurso")
    }
}
