package com.example.gestionacademicaapp.ui.alumnos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
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

        // Datos simulados adaptados a entidad completa
        listaAlumnos.addAll(
            listOf(
                Alumno(
                    1,
                    "101010101",
                    "Juan Pérez",
                    "8888-8888",
                    "juan@example.com",
                    "2001-03-15",
                    1
                ),
                Alumno(
                    2,
                    "202020202",
                    "María González",
                    "8999-9999",
                    "maria@example.com",
                    "2002-07-20",
                    2
                ),
                Alumno(
                    3,
                    "303030303",
                    "Luis Sánchez",
                    "8777-7777",
                    "luis@example.com",
                    "2000-11-05",
                    3
                )
            )
        )

        adapter = AlumnosAdapter(
            listaAlumnos,
            onEditar = { alumno, position ->
                mostrarDialogoAlumno(parentFragmentManager, alumno) { alumnoActualizado ->
                    adapter.actualizarItem(position, alumnoActualizado)
                    Toast.makeText(requireContext(), "Alumno actualizado", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            onEliminar = { alumno ->
                Toast.makeText(
                    requireContext(),
                    "Alumno eliminado: ${alumno.nombre}",
                    Toast.LENGTH_SHORT
                ).show()
            })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?) = adapter.run {
                filter.filter(newText)
                false
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
                val alumno = listaAlumnos[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.eliminarItem(position)
                    }

                    ItemTouchHelper.RIGHT -> {
                        adapter.notifyItemChanged(position)
                        mostrarDialogoAlumno(parentFragmentManager, alumno) { alumnoActualizado ->
                            adapter.actualizarItem(position, alumnoActualizado)
                            Toast.makeText(
                                requireContext(),
                                "Alumno actualizado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()

            mostrarDialogoAlumno(parentFragmentManager, null) { nuevoAlumno ->
                adapter.agregarItem(nuevoAlumno)
                Toast.makeText(requireContext(), "Alumno agregado", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    fun mostrarDialogoAlumno(
        fragmentManager: FragmentManager,
        alumno: Alumno?,
        onGuardar: (Alumno) -> Unit
    ) {
        val campos = listOf(
            CampoFormulario("cedula", "Cédula", "text", obligatorio = true),
            CampoFormulario("nombre", "Nombre", "text", obligatorio = true),
            CampoFormulario("telefono", "Teléfono", "text"),
            CampoFormulario("email", "Correo", "text"),
            CampoFormulario("fechaNacimiento", "Fecha de nacimiento (YYYY-MM-DD)", "text"),
            CampoFormulario("pkCarrera", "ID Carrera", "number")
        )

        val datosIniciales = alumno?.let {
            mapOf(
                "cedula" to it.cedula,
                "nombre" to it.nombre,
                "telefono" to it.telefono,
                "email" to it.email,
                "fechaNacimiento" to it.fechaNacimiento,
                "pkCarrera" to it.pkCarrera.toString()
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment(
            titulo = if (alumno == null) "Nuevo Alumno" else "Editar Alumno",
            campos = campos,
            datosIniciales = datosIniciales
        ) { datosMap ->
            val nuevoAlumno = Alumno(
                idAlumno = alumno?.idAlumno ?: System.currentTimeMillis(),
                cedula = datosMap["cedula"] ?: "",
                nombre = datosMap["nombre"] ?: "",
                telefono = datosMap["telefono"] ?: "",
                email = datosMap["email"] ?: "",
                fechaNacimiento = datosMap["fechaNacimiento"] ?: "",
                pkCarrera = datosMap["pkCarrera"]?.toLongOrNull() ?: 0L
            )
            onGuardar(nuevoAlumno)
        }

        dialog.show(fragmentManager, "DialogFormularioAlumno")
    }
}
