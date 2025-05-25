package com.example.gestionacademicaapp.ui.grupos

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
import com.example.gestionacademicaapp.model.Grupo
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GruposFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: GrupoAdapter
    private lateinit var fab: FloatingActionButton
    private val listaGrupos = mutableListOf<Grupo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grupos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewGrupos)
        searchView = view.findViewById(R.id.searchViewGrupos)
        fab = view.findViewById(R.id.fabGrupos)

        // Datos simulados
        listaGrupos.add(Grupo(1, "Grupo A", "Ciclo 2023-1", 30))
        listaGrupos.add(Grupo(2, "Grupo B", "Ciclo 2023-1", 28))

        adapter = GrupoAdapter(listaGrupos)
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
                val grupo = listaGrupos[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Grupo eliminado: ${grupo.nombre}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar grupo: ${grupo.nombre}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Agregar grupo con FAB
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaGrupos.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevoGrupo = Grupo(
                id = nuevoId,
                nombre = "Grupo ${nuevoId}",
                ciclo = "Ciclo 2023-${nuevoId}",
                cantidadAlumnos = 30
            )

            adapter.agregarItem(nuevoGrupo)
            Toast.makeText(requireContext(), "Grupo agregado", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
