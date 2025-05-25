package com.example.gestionacademicaapp.ui.profesores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Profesor
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfesoresFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: ProfesoresAdapter
    private lateinit var fab: FloatingActionButton
    private val listaProfesores = mutableListOf<Profesor>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profesores, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewProfesores)
        searchView = view.findViewById(R.id.searchViewProfesores)
        fab = view.findViewById(R.id.fabProfesores)

        // Datos simulados
        listaProfesores.add(Profesor(1, "Laura Fernández", "Matemáticas"))
        listaProfesores.add(Profesor(2, "Carlos Pérez", "Física"))
        listaProfesores.add(Profesor(3, "Ana Gómez", "Lengua y Literatura"))

        adapter = ProfesoresAdapter(listaProfesores)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        // Swipe: eliminar o editar
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val profesor = listaProfesores[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Profesor eliminado: ${profesor.nombre}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar profesor: ${profesor.nombre}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Agregar profesor con FAB
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaProfesores.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevoProfesor = Profesor(
                id = nuevoId,
                nombre = "Nuevo Profesor $nuevoId",
                especialidad = "Especialidad de ejemplo"
            )

            adapter.agregarItem(nuevoProfesor)
            Toast.makeText(requireContext(), "Profesor agregado", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
