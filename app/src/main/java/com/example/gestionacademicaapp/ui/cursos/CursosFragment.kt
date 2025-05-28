package com.example.gestionacademicaapp.ui.cursos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.utils.Notificador
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CursosFragment : Fragment() {

    private val viewModel: CursosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CursosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View // Añadiremos un ProgressBar al layout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cursos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCursos)
        searchView = view.findViewById(R.id.searchViewCursos)
        fab = view.findViewById(R.id.fabCursos)
        progressBar = view.findViewById(R.id.progressBar) // Añadiremos este ID al layout

        // Configurar SearchView
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.requestFocus()

        // Configurar RecyclerView y Adapter
        adapter = CursosAdapter(
            onEdit = { curso, position -> mostrarDialogoCurso(curso, position) },
            onDelete = { curso, position -> viewModel.deleteCurso(curso.idCurso) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Configurar búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
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
            mostrarDialogoCurso(null, null)
        }

        // Configurar deslizamiento para eliminar/editar
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                when (direction) {
                    ItemTouchHelper.LEFT -> adapter.onSwipeDelete(position)
                    ItemTouchHelper.RIGHT -> {
                        val curso = adapter.getCursoAt(position)
                        adapter.triggerEdit(curso, position)
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Observar estados del ViewModel
        viewModel.cursosState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CursosState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }

                is CursosState.Success -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateCursos(state.cursos)
                }

                is CursosState.Error -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ActionState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    fab.isEnabled = false
                }

                is ActionState.Success -> {
                    progressBar.visibility = View.GONE
                    fab.isEnabled = true
                    Notificador.show(requireView(), state.message, R.color.colorPrimary)
                }

                is ActionState.Error -> {
                    progressBar.visibility = View.GONE
                    fab.isEnabled = true
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }

        return view
    }

    private fun mostrarDialogoCurso(curso: Curso?, position: Int?) {
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

        val dialog = DialogFormularioFragment(
            titulo = if (curso == null) "Nuevo Curso" else "Editar Curso",
            campos = campos,
            datosIniciales = datosIniciales
        ) { datosMap ->
            val nuevoCurso = Curso(
                idCurso = curso?.idCurso ?: 0, // El backend asignará el ID al crear
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
        }

        dialog.show(parentFragmentManager, "DialogFormularioCurso")
    }
}
