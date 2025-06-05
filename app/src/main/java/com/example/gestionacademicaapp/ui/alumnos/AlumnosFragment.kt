package com.example.gestionacademicaapp.ui.alumnos

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.ui.common.validators.AlumnoValidator
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.clearSwipe
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlumnosFragment : Fragment() {

    private val viewModel: AlumnosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private val adapter: AlumnosAdapter by lazy {
        AlumnosAdapter(
            onEditListener = { alumno, _ -> mostrarDialogoAlumno(alumno) },
            onViewHistorialListener = { alumno, _ ->
                Log.d("AlumnosFragment", "Navigating to Historial for cedula: ${alumno.cedula}")
                val action = AlumnosFragmentDirections.actionAlumnosFragmentToHistorialAcademicoFragment(alumno.cedula)
                findNavController().navigate(action)
            }
        )
    }
    private lateinit var searchView: SearchView
    private lateinit var progressBar: View
    private lateinit var fab: ExtendedFloatingActionButton
    private var itemTouchHelper: ItemTouchHelper? = null
    private var swipedPosition: Int? = null
    private var allItems: List<Alumno> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alumnos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewAlumnos)
        searchView = view.findViewById(R.id.searchViewAlumnos)
        progressBar = view.findViewById(R.id.progressBar)
        fab = view.findViewById(R.id.fabAlumnos)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        fab.setOnClickListener { mostrarDialogoAlumno(null) }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AlumnosFragment", "onViewCreated: Triggering data load")
        viewModel.loadInitialData()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        val paint = Paint().apply { isAntiAlias = true }
        val path = Path()
        val outerCornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics
        )
        val innerCornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        )

        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                swipedPosition = position
                val alumno = adapter.getItemAt(position)
                if (direction == ItemTouchHelper.RIGHT) {
                    Log.d("AlumnosFragment", "Swiped to edit alumno: ${alumno.cedula}")
                    adapter.triggerEdit(alumno, position)
                }
                recyclerView.clearSwipe(position, itemTouchHelper)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_reorder)?.apply {
                    setTint(ContextCompat.getColor(requireContext(), android.R.color.white))
                }

                paint.color = ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light)

                val rect = RectF(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    itemView.left + dX,
                    itemView.bottom.toFloat()
                )

                val radii = floatArrayOf(
                    outerCornerRadius, outerCornerRadius,
                    innerCornerRadius, innerCornerRadius,
                    innerCornerRadius, innerCornerRadius,
                    outerCornerRadius, outerCornerRadius
                )

                path.reset()
                path.addRoundRect(rect, radii, Path.Direction.CW)
                c.drawPath(path, paint)

                icon?.let {
                    val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                    val bounds = Rect(
                        itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin + it.intrinsicWidth,
                        itemView.bottom - iconMargin
                    )
                    it.bounds = bounds
                    it.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }).apply { attachToRecyclerView(recyclerView) }
    }

    private fun setupSearchView() {
        searchView.isIconified = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("AlumnosFragment", "Filtering query: $newText")
                filterList(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.alumnosState.collect { state ->
                        Log.d("AlumnosFragment", "Alumnos state: $state")
                        updateUiState(state, isFetch = true)
                    }
                }
                launch {
                    viewModel.actionState.collect { state ->
                        Log.d("AlumnosFragment", "Action state: $state")
                        updateUiState(state, isFetch = false)
                    }
                }
            }
        }
    }

    private fun updateUiState(state: UiState<*>, isFetch: Boolean) {
        when (state) {
            is UiState.Loading -> {
                progressBar.isVisible = true
                recyclerView.isVisible = false
                Log.d("AlumnosFragment", "Showing loading state")
            }
            is UiState.Success<*> -> {
                progressBar.isVisible = false
                if (isFetch) {
                    @Suppress("UNCHECKED_CAST")
                    allItems = (state.data as? List<Alumno>) ?: emptyList()
                    recyclerView.isVisible = allItems.isNotEmpty()
                    adapter.submitList(allItems)
                    filterList(searchView.query.toString())
                    Log.d("AlumnosFragment", "Success: Loaded ${allItems.size} alumnos")
                } else if (state.message != null) {
                    Notificador.show(
                        view = requireView(),
                        mensaje = state.message,
                        colorResId = R.color.colorAccent,
                        anchorView = fab
                    )
                    Log.d("AlumnosFragment", "Action success: ${state.message}")
                }
            }
            is UiState.Error -> {
                progressBar.isVisible = false
                recyclerView.isVisible = allItems.isNotEmpty()
                Notificador.show(
                    view = requireView(),
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = fab
                )
                if (!isFetch && swipedPosition != null) {
                    val position = swipedPosition!! // swipedPosition is set in onSwiped
                    adapter.notifyItemChanged(position)
                    recyclerView.clearSwipe(position, itemTouchHelper)
                    Log.d("AlumnosFragment", "Error: Reset swipe at position $position")
                    swipedPosition = null
                }
                filterList(searchView.query.toString())
                Log.d("AlumnosFragment", "Error: ${state.message}")
            }
        }
    }

    private fun filterList(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allItems
        } else {
            val queryLower = query.lowercase()
            val carrerasMap = viewModel.carrerasState.value.associate { it.idCarrera to it.nombre.lowercase() }
            allItems.filter {
                it.cedula.lowercase().contains(queryLower) ||
                        it.nombre.lowercase().contains(queryLower) ||
                        (carrerasMap[it.pkCarrera]?.contains(queryLower) ?: false)
            }
        }
        adapter.submitList(filtered)
        recyclerView.isVisible = filtered.isNotEmpty()
        Log.d("AlumnosFragment", "Filtered ${filtered.size} alumnos")
    }

    private fun mostrarDialogoAlumno(alumno: Alumno?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val carreras = viewModel.carrerasState.value
            val opcionesCarrera = carreras.map { it.idCarrera.toString() to it.nombre }

            val campos = listOf(
                CampoFormulario(
                    key = "cedula",
                    label = getString(R.string.cedula),
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    editable = alumno == null,
                    rules = { value, _ -> AlumnoValidator.validateCedula(value) }
                ),
                CampoFormulario(
                    key = "nombre",
                    label = getString(R.string.nombre),
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    rules = { value, _ -> AlumnoValidator.validateNombre(value) }
                ),
                CampoFormulario(
                    key = "telefono",
                    label = getString(R.string.telefono),
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    rules = { value, _ -> AlumnoValidator.validateTelefono(value) }
                ),
                CampoFormulario(
                    key = "email",
                    label = getString(R.string.email),
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    rules = { value, _ -> AlumnoValidator.validateEmail(value) }
                ),
                CampoFormulario(
                    key = "fecha_nacimiento",
                    label = getString(R.string.fecha_nacimiento),
                    tipo = CampoTipo.DATE,
                    obligatorio = true,
                    rules = { value, _ -> AlumnoValidator.validateFechaNacimiento(value) }
                ),
                CampoFormulario(
                    key = "pkCarrera",
                    label = getString(R.string.carrera),
                    tipo = CampoTipo.SPINNER,
                    obligatorio = true,
                    opciones = opcionesCarrera,
                    rules = { value, _ -> AlumnoValidator.validateCarrera(value, carreras) }
                )
            )

            val datosIniciales = if (alumno != null) {
                mapOf(
                    "cedula" to alumno.cedula,
                    "nombre" to alumno.nombre,
                    "telefono" to alumno.telefono,
                    "email" to alumno.email,
                    "fecha_nacimiento" to alumno.fechaNacimiento,
                    "pkCarrera" to alumno.pkCarrera.toString()
                )
            } else {
                emptyMap()
            }

            val dialog = DialogFormularioFragment.newInstance(
                titulo = getString(if (alumno != null) R.string.editar_alumno else R.string.crear_alumno),
                campos = campos,
                datosIniciales = datosIniciales
            ).apply {
                setOnGuardarListener { datosMap ->
                    val newOrUpdatedAlumno = Alumno(
                        idAlumno = alumno?.idAlumno ?: 0L,
                        cedula = datosMap["cedula"]?.takeIf { it != "" } ?: "",
                        nombre = datosMap["nombre"]?.takeIf { it != "" } ?: "",
                        telefono = datosMap["telefono"]?.takeIf { it != "" } ?: "",
                        email = datosMap["email"]?.takeIf { it != "" } ?: "",
                        fechaNacimiento = datosMap["fecha_nacimiento"]?.takeIf { it != "" } ?: "",
                        pkCarrera = datosMap["pkCarrera"]?.toLongOrNull() ?: 0L
                    )
                    if (alumno != null) {
                        Log.d("AlumnosFragment", "Updating alumno: ${newOrUpdatedAlumno.cedula}")
                        viewModel.updateAlumno(newOrUpdatedAlumno)
                    } else {
                        Log.d("AlumnosFragment", "Creating alumno: ${newOrUpdatedAlumno.cedula}")
                        viewModel.createAlumno(newOrUpdatedAlumno)
                    }
                }
                setOnCancelListener {
                    if (swipedPosition != null) {
                        Log.d("AlumnosFragment", "Cancel dialog: Reset swipe at position $swipedPosition")
                        adapter.notifyItemChanged(swipedPosition!!)
                        recyclerView.clearSwipe(swipedPosition!!, itemTouchHelper)
                        swipedPosition = null
                    }
                }
            }
            dialog.show(parentFragmentManager, "DialogFormularioAlumno")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemTouchHelper = null
        Log.d("AlumnosFragment", "onDestroyView: Cleared itemTouchHelper")
    }
}
