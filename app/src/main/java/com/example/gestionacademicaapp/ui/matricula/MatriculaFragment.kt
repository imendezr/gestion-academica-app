package com.example.gestionacademicaapp.ui.matricula

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
import com.example.gestionacademicaapp.model.Matricula
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MatriculaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: MatriculaAdapter
    private lateinit var fab: FloatingActionButton
    private val listaMatriculas = mutableListOf<Matricula>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_matricula, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMatriculas)
        searchView = view.findViewById(R.id.searchViewMatriculas)
        fab = view.findViewById(R.id.fabMatriculas)

        // Datos simulados
        listaMatriculas.add(Matricula(1, 1, "Matemática I", "2023-01-01"))
        listaMatriculas.add(Matricula(2, 2, "Programación", "2023-01-05"))
        listaMatriculas.add(Matricula(3, 3, "Inglés Básico", "2023-02-01"))

        // Configurar RecyclerView y adaptador
        adapter = MatriculaAdapter(listaMatriculas)
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
                val matricula = listaMatriculas[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Matrícula eliminada: ${matricula.curso}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar matrícula: ${matricula.curso}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Agregar matrícula con FAB
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaMatriculas.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevaMatricula = Matricula(
                id = nuevoId,
                alumnoId = 4, // Se asigna dinámicamente más tarde
                curso = "Nuevo Curso $nuevoId",
                fecha = "2023-03-01"
            )

            adapter.agregarItem(nuevaMatricula)
            Toast.makeText(requireContext(), "Matrícula agregada", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
