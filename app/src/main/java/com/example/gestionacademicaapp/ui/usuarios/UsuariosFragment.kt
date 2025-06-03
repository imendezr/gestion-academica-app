package com.example.gestionacademicaapp.ui.usuarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.databinding.FragmentUsuariosBinding
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.CampoTipo
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ActionState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class UsuariosFragment : Fragment() {

    private var _binding: FragmentUsuariosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsuariosViewModel by viewModels()
    private lateinit var adapter: UsuariosAdapter

    private var originalItems: List<Usuario> = emptyList()
    private var currentEditPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsuariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupFab()
        observeViewModel()
        viewModel.fetchUsuarios()
    }

    private fun setupRecyclerView() {
        adapter = UsuariosAdapter(
            onEdit = { usuario -> viewModel.prepareUserForEdit(usuario) },
            onDelete = { usuario -> viewModel.deleteUsuario(usuario.idUsuario, requireContext()) }
        )
        binding.recyclerViewUsuarios.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@UsuariosFragment.adapter
        }

        binding.recyclerViewUsuarios.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && binding.fabAddUsuario.isExtended) binding.fabAddUsuario.shrink()
                else if (dy < -10 && !binding.fabAddUsuario.isExtended) binding.fabAddUsuario.extend()
            }
        })

        binding.recyclerViewUsuarios.enableSwipeActions(
            onSwipeLeft = { pos ->
                val usuario = adapter.getItemAt(pos)
                viewModel.deleteUsuario(usuario.idUsuario, requireContext())
                adapter.notifyItemChanged(pos)
            },
            onSwipeRight = { pos ->
                currentEditPosition = pos
                val usuario = adapter.getItemAt(pos)
                viewModel.prepareUserForEdit(usuario)
                adapter.notifyItemChanged(pos)
            }
        )
    }

    private fun setupSearchView() {
        binding.searchViewUsuarios.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddUsuario.setOnClickListener {
            binding.searchViewUsuarios.setQuery("", false)
            binding.searchViewUsuarios.clearFocus()
            currentEditPosition = null
            showCreateUserDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.usuarios.observe(viewLifecycleOwner) { usuarios ->
            originalItems = usuarios
            filterList(binding.searchViewUsuarios.query.toString())
            binding.progressBar.visibility = View.GONE
        }
        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = View.GONE
            binding.fabAddUsuario.isEnabled = true
            when (state) {
                is ActionState.Success -> {
                    Notificador.show(
                        view = requireView(),
                        mensaje = "Acción completada con éxito",
                        colorResId = R.color.colorAccent,
                        duracion = Snackbar.LENGTH_SHORT,
                        anchorView = binding.fabAddUsuario
                    )
                    viewModel.fetchUsuarios()
                    currentEditPosition = null
                }
                is ActionState.ValidationError -> {
                    Notificador.show(
                        view = requireView(),
                        mensaje = state.message,
                        colorResId = R.color.colorError,
                        duracion = Snackbar.LENGTH_LONG,
                        anchorView = binding.fabAddUsuario
                    )
                }
                is ActionState.DependencyError -> {
                    Notificador.show(
                        view = requireView(),
                        mensaje = state.message,
                        colorResId = R.color.colorError,
                        duracion = Snackbar.LENGTH_LONG,
                        anchorView = binding.fabAddUsuario
                    )
                }
                is ActionState.Error -> {
                    Notificador.show(
                        view = requireView(),
                        mensaje = state.message,
                        colorResId = R.color.colorError,
                        duracion = Snackbar.LENGTH_LONG,
                        anchorView = binding.fabAddUsuario
                    )
                }
            }
        }
        viewModel.editingUserData.observe(viewLifecycleOwner) { data ->
            data?.let { (usuario, datosIniciales) ->
                showEditUserDialog(usuario, datosIniciales)
                viewModel.clearEditingUserData()
            }
        }
    }

    private fun filterList(query: String?) {
        val texto = query?.lowercase()?.trim() ?: ""
        val filtered = if (texto.isEmpty()) {
            originalItems
        } else {
            originalItems.filter {
                it.cedula.lowercase().contains(texto) || it.tipo.lowercase().contains(texto)
            }
        }
        adapter.submitList(filtered)
    }

    private fun showCreateUserDialog() {
        val defaultTipo = "Administrador"
        val campos = getBaseCampos({ tipo -> updateDynamicFields(tipo) }, isEditMode = false) + getCamposDinamicosPorTipo(defaultTipo)
        val dialog = DialogFormularioFragment.newInstance(
            titulo = "Nuevo Usuario",
            campos = campos,
            datosIniciales = mapOf("tipo" to defaultTipo)
        )
        dialog.setOnGuardarListener { datos ->
            viewModel.saveUsuario(datos)
        }
        dialog.setOnCancelListener {
            currentEditPosition?.let { adapter.notifyItemChanged(it) }
        }
        dialog.show(childFragmentManager, "UserDialog")
    }

    private fun showEditUserDialog(usuario: Usuario, datosIniciales: Map<String, String>) {
        val campos = getBaseCampos(isEditMode = true) + getCamposDinamicosPorTipo(usuario.tipo)
        val dialog = DialogFormularioFragment.newInstance(
            titulo = "Editar Usuario",
            campos = campos,
            datosIniciales = datosIniciales
        )
        dialog.setOnGuardarListener { datos ->
            viewModel.saveUsuario(datos, usuario.idUsuario)
        }
        dialog.setOnCancelListener {
            currentEditPosition?.let { adapter.notifyItemChanged(it) }
        }
        dialog.show(childFragmentManager, null)
    }

    private fun getBaseCampos(onTipoChanged: ((String) -> Unit)? = null, isEditMode: Boolean = false): List<CampoFormulario> {
        return listOf(
            CampoFormulario(
                key = "cedula",
                label = "Cédula",
                tipo = CampoTipo.TEXT,
                obligatorio = true,
                editable = true,
                rules = { value, _ ->
                    if (!value.matches(Regex("^[0-9]{6,10}$"))) "Cédula debe ser numérica y tener entre 6 y 10 dígitos" else null
                }
            ),
            CampoFormulario(
                key = "clave",
                label = "Clave",
                tipo = CampoTipo.TEXT,
                obligatorio = !isEditMode,
                rules = { value, _ ->
                    if (!isEditMode && value.trim().isEmpty()) "La clave no puede estar vacía ni contener solo espacios" else null
                }
            ),
            CampoFormulario(
                key = "tipo",
                label = "Tipo de Usuario",
                tipo = CampoTipo.SPINNER,
                obligatorio = true,
                opciones = listOf("Administrador", "Matriculador", "Profesor", "Alumno").map { it to it },
                editable = onTipoChanged != null,
                onValueChanged = onTipoChanged
            )
        )
    }

    private fun getCamposDinamicosPorTipo(tipo: String): List<CampoFormulario> {
        return when (tipo) {
            "Alumno" -> listOf(
                CampoFormulario(
                    key = "nombre",
                    label = "Nombre",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    rules = { value, _ ->
                        if (value.trim().isEmpty()) "El nombre no puede estar vacío" else null
                    }
                ),
                CampoFormulario(
                    key = "telefono",
                    label = "Teléfono",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true
                ),
                CampoFormulario(
                    key = "email",
                    label = "Email",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    rules = { value, _ ->
                        if (!value.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))) "Email inválido" else null
                    }
                ),
                CampoFormulario(
                    key = "fechaNacimiento",
                    label = "Fecha de Nacimiento",
                    tipo = CampoTipo.DATE,
                    obligatorio = true,
                    rules = { value, _ ->
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                        val date = LocalDate.parse(value, formatter)
                        if (date.isAfter(LocalDate.now())) "La fecha de nacimiento no puede ser futura" else null
                    }
                ),
                CampoFormulario(
                    key = "carrera",
                    label = "Carrera",
                    tipo = CampoTipo.SPINNER,
                    obligatorio = true,
                    opciones = viewModel.carreras.value?.map { carrera ->
                        carrera.idCarrera.toString() to carrera.nombre
                    } ?: emptyList(),
                    editable = viewModel.carreras.value?.isNotEmpty() == true,
                    rules = { _, _ ->
                        if (viewModel.carreras.value?.isEmpty() == true) "No hay carreras disponibles" else null
                    }
                )
            )
            "Profesor" -> listOf(
                CampoFormulario(
                    key = "nombre",
                    label = "Nombre",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    rules = { value, _ ->
                        if (value.trim().isEmpty()) "El nombre no puede estar vacío" else null
                    }
                ),
                CampoFormulario(
                    key = "telefono",
                    label = "Teléfono",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true
                ),
                CampoFormulario(
                    key = "email",
                    label = "Email",
                    tipo = CampoTipo.TEXT,
                    obligatorio = true,
                    rules = { value, _ ->
                        if (!value.matches(Regex("^[a-z0-9A-Z._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))) "Email inválido" else null
                    }
                )
            )
            else -> emptyList()
        }
    }

    private fun updateDynamicFields(tipo: String) {
        val dialog = childFragmentManager.findFragmentByTag("UserDialog") as? DialogFormularioFragment
        dialog?.let { dialogFragment ->
            val currentValues = dialogFragment.getCurrentValues()
            val camposBase = getBaseCampos(onTipoChanged = { newTipo -> updateDynamicFields(newTipo) })
            val camposActualizados = camposBase + getCamposDinamicosPorTipo(tipo)
            dialogFragment.updateDynamicFields(camposActualizados)
            dialogFragment.setCurrentValues(currentValues)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
