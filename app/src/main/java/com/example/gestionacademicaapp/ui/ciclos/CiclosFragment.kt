package com.example.gestionacademicaapp.ui.ciclos

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
import com.example.gestionacademicaapp.model.Ciclo
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CiclosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CicloAdapter
    private lateinit var fab: FloatingActionButton
    private val listaCiclos = mutableListOf<Ciclo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ciclos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCiclos)
        searchView = view.findViewById(R.id.searchViewCiclos)
        fab = view.findViewById(R.id.fabCiclos)

        // Datos simulados
        listaCiclos.add(Ciclo(1, "Ciclo 2023-1", "Primer ciclo del 2023", "2023-01-01", "2023-06-30"))
        listaCiclos.add(Ciclo(2, "Ciclo 2023-2", "Segundo ciclo del 2023", "2023-07-01", "2023-12-31"))

        adapter = CicloAdapter(listaCiclos)
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
                val ciclo = listaCiclos[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Ciclo eliminado: ${ciclo.nombre}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar ciclo: ${ciclo.nombre}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Agregar ciclo con FAB
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaCiclos.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevoCiclo = Ciclo(
                id = nuevoId,
                nombre = "Ciclo 2023-${nuevoId}",
                descripcion = "Descripción del ciclo ${nuevoId}",
                fechaInicio = "2023-01-01",
                fechaFin = "2023-06-30"
            )

            adapter.agregarItem(nuevoCiclo)
            Toast.makeText(requireContext(), "Ciclo agregado", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
