package com.example.gestionacademicaapp.ui.cursos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.utils.Notificador
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class CursosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CursosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private val listaCursos = mutableListOf<Curso>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cursos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCursos)
        searchView = view.findViewById(R.id.searchViewCursos)
        fab = view.findViewById(R.id.fabCursos)

        // Forzar SearchView expandido y con foco
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.requestFocus()

        // Datos simulados
        listaCursos.addAll(
            listOf(
                Curso(1, "MATE-101", "Matemática I", 4, 5),
                Curso(2, "PROG-201", "Programación Avanzada", 5, 6),
                Curso(3, "ING-101", "Inglés Básico", 3, 4),
                Curso(4, "HIST-102", "Historia Universal", 2, 3),
                Curso(5, "FIS-202", "Física General", 4, 5),
                Curso(6, "QUIM-110", "Química Orgánica", 4, 4),
                Curso(7, "BIO-130", "Biología Celular", 3, 3),
                Curso(8, "FILO-140", "Filosofía Moderna", 2, 2),
                Curso(9, "COMP-310", "Compiladores", 5, 6),
                Curso(10, "ARTE-210", "Historia del Arte", 2, 3)
            )
        )

        adapter = CursosAdapter(listaCursos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && fab.isExtended) fab.shrink()
                else if (dy < -10 && !fab.isExtended) fab.extend()
            }
        })

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
                val curso = listaCursos[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Notificador.show(
                            requireView(),
                            "Curso eliminado: ${curso.nombre}",
                            R.color.colorError
                        )
                    }

                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        mostrarDialogoCurso(curso, position)
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            mostrarDialogoCurso(null, null)
        }

        return view
    }

    private fun mostrarDialogoCurso(curso: Curso?, position: Int?) {
        val campos = listOf(
            CampoFormulario("codigo", "Código", "text", obligatorio = true),
            CampoFormulario("nombre", "Nombre", "text", obligatorio = true),
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
                idCurso = curso?.idCurso ?: (listaCursos.maxOfOrNull { it.idCurso }?.plus(1) ?: 1),
                codigo = datosMap["codigo"] ?: "",
                nombre = datosMap["nombre"] ?: "",
                creditos = datosMap["creditos"]?.toLongOrNull() ?: 0,
                horasSemanales = datosMap["horasSemanales"]?.toLongOrNull() ?: 0
            )

            if (position != null) {
                adapter.actualizarItem(position, nuevoCurso)
                Notificador.show(requireView(), "Curso actualizado", R.color.colorAccent)
            } else {
                adapter.agregarItem(nuevoCurso)
                Notificador.show(requireView(), "Curso agregado", R.color.colorPrimary)
            }
        }

        dialog.show(parentFragmentManager, "DialogFormularioCurso")
    }
}
