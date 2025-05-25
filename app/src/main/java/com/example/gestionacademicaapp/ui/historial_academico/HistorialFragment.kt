package com.example.gestionacademicaapp.ui.historial_academico

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
import com.example.gestionacademicaapp.model.Historial
import com.example.gestionacademicaapp.ui.historial.HistorialAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HistorialFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: HistorialAdapter
    private lateinit var fab: FloatingActionButton
    private val listaHistorial = mutableListOf<Historial>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        searchView = view.findViewById(R.id.searchViewHistorial)
        fab = view.findViewById(R.id.fabHistorial)

        // Datos simulados
        listaHistorial.add(Historial(1, 1, "Ciclo 2023-1", "Matemática I", 8.5, "2023-01-15"))
        listaHistorial.add(Historial(2, 2, "Ciclo 2023-1", "Programación", 9.0, "2023-01-20"))
        listaHistorial.add(Historial(3, 3, "Ciclo 2023-1", "Inglés Básico", 7.0, "2023-01-25"))

        adapter = HistorialAdapter(listaHistorial)
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
                val historial = listaHistorial[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Historial eliminado: ${historial.curso}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar historial: ${historial.curso}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Agregar historial con FAB
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaHistorial.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevoHistorial = Historial(
                id = nuevoId,
                alumnoId = 4, // Se asigna dinámicamente más tarde
                ciclo = "Ciclo 2023-2",
                curso = "Nuevo Curso",
                nota = 8.0,
                fecha = "2023-03-01"
            )

            adapter.agregarItem(nuevoHistorial)
            Toast.makeText(requireContext(), "Historial agregado", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
