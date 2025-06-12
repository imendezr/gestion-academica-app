package com.example.gestionacademicaapp.ui.historial_academico

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class HistorialAcademicoFragment : Fragment() {

    private val viewModel: HistorialAcademicoViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: View
    private lateinit var tvEmptyState: TextView
    private val adapter: MatriculaAdapter by lazy {
        MatriculaAdapter()
    }

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial_academico, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        setupRecyclerView()
        observeViewModel()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isAlumno = sessionManager.hasRole("Alumno")
        Log.d(
            "HistorialAcademicoFragment",
            "User role: ${if (isAlumno) "Alumno" else "Administrador"}"
        )

        val cedula = if (isAlumno) {
            sessionManager.getUsuario()?.cedula?.also {
                Log.d("HistorialAcademicoFragment", "Using session cedula: $it")
            } ?: run {
                Log.e("HistorialAcademicoFragment", "No user logged in")
                Notificador.show(
                    view = requireView(),
                    mensaje = "Error: No hay usuario autenticado",
                    colorResId = R.color.colorError,
                    anchorView = null
                )
                findNavController().popBackStack()
                return
            }
        } else {
            val args = try {
                HistorialAcademicoFragmentArgs.fromBundle(requireArguments())
            } catch (e: IllegalArgumentException) {
                Log.e("HistorialAcademicoFragment", "Missing navigation arguments", e)
                Notificador.show(
                    view = requireView(),
                    mensaje = "Error: No se proporcionó cédula",
                    colorResId = R.color.colorError,
                    anchorView = null
                )
                findNavController().popBackStack()
                return
            }
            Log.d("HistorialAcademicoFragment", "Using argument cedula: ${args.cedula}")
            args.cedula
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(!isAlumno)
            title =
                getString(if (isAlumno) R.string.mi_historial_academico else R.string.historial_academico)
        }

        viewModel.loadHistorial(cedula)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historialState.collect { state ->
                    Log.d("HistorialAcademicoFragment", "Historial state: $state")
                    updateUiState(state)
                }
            }
        }
    }

    private fun updateUiState(state: UiState<List<MatriculaAlumnoDto>>) {
        when (state) {
            is UiState.Loading -> {
                progressBar.isVisible = true
                recyclerView.isVisible = false
                tvEmptyState.isVisible = false
            }

            is UiState.Success -> {
                progressBar.isVisible = false
                val matriculas = state.data ?: emptyList()
                recyclerView.isVisible = matriculas.isNotEmpty()
                tvEmptyState.isVisible = matriculas.isEmpty()
                tvEmptyState.text = getString(
                    if (sessionManager.hasRole("Alumno"))
                        R.string.historial_vacio_alumno
                    else
                        R.string.historial_vacio
                )
                adapter.submitList(matriculas)
                Log.d("HistorialAcademicoFragment", "Loaded ${matriculas.size} matriculas")
            }

            is UiState.Error -> {
                progressBar.isVisible = false
                recyclerView.isVisible = false
                tvEmptyState.isVisible = true
                tvEmptyState.text = state.message
                Notificador.show(
                    view = requireView(),
                    mensaje = state.message,
                    colorResId = R.color.colorError,
                    anchorView = null
                )
                Log.e("HistorialAcademicoFragment", "Error: ${state.message}")
            }
        }
    }
}

class MatriculaAdapter : BaseAdapter<MatriculaAlumnoDto, MatriculaAdapter.MatriculaViewHolder>(
    diffCallback = MatriculaDiffCallback,
    onEdit = {},
    onDelete = {}
) {

    inner class MatriculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCurso: TextView = itemView.findViewById(R.id.tvCurso)
        private val tvGrupo: TextView = itemView.findViewById(R.id.tvGrupo)
        private val tvProfesor: TextView = itemView.findViewById(R.id.tvProfesor)
        private val tvNota: TextView = itemView.findViewById(R.id.tvNota)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)

        fun bind(matricula: MatriculaAlumnoDto) {
            tvCurso.text = itemView.context.getString(
                R.string.curso_details,
                matricula.codigoCurso,
                matricula.nombreCurso
            )
            tvGrupo.text = itemView.context.getString(R.string.grupo, matricula.numeroGrupo)
            tvProfesor.text =
                itemView.context.getString(R.string.profesor, matricula.nombreProfesor)
            tvNota.text = itemView.context.getString(
                R.string.nota,
                when (matricula.nota) {
                    -1.0 -> "-"
                    -2.0 -> "-"
                    else -> String.format(Locale.US, "%.2f", matricula.nota)
                }
            )
            tvEstado.text = itemView.context.getString(
                R.string.estado,
                when {
                    matricula.nota >= 70 -> itemView.context.getString(R.string.estado_aprobado)
                    matricula.nota >= 0 -> itemView.context.getString(R.string.estado_reprobado)
                    matricula.nota == -1.0 -> itemView.context.getString(R.string.estado_en_curso)
                    else -> itemView.context.getString(R.string.estado_sin_evaluar)
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatriculaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matricula_alumnos, parent, false)
        return MatriculaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatriculaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val MatriculaDiffCallback = object : DiffUtil.ItemCallback<MatriculaAlumnoDto>() {
            override fun areItemsTheSame(
                oldItem: MatriculaAlumnoDto,
                newItem: MatriculaAlumnoDto
            ): Boolean {
                return oldItem.idMatricula == newItem.idMatricula
            }

            override fun areContentsTheSame(
                oldItem: MatriculaAlumnoDto,
                newItem: MatriculaAlumnoDto
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
