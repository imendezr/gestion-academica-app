package com.example.gestionacademicaapp.ui.alumnos

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
import com.example.gestionacademicaapp.model.Alumno
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AlumnosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: AlumnosAdapter
    private lateinit var fab: FloatingActionButton
    private val listaAlumnos = mutableListOf<Alumno>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alumnos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewAlumnos)
        searchView = view.findViewById(R.id.searchViewAlumnos)
        fab = view.findViewById(R.id.fabAlumnos)

        // Datos simulados
        listaAlumnos.add(Alumno(1, "Juan Pérez", "Ingeniería Informática"))
        listaAlumnos.add(Alumno(2, "María González", "Administración"))
        listaAlumnos.add(Alumno(3, "Luis Sánchez", "Medicina"))

        // Configurar RecyclerView y adaptador
        adapter = AlumnosAdapter(listaAlumnos)
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
                val alumno = listaAlumnos[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Alumno eliminado: ${alumno.nombre}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar alumno: ${alumno.nombre}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Agregar alumno con FAB
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaAlumnos.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevoAlumno = Alumno(
                id = nuevoId,
                nombre = "Nuevo Alumno $nuevoId",
                carrera = "Carrera de ejemplo"
            )

            adapter.agregarItem(nuevoAlumno)
            Toast.makeText(requireContext(), "Alumno agregado", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
