package com.example.gestionacademicaapp.ui.carreras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class CarrerasFragment : Fragment() {
    private val viewModel: CarrerasViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CarrerasAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View
    private var currentSearchQuery: String? = null // Para almacenar la consulta actual

    //private val listaCarreras = mutableListOf<Carrera>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_carreras, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCarreras)
        searchView = view.findViewById(R.id.searchViewCarreras)
        fab = view.findViewById(R.id.fabCarreras)
        progressBar = view.findViewById(R.id.progressBar)

//        Datos simulados
//        listaCarreras.add(Carrera(1, "INFO", "Ingeniería Informática", "Diseño y desarrollo de software"))
//        listaCarreras.add(Carrera(2, "ADME", "Administración de Empresas", "Gestión empresarial y liderazgo"))
//        listaCarreras.add(Carrera(3, "MED", "Medicina", "Formación médica integral"))

        // Configurar SearchView
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.requestFocus()

        // Configurar RecyclerView y adaptador
        adapter = CarrerasAdapter(
            onEdit = { carrera, position -> mostrarDialogoCarrera(carrera, position) },
            onDelete = { carrera, position -> viewModel.deleteCarrera(carrera.idCarrera) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Configurar búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText
                adapter.filter.filter(newText)
                return false
            }
        })
        // Configurar FAB
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && fab.isExtended) fab.shrink()
                else if (dy < -10 && !fab.isExtended) fab.extend()
            }
        })
        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            mostrarDialogoCarrera(null, null)
        }

        // Configurar deslizamiento para eliminar/editar usando la función de extensión
        recyclerView.enableSwipeActions(
            onSwipeLeft = { position ->
                adapter.onSwipeDelete(position)
            },
            onSwipeRight = { position ->
                val carrera = adapter.getCarreraAt(position)
                adapter.triggerEdit(carrera, position)
            }
        )

        // Observar estados del ViewModel
        viewModel.carrerasState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CarrerasState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }

                is CarrerasState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    adapter.updateCarreras(state.carreras)
                    // Reaplicar el filtro si hay una consulta activa
                    currentSearchQuery?.let { adapter.filter.filter(it) }
                }

                is CarrerasState.Error -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = false
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ActionState.Loading -> {
                    progressBar.isVisible = true
                    fab.isEnabled = false
                }

                is ActionState.Success -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(requireView(), state.message, R.color.colorPrimary)
                }

                is ActionState.Error -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(requireView(), state.message, R.color.colorError)
                }
            }
        }

        return view
    }
    private fun mostrarDialogoCarrera(carrera: Carrera?, position: Int?) {
        val campos = listOf(
            CampoFormulario(
                "codigo",
                "Código",
                "texto",
                obligatorio = true,
                editable = carrera == null
            ),
            CampoFormulario("nombre", "Nombre", "texto", obligatorio = true),
            CampoFormulario("titulo", "Titulo", "texto", obligatorio = true)
        )

        val datosIniciales = carrera?.let {
            mapOf(
                "codigo" to it.codigo,
                "nombre" to it.nombre,
                "titulo" to it.titulo
            )
        } ?: emptyMap()

        val dialog = DialogFormularioFragment(
            titulo = if (carrera == null) "Nueva Carrera" else "Editar Carrera",
            campos = campos,
            datosIniciales = datosIniciales,
            onGuardar = { datosMap ->
                val nuevaCarrera = Carrera(
                    idCarrera = carrera?.idCarrera ?: 0,
                    codigo = datosMap["codigo"] ?: "",
                    nombre = datosMap["nombre"] ?: "",
                    titulo = datosMap["titulo"] ?: ""
                )

                if (carrera == null) {
                    viewModel.createCarrera(nuevaCarrera)
                } else {
                    viewModel.updateCarrera(nuevaCarrera)
                }
            },
            onCancel = {
                // Restaurar la lista filtrada al cancelar
                adapter.restoreFilteredList()
                // Opcionalmente, reaplicar el filtro si hay una consulta activa
                currentSearchQuery?.let { adapter.filter.filter(it) }
            }
        )

        dialog.show(parentFragmentManager, "DialogFormularioCarrera")
    }
}
