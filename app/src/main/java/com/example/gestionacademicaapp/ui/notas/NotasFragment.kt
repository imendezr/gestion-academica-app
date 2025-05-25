package com.example.gestionacademicaapp.ui.notas

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
import com.example.gestionacademicaapp.model.Nota
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: NotasAdapter
    private lateinit var fab: FloatingActionButton
    private val listaNotas = mutableListOf<Nota>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notas, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewNotas)
        searchView = view.findViewById(R.id.searchViewNotas)
        fab = view.findViewById(R.id.fabNotas)

        // Datos simulados
        listaNotas.add(Nota(1, 1, "Matemática I", 8.5))
        listaNotas.add(Nota(2, 2, "Programación", 9.0))
        listaNotas.add(Nota(3, 3, "Inglés", 7.0))

        adapter = NotasAdapter(listaNotas)
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
                val nota = listaNotas[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Nota eliminada: ${nota.curso}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar nota: ${nota.curso}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Agregar nota con FAB
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaNotas.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevaNota = Nota(
                id = nuevoId,
                alumnoId = 4, // Se puede asignar dinámicamente más tarde
                curso = "Nuevo Curso $nuevoId",
                nota = 7.5
            )

            adapter.agregarItem(nuevaNota)
            Toast.makeText(requireContext(), "Nota agregada", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
