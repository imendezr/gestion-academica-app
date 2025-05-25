package com.example.gestionacademicaapp.ui.carreras

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
import com.example.gestionacademicaapp.model.Carrera
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CarrerasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CarrerasAdapter
    private lateinit var fab: FloatingActionButton
    private val listaCarreras = mutableListOf<Carrera>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_carreras, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCarreras)
        searchView = view.findViewById(R.id.searchViewCarreras)
        fab = view.findViewById(R.id.fabCarreras)

        // Datos simulados
        listaCarreras.add(Carrera(1, "Ingeniería Informática", "Diseño y desarrollo de software"))
        listaCarreras.add(Carrera(2, "Administración de Empresas", "Gestión empresarial y liderazgo"))
        listaCarreras.add(Carrera(3, "Medicina", "Formación médica integral"))

        // Configurar RecyclerView y adaptador
        adapter = CarrerasAdapter(listaCarreras)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Buscar carreras
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

        // Swipe para eliminar o editar
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
                val carrera = listaCarreras[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Carrera eliminada: ${carrera.nombre}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar carrera: ${carrera.nombre}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // FAB para agregar nueva carrera
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaCarreras.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevaCarrera = Carrera(
                id = nuevoId,
                nombre = "Nueva Carrera $nuevoId",
                descripcion = "Descripción de ejemplo"
            )

            adapter.agregarItem(nuevaCarrera)
            Toast.makeText(requireContext(), "Carrera agregada", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
