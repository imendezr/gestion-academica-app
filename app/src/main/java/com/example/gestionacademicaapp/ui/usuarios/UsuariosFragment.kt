package com.example.gestionacademicaapp.ui.usuarios

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.api.model.support.AlumnoData
import com.example.gestionacademicaapp.data.api.model.support.NuevoUsuario
import com.example.gestionacademicaapp.data.api.model.support.ProfesorData
import com.example.gestionacademicaapp.data.repository.AlumnoRepository
import com.example.gestionacademicaapp.data.repository.CarreraRepository
import com.example.gestionacademicaapp.data.repository.ProfesorRepository
import com.example.gestionacademicaapp.databinding.FragmentUsuariosBinding
import com.example.gestionacademicaapp.ui.MainActivity
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.SessionManager
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.isVisible
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsuariosFragment : Fragment() {

    private val viewModel: UsuariosViewModel by viewModels()

    @Inject
    lateinit var profesorRepository: ProfesorRepository

    @Inject
    lateinit var alumnoRepository: AlumnoRepository

    @Inject
    lateinit var carreraRepository: CarreraRepository

    private var _binding: FragmentUsuariosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsuariosAdapter
    private var usuariosOriginal: List<Usuario> = emptyList()
    private var currentSearchQuery: String? = null
    private var carreras: List<Carrera> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsuariosBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel.idUsuarioActual = SessionManager.getUserId(requireContext())

        setupViews()
        setupObservers()
        loadCarreras()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDialogEvents()
    }

    private fun setupViews() {
        with(binding) {
            searchViewUsuarios.apply {
                isIconified = false
                clearFocus()
                queryHint = getString(R.string.search_hint_codigo_nombre)
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        currentSearchQuery = newText
                        filtrarLista(newText)
                        return true
                    }
                })
            }

            adapter = UsuariosAdapter(
                idUsuarioActual = viewModel.idUsuarioActual,
                onEdit = { usuario ->
                    viewModel.triggerDialog(usuario)
                }
            )
            recyclerViewUsuarios.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@UsuariosFragment.adapter
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 10 && fabUsuarios.isExtended) {
                            fabUsuarios.shrink()
                        } else if (dy < -10 && !fabUsuarios.isExtended) {
                            fabUsuarios.extend()
                        }
                    }
                })
                enableSwipeActions(
                    onSwipeLeft = { position ->
                        val usuario = this@UsuariosFragment.adapter.getUsuarioAt(position)
                        viewModel.deleteUsuario(usuario.idUsuario)
                        recyclerViewUsuarios.postDelayed({
                            if (!recyclerViewUsuarios.isComputingLayout) {
                                this@UsuariosFragment.adapter.notifyItemChanged(position)
                            }
                        }, 300)
                    },
                    onSwipeRight = { position ->
                        val usuario = this@UsuariosFragment.adapter.getUsuarioAt(position)
                        viewModel.triggerDialog(usuario)
                    }
                )
            }

            fabUsuarios.setOnClickListener {
                searchViewUsuarios.setQuery("", false)
                searchViewUsuarios.clearFocus()
                viewModel.triggerDialog(null)
            }
        }
    }

    private fun setupObservers() {
        viewModel.usuariosState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                progressBar.isVisible = state is ListUiState.Loading
                recyclerViewUsuarios.isVisible = state is ListUiState.Success
                when (state) {
                    is ListUiState.Loading -> {}
                    is ListUiState.Success -> {
                        usuariosOriginal = state.data
                        filtrarLista(currentSearchQuery)
                    }
                    is ListUiState.Error -> {
                        Notificador.show(requireView(), state.message, R.color.colorError)
                    }
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                progressBar.isVisible = state is SingleUiState.Loading
                fabUsuarios.isEnabled = state !is SingleUiState.Loading
                when (state) {
                    is SingleUiState.Loading -> {}
                    is SingleUiState.Success -> {
                        val color = when {
                            state.data.contains("creado", true) || state.data.contains("actualizado", true) -> R.color.colorAccent
                            state.data.contains("eliminado", true) -> R.color.colorError
                            else -> R.color.colorPrimary
                        }
                        Notificador.show(requireView(), state.data, color, anchorView = fabUsuarios)
                    }
                    is SingleUiState.Error -> {
                        Notificador.show(requireView(), state.message, R.color.colorError)
                    }
                }
            }
        }
    }

    private fun loadCarreras() {
        viewLifecycleOwner.lifecycleScope.launch {
            carreraRepository.listar()
                .onSuccess {
                    carreras = it
                }
                .onFailure {
                    Notificador.show(
                        requireView(),
                        "Error al cargar carreras: ${it.toUserMessage()}",
                        R.color.colorError
                    )
                }
        }
    }

    private fun filtrarLista(query: String?) {
        val texto = query?.lowercase()?.trim().orEmpty()
        val filtrados = if (texto.isEmpty()) usuariosOriginal else usuariosOriginal.filter { it.cedula.lowercase().contains(texto) }
        adapter.submitList(filtrados)
    }

    private fun addCamposAdicionales(camposList: MutableList<CampoFormulario>, tipo: String?) {
        when (tipo) {
            "Profesor" -> {
                camposList.add(CampoFormulario(key = "nombre", label = "Nombre", tipo = "text", obligatorio = true, editable = true))
                camposList.add(CampoFormulario(key = "telefono", label = "Teléfono", tipo = "text", obligatorio = true, editable = true))
                camposList.add(
                    CampoFormulario(
                        key = "email", label = "Email", tipo = "text", obligatorio = true, editable = true,
                        rules = { value -> if (!value.contains("@")) "El email debe ser válido" else null }
                    )
                )
            }
            "Alumno" -> {
                camposList.add(CampoFormulario(key = "nombre", label = "Nombre", tipo = "text", obligatorio = true, editable = true))
                camposList.add(CampoFormulario(key = "telefono", label = "Teléfono", tipo = "text", obligatorio = true, editable = true))
                camposList.add(
                    CampoFormulario(
                        key = "email", label = "Email", tipo = "text", obligatorio = true, editable = true,
                        rules = { value -> if (!value.contains("@")) "El email debe ser válido" else null }
                    )
                )
                camposList.add(CampoFormulario(key = "fechaNacimiento", label = "Fecha de Nacimiento", tipo = "date", obligatorio = true, editable = true))
                camposList.add(
                    CampoFormulario(
                        key = "pkCarrera", label = "Carrera", tipo = "spinner", obligatorio = true, editable = true,
                        opciones = carreras.map { it.idCarrera.toString() to it.nombre }
                    )
                )
            }
        }
    }

    private suspend fun loadDatosIniciales(usuario: Usuario?): Map<String, String> {
        if (usuario == null) {
            return emptyMap()
        }

        val baseDatos = mutableMapOf(
            "cedula" to usuario.cedula,
            "clave" to "",
            "tipo" to usuario.tipo
        )

        when (usuario.tipo) {
            "Profesor" -> {
                profesorRepository.buscarPorCedula(usuario.cedula)
                    .onSuccess { profesor ->
                        baseDatos["nombre"] = profesor.nombre
                        baseDatos["telefono"] = profesor.telefono
                        baseDatos["email"] = profesor.email
                    }
                    .onFailure {
                        Notificador.show(
                            requireView(),
                            "Error al cargar datos del profesor: ${it.toUserMessage()}",
                            R.color.colorError
                        )
                    }
            }
            "Alumno" -> {
                alumnoRepository.buscarPorCedula(usuario.cedula)
                    .onSuccess { alumno ->
                        baseDatos["nombre"] = alumno.nombre
                        baseDatos["telefono"] = alumno.telefono
                        baseDatos["email"] = alumno.email
                        baseDatos["fechaNacimiento"] = alumno.fechaNacimiento
                        baseDatos["pkCarrera"] = alumno.pkCarrera.toString()
                    }
                    .onFailure {
                        Notificador.show(
                            requireView(),
                            "Error al cargar datos del alumno: ${it.toUserMessage()}",
                            R.color.colorError
                        )
                    }
            }
        }
        return baseDatos
    }

    private fun createProfesorData(datosMap: Map<String, String>): ProfesorData {
        return ProfesorData(
            nombre = datosMap["nombre"].orEmpty(),
            telefono = datosMap["telefono"].orEmpty(),
            email = datosMap["email"].orEmpty()
        )
    }

    private fun createAlumnoData(datosMap: Map<String, String>): AlumnoData {
        return AlumnoData(
            nombre = datosMap["nombre"].orEmpty(),
            telefono = datosMap["telefono"].orEmpty(),
            email = datosMap["email"].orEmpty(),
            fechaNacimiento = datosMap["fechaNacimiento"].orEmpty(),
            pkCarrera = datosMap["pkCarrera"]?.toLongOrNull() ?: 1L
        )
    }

    private fun observeDialogEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showDialogEvent.collect { event ->
                if (event == null) return@collect

                val usuario = event.getContentIfNotHandled()

                val datosIniciales = if (usuario != null) loadDatosIniciales(usuario) else emptyMap()
                val campos = mutableListOf<CampoFormulario>()

                campos.add(CampoFormulario(key = "cedula", label = "Cédula", tipo = "text", obligatorio = true, editable = true))
                campos.add(CampoFormulario(key = "clave", label = "Clave", tipo = "text", obligatorio = true, editable = true))
                campos.add(
                    CampoFormulario(
                        key = "tipo",
                        label = "Tipo",
                        tipo = "spinner",
                        obligatorio = true,
                        editable = true,
                        opciones = listOf(
                            "Alumno" to "Alumno",
                            "Profesor" to "Profesor",
                            "Matriculador" to "Matriculador",
                            "Administrador" to "Administrador"
                        ),
                        onValueChanged = { newValue ->
                            val newCampos = mutableListOf<CampoFormulario>()
                            newCampos.add(campos.first { it.key == "cedula" })
                            newCampos.add(campos.first { it.key == "clave" })
                            newCampos.add(campos.first { it.key == "tipo" })
                            addCamposAdicionales(newCampos, newValue)
                            val dialogFragment = parentFragmentManager.findFragmentByTag("DialogFormularioUsuario") as? DialogFormularioFragment
                            dialogFragment?.updateDynamicFields(newCampos)
                        }
                    )
                )

                if (usuario != null) {
                    addCamposAdicionales(campos, usuario.tipo)
                }

                val dialogFragment = DialogFormularioFragment.newInstance(
                    titulo = if (usuario == null) "Nuevo Usuario" else "Editar Usuario",
                    campos = campos,
                    datosIniciales = datosIniciales
                ).apply {
                    setOnGuardarListener { datosMap ->
                        val cedulaNueva = datosMap["cedula"].orEmpty()
                        val claveIngresada = datosMap["clave"]?.trim().orEmpty()
                        val tipoNuevo = datosMap["tipo"].orEmpty()

                        if (usuario == null) {
                            // Crear nuevo usuario
                            if (claveIngresada.isBlank()) {
                                Notificador.show(requireView(), "La clave no puede estar vacía", R.color.colorError)
                                return@setOnGuardarListener
                            }

                            val nuevoUsuario = NuevoUsuario(
                                usuario = Usuario(0, cedulaNueva, claveIngresada, tipoNuevo),
                                profesorData = if (tipoNuevo == "Profesor") createProfesorData(datosMap) else null,
                                alumnoData = if (tipoNuevo == "Alumno") createAlumnoData(datosMap) else null
                            )
                            viewModel.createUsuario(nuevoUsuario)
                        } else {
                            // Actualizar usuario existente
                            fun continuarActualizacion() {
                                val hayCambios = usuario.cedula != cedulaNueva ||
                                        usuario.tipo != tipoNuevo ||
                                        (claveIngresada.isNotBlank() && usuario.idUsuario != viewModel.idUsuarioActual)

                                if (!hayCambios) {
                                    Notificador.show(requireView(), "No se realizaron cambios", R.color.colorAccent)
                                    adapter.notifyItemChanged(usuariosOriginal.indexOfFirst { it.idUsuario == usuario.idUsuario })
                                    return
                                }

                                val claveFinal = if (claveIngresada.isNotBlank() && usuario.idUsuario != viewModel.idUsuarioActual) claveIngresada else usuario.clave ?: ""
                                val usuarioModificado = usuario.copy(cedula = cedulaNueva, clave = claveFinal, tipo = tipoNuevo)

                                viewModel.updateUsuario(
                                    usuarioModificado,
                                    profesorData = if (tipoNuevo == "Profesor") createProfesorData(datosMap) else null,
                                    alumnoData = if (tipoNuevo == "Alumno") createAlumnoData(datosMap) else null
                                )

                                if (usuario.idUsuario == viewModel.idUsuarioActual) {
                                    val actualizado = SessionManager.getUsuario(requireContext())?.copy(
                                        cedula = cedulaNueva,
                                        tipo = tipoNuevo
                                    ) ?: usuarioModificado
                                    SessionManager.setUsuario(requireContext(), actualizado)
                                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    startActivity(intent)
                                }
                            }

                            if (usuario.idUsuario == viewModel.idUsuarioActual) {
                                if (claveIngresada.isBlank()) {
                                    Notificador.show(requireView(), "Debe ingresar su clave actual", R.color.colorError)
                                    adapter.notifyItemChanged(usuariosOriginal.indexOfFirst { it.idUsuario == usuario.idUsuario })
                                    return@setOnGuardarListener
                                }
                                viewModel.validarClaveActual(usuario.cedula, claveIngresada) { esValida ->
                                    if (!esValida) {
                                        Notificador.show(requireView(), "La clave actual es incorrecta", R.color.colorError)
                                        adapter.notifyItemChanged(usuariosOriginal.indexOfFirst { it.idUsuario == usuario.idUsuario })
                                        return@validarClaveActual
                                    }
                                    continuarActualizacion()
                                }
                            } else {
                                continuarActualizacion()
                            }
                        }
                    }
                    setOnCancelListener {
                        if (usuario != null) {
                            val index = usuariosOriginal.indexOfFirst { it.idUsuario == usuario.idUsuario }
                            if (index != -1) {
                                adapter.notifyItemChanged(index)
                            }
                        }
                    }
                }

                dialogFragment.show(parentFragmentManager, "DialogFormularioUsuario")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
