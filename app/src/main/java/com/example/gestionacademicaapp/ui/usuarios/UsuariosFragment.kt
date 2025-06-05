package com.example.gestionacademicaapp.ui.usuarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.ui.common.validators.UsuarioValidator
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.setupSearchView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsuariosFragment : Fragment() {

    private val viewModel: UsuariosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: UsuariosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View

    private var allUsuarios: List<Usuario> = emptyList()
    private var editingPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_usuarios, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewUsuarios)
        searchView = view.findViewById(R.id.searchViewUsuarios)
        fab = view.findViewById(R.id.fabAddUsuario)
        progressBar = view.findViewById(R.id.progressBar)

        setupSearchView(searchView, getString(R.string.search_hint_usuario)) { query ->
            filterList(query)
        }

        adapter = UsuariosAdapter(
            onEdit = { viewModel.prepareUserForEdit(it) },
            onDelete = { viewModel.deleteUsuario(it.idUsuario, requireContext()) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && fab.isExtended) fab.shrink()
                else if (dy < -10 && !fab.isExtended) fab.extend()
            }
        })

        recyclerView.enableSwipeActions(
            onSwipeLeft = { pos ->
                val usuario = adapter.getItemAt(pos)
                viewModel.deleteUsuario(usuario.idUsuario, requireContext())
                adapter.notifyItemChanged(pos)
            },
            onSwipeRight = { pos ->
                editingPosition = pos
                viewModel.prepareUserForEdit(adapter.getItemAt(pos))
                adapter.notifyItemChanged(pos)
            }
        )

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            editingPosition = null
            showUsuarioDialog(null, mapOf("tipo" to "Administrador"))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.usuariosState.collectLatest { state ->
                        updateUiState(state, isAction = false)
                    }
                }
                launch {
                    viewModel.actionState.collectLatest { state ->
                        updateUiState(state, isAction = true)
                    }
                }
                launch {
                    viewModel.editingUserData.collectLatest { data ->
                        data?.let { (usuario, datosIniciales) ->
                            showUsuarioDialog(usuario, datosIniciales)
                            viewModel.clearEditingUserData()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun updateUiState(state: UiState<*>, isAction: Boolean) {
        when (state) {
            is UiState.Loading -> {
                progressBar.isVisible = true
                recyclerView.isVisible = !isAction && allUsuarios.isNotEmpty()
                fab.isEnabled = !isAction
            }
            is UiState.Success -> {
                progressBar.isVisible = false
                fab.isEnabled = true
                if (!isAction) {
                    recyclerView.isVisible = true
                    if (state.data is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        allUsuarios = state.data as List<Usuario>
                    }
                    filterList(searchView.query.toString())
                }
                state.message?.let { message ->
                    val color = when {
                        message.contains("creado", true) || message.contains("actualizado", true) -> R.color.colorAccent
                        message.contains("eliminado", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }
                    Notificador.show(requireView(), message, color, anchorView = fab)
                }
                if (isAction) {
                    recyclerView.isVisible = allUsuarios.isNotEmpty()
                    filterList(searchView.query.toString())
                    editingPosition = null
                }
            }
            is UiState.Error -> {
                progressBar.isVisible = false
                recyclerView.isVisible = allUsuarios.isNotEmpty()
                fab.isEnabled = true
                val message = when (state.type) {
                    ErrorType.DEPENDENCY -> state.message
                    ErrorType.VALIDATION -> "Error de validación: ${state.message}"
                    ErrorType.GENERAL -> state.message
                }
                Notificador.show(requireView(), message, R.color.colorError, anchorView = fab)
                if (isAction) {
                    filterList(searchView.query.toString())
                }
            }
        }
    }

    private fun filterList(query: String?) {
        val texto = query?.lowercase()?.trim() ?: ""
        val filtered = if (texto.isEmpty()) {
            allUsuarios
        } else {
            allUsuarios.filter {
                it.cedula.lowercase().contains(texto) || it.tipo.lowercase().contains(texto)
            }
        }
        adapter.submitList(filtered)
    }

    private fun buildCampos(tipo: String, isEditMode: Boolean = false): List<CampoFormulario> {
        val validator = UsuarioValidator()
        val campos = mutableListOf(
            CampoFormulario(
                key = "cedula",
                label = "Cédula",
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                obligatorioError = validator.cedulaRequiredError,
                rules = { value, _ -> validator.validateCedula(value) }
            ),
            CampoFormulario(
                key = "clave",
                label = "Clave",
                tipo = CampoTipo.TEXT,
                obligatorio = !isEditMode,
                obligatorioError = validator.claveRequiredError,
                rules = { value, _ -> validator.validateClave(value, isEditMode) }
            ),
            CampoFormulario(
                key = "tipo",
                label = "Tipo de Usuario",
                tipo = CampoTipo.SPINNER,
                obligatorio = true,
                obligatorioError = validator.tipoRequiredError,
                opciones = listOf("Administrador", "Matriculador", "Profesor", "Alumno").map { it to it },
                editable = !isEditMode,
                rules = { value, _ -> validator.validateTipo(value) },
                onValueChanged = { newTipo -> updateDynamicFields(newTipo) }
            )
        )

        when (tipo) {
            "Alumno" -> campos.addAll(listOf(
                CampoFormulario(
                    key = "nombre",
                    label = "Nombre",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    obligatorioError = validator.nombreRequiredError,
                    rules = { value, _ -> validator.validateNombre(value) }
                ),
                CampoFormulario(
                    key = "telefono",
                    label = "Teléfono",
                    tipo = CampoTipo.NUMBER,
                    obligatorio = true,
                    obligatorioError = validator.telefonoRequiredError,
                    rules = { value, _ -> validator.validateTelefono(value) }
                ),
                CampoFormulario(
                    key = "email",
                    label = "Email",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    obligatorioError = validator.emailRequiredError,
                    rules = { value, _ -> validator.validateEmail(value) }
                ),
                CampoFormulario(
                    key = "fechaNacimiento",
                    label = "Fecha de Nacimiento",
                    tipo = CampoTipo.DATE,
                    obligatorio = true,
                    obligatorioError = validator.fechaNacimientoRequiredError,
                    rules = { value, _ -> validator.validateFechaNacimiento(value) }
                ),
                CampoFormulario(
                    key = "carrera",
                    label = "Carrera",
                    tipo = CampoTipo.SPINNER,
                    obligatorio = true,
                    obligatorioError = validator.carreraRequiredError,
                    opciones = viewModel.carreras.value.map { it.idCarrera.toString() to it.nombre },
                    rules = { value, _ -> validator.validateCarrera(value, viewModel.carreras.value) }
                )
            ))
            "Profesor" -> campos.addAll(listOf(
                CampoFormulario(
                    key = "nombre",
                    label = "Nombre",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    obligatorioError = validator.nombreRequiredError,
                    rules = { value, _ -> validator.validateNombre(value) }
                ),
                CampoFormulario(
                    key = "telefono",
                    label = "Teléfono",
                    tipo = CampoTipo.NUMBER,
                    obligatorio = true,
                    obligatorioError = validator.telefonoRequiredError,
                    rules = { value, _ -> validator.validateTelefono(value) }
                ),
                CampoFormulario(
                    key = "email",
                    label = "Correo",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    obligatorioError = validator.emailRequiredError,
                    rules = { value, _ -> validator.validateEmail(value) }
                )
            ))
        }
        return campos
    }

    private fun showUsuarioDialog(usuario: Usuario?, datosIniciales: Map<String, String>) {
        val isEditMode = usuario != null
        val tipo = datosIniciales["tipo"] ?: "Administrador"
        val campos = buildCampos(tipo, isEditMode)

        val dialog = DialogFormularioFragment.newInstance(
            titulo = if (isEditMode) "Editar Usuario" else "Nuevo Usuario",
            campos = campos,
            datosIniciales = datosIniciales
        )

        dialog.setOnGuardarListener { datos ->
            viewModel.saveUsuario(datos, usuario?.idUsuario)
        }

        dialog.setOnCancelListener {
            editingPosition?.let { adapter.notifyItemChanged(it) }
        }

        dialog.show(parentFragmentManager, "UserDialog")
    }

    private fun updateDynamicFields(tipo: String) {
        val dialog = parentFragmentManager.findFragmentByTag("UserDialog") as? DialogFormularioFragment
        dialog?.let {
            val currentValues = it.getCurrentValues()
            val campos = buildCampos(tipo)
            it.updateDynamicFields(campos)
            it.setCurrentValues(currentValues)
        }
    }
}
