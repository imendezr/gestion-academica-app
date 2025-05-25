package com.example.gestionacademicaapp.ui.usuarios

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
import com.example.gestionacademicaapp.model.Usuario
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UsuariosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var fab: FloatingActionButton
    private val listaUsuarios = mutableListOf<Usuario>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_usuarios, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewUsuarios)
        searchView = view.findViewById(R.id.searchViewUsuarios)
        fab = view.findViewById(R.id.fabUsuarios)

        // Datos simulados
        listaUsuarios.add(Usuario(1, "admin", "Administrador", "admin@universidad.com", "admin123", "Administrador"))
        listaUsuarios.add(Usuario(2, "juan", "Juan Pérez", "juan@universidad.com", "juan123", "Alumno"))
        listaUsuarios.add(Usuario(3, "maria", "María González", "maria@universidad.com", "maria123", "Profesor"))

        adapter = UsuarioAdapter(listaUsuarios)
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
                val usuario = listaUsuarios[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                        Toast.makeText(requireContext(), "Usuario eliminado: ${usuario.username}", Toast.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Editar usuario: ${usuario.username}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Agregar usuario con FAB
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            val nuevoId = listaUsuarios.maxOfOrNull { it.id }?.plus(1) ?: 1
            val nuevoUsuario = Usuario(
                id = nuevoId,
                username = "usuario$nuevoId",
                nombre = "Nuevo Usuario $nuevoId",
                correo = "usuario$nuevoId@universidad.com",
                password = "usuario$123",
                tipoUsuario = "Alumno"
            )

            adapter.agregarItem(nuevoUsuario)
            Toast.makeText(requireContext(), "Usuario agregado", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
