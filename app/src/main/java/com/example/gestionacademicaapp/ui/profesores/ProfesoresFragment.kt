package com.example.gestionacademicaapp.ui.profesores

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.clearSwipe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfesoresFragment : Fragment() {

    private val viewModel: ProfesoresViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private val adapter: ProfesoresAdapter by lazy {
        ProfesoresAdapter(
            onEditListener = { profesor, _ -> mostrarDialogoProfesor(profesor) }
        )
    }
    private lateinit var searchView: SearchView
    private lateinit var progressBar: View
    private var itemTouchHelper: ItemTouchHelper? = null
    private var swipedPosition: Int? = null
    private var allItems: List<Profesor> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profesores, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewProfesores)
        searchView = view.findViewById(R.id.searchViewProfesores)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        return view
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
            0, ItemTouchHelper.RIGHT // Only allow swipe-right
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.RIGHT) {
                    swipedPosition = position
                    val profesor = adapter.getItemAt(position)
                    adapter.triggerEdit(profesor, position)
                }
                // Reset swipe state
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
                    outerCornerRadius, outerCornerRadius, // superior-izquierda
                    innerCornerRadius, innerCornerRadius, // superior-derecha
                    innerCornerRadius, innerCornerRadius, // inferior-derecha
                    outerCornerRadius, outerCornerRadius  // inferior-izquierda
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
                filterList(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.profesoresState.collect { state ->
                        updateUiState(state, isFetch = true)
                    }
                }
                launch {
                    viewModel.actionState.collect { state ->
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
            }
            is UiState.Success<*> -> {
                progressBar.isVisible = false
                if (isFetch) {
                    @Suppress("UNCHECKED_CAST")
                    allItems = (state.data as? List<Profesor>) ?: emptyList()
                    recyclerView.isVisible = allItems.isNotEmpty()
                    adapter.submitList(allItems)
                    filterList(searchView.query.toString())
                } else if (state.message != null) {
                    Notificador.show(
                        view = requireView(),
                        mensaje = state.message,
                        colorResId = R.color.colorAccent,
                        anchorView = null // No FAB
                    )
                }
            }
            is UiState.Error -> {
                progressBar.isVisible = false
                recyclerView.isVisible = allItems.isNotEmpty()
                Notificador.show(
                    view = requireView(),
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = null // No FAB
                )
                if (!isFetch && swipedPosition != null) {
                    adapter.notifyItemChanged(swipedPosition!!)
                    recyclerView.clearSwipe(swipedPosition!!, itemTouchHelper)
                    swipedPosition = null
                }
                filterList(searchView.query.toString())
            }
        }
    }

    private fun filterList(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allItems
        } else {
            val queryLower = query.lowercase()
            allItems.filter {
                it.cedula.lowercase().contains(queryLower) ||
                        it.nombre.lowercase().contains(queryLower) ||
                        it.telefono.lowercase().contains(queryLower) ||
                        it.email.lowercase().contains(queryLower)
            }
        }
        adapter.submitList(filtered)
        recyclerView.isVisible = filtered.isNotEmpty()
    }

    private fun mostrarDialogoProfesor(profesor: Profesor?) {
        if (profesor == null) return // Only allow editing existing professors

        val campos = listOf(
            CampoFormulario(
                key = "cedula",
                label = getString(R.string.cedula),
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                editable = true, // Cedula is now editable
                rules = { value, _ -> ProfesorValidator.validateCedula(value) }
            ),
            CampoFormulario(
                key = "nombre",
                label = getString(R.string.nombre),
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                rules = { value, _ -> ProfesorValidator.validateNombre(value) }
            ),
            CampoFormulario(
                key = "telefono",
                label = getString(R.string.telefono),
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                rules = { value, _ -> ProfesorValidator.validateTelefono(value) }
            ),
            CampoFormulario(
                key = "email",
                label = getString(R.string.email),
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                rules = { value, _ -> ProfesorValidator.validateEmail(value) }
            )
        )

        val datosIniciales = mapOf(
            "cedula" to profesor.cedula,
            "nombre" to profesor.nombre,
            "telefono" to profesor.telefono,
            "email" to profesor.email
        )

        val dialog = DialogFormularioFragment.newInstance(
            titulo = getString(R.string.editar_profesor),
            campos = campos,
            datosIniciales = datosIniciales
        ).apply {
            setOnGuardarListener { datosMap ->
                val updatedProfesor = Profesor(
                    idProfesor = profesor.idProfesor,
                    cedula = datosMap["cedula"] ?: "",
                    nombre = datosMap["nombre"] ?: "",
                    telefono = datosMap["telefono"] ?: "",
                    email = datosMap["email"] ?: ""
                )
                viewModel.updateProfesor(updatedProfesor)
            }
            setOnCancelListener {
                if (swipedPosition != null) {
                    adapter.notifyItemChanged(swipedPosition!!)
                    recyclerView.clearSwipe(swipedPosition!!, itemTouchHelper)
                    swipedPosition = null
                }
            }
        }
        dialog.show(parentFragmentManager, "DialogFormularioProfesor")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemTouchHelper = null
    }
}
