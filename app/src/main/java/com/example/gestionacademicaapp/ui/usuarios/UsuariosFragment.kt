package com.example.gestionacademicaapp.ui.usuarios

import android.content.Intent
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
import com.example.gestionacademicaapp.ui.MainActivity
import com.example.gestionacademicaapp.ui.common.CampoFormulario
import com.example.gestionacademicaapp.ui.common.DialogFormularioFragment
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.SessionManager
import com.example.gestionacademicaapp.utils.enableSwipeActions
import com.example.gestionacademicaapp.utils.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsuariosFragment : Fragment() {

    private val viewModel: UsuariosViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: UsuariosAdapter
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var progressBar: View

    private var usuariosOriginal: List<Usuario> = emptyList()
    private var currentSearchQuery: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_usuarios, container, false)

        viewModel.idUsuarioActual = SessionManager
            .getUserId(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewUsuarios)
        searchView = view.findViewById(R.id.searchViewUsuarios)
        fab = view.findViewById(R.id.fabUsuarios)
        progressBar = view.findViewById(R.id.progressBar)

        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = getString(R.string.search_hint_codigo_nombre)

        adapter = UsuariosAdapter(
            idUsuarioActual = viewModel.idUsuarioActual,
            onEdit = { usuario -> mostrarDialogoUsuario(usuario) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText
                filtrarLista(newText)
                return true
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && fab.isExtended) fab.shrink()
                else if (dy < -10 && !fab.isExtended) fab.extend()
            }
        })

        fab.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            mostrarDialogoUsuario(null)
        }

        recyclerView.enableSwipeActions(
            onSwipeLeft = { position ->
                val usuario = adapter.getUsuarioAt(position)
                viewModel.deleteUsuario(usuario.idUsuario)

                recyclerView.postDelayed({
                    if (!recyclerView.isComputingLayout) {
                        adapter.notifyItemChanged(position)
                    }
                }, 300)
            },
            onSwipeRight = { position ->
                mostrarDialogoUsuario(adapter.getUsuarioAt(position))
            }
        )

        viewModel.usuariosState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ListUiState.Loading -> {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                }

                is ListUiState.Success -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = true
                    usuariosOriginal = state.data
                    filtrarLista(currentSearchQuery)
                }

                is ListUiState.Error -> {
                    progressBar.isVisible = false
                    recyclerView.isVisible = false
                    Notificador.show(view, state.message, R.color.colorError)
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SingleUiState.Loading -> {
                    progressBar.isVisible = true
                    fab.isEnabled = false
                }

                is SingleUiState.Success -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    val color = when {
                        state.data.contains("creado", true) || state.data.contains(
                            "actualizado",
                            true
                        ) -> R.color.colorAccent

                        state.data.contains("eliminado", true) -> R.color.colorError
                        else -> R.color.colorPrimary
                    }
                    Notificador.show(view, state.data, color, anchorView = fab)
                }

                is SingleUiState.Error -> {
                    progressBar.isVisible = false
                    fab.isEnabled = true
                    Notificador.show(view, state.message, R.color.colorError)
                }
            }
        }

        return view
    }

    private fun filtrarLista(query: String?) {
        val texto = query?.lowercase()?.trim().orEmpty()
        val filtrados = if (texto.isEmpty()) {
            usuariosOriginal
        } else {
            usuariosOriginal.filter { it.cedula.lowercase().contains(texto) }
        }
        adapter.submitList(filtrados)
    }

    private fun mostrarDialogoUsuario(usuario: Usuario?) {
        val index = usuario?.let {
            usuariosOriginal.indexOfFirst { u -> u.idUsuario == it.idUsuario }
        } ?: -1

        val esUsuarioActual = usuario?.idUsuario == viewModel.idUsuarioActual

        val campos = mutableListOf(
            CampoFormulario("cedula", "Cédula", "text", obligatorio = true, editable = true),
            CampoFormulario(
                "tipo", "Tipo", "spinner", obligatorio = true,
                opciones = listOf(
                    "Alumno" to "Alumno",
                    "Profesor" to "Profesor",
                    "Matriculador" to "Matriculador",
                    "Administrador" to "Administrador"
                )
            )
        )

        when {
            usuario == null -> {
                campos.add(1, CampoFormulario("clave", "Clave", "text", obligatorio = true))
            }

            esUsuarioActual -> {
                campos.add(1, CampoFormulario("clave", "Clave actual", "text", obligatorio = true))
            }

            else -> {
                campos.add(
                    1,
                    CampoFormulario("clave", "Nueva Clave (opcional)", "text", obligatorio = false)
                )
            }
        }

        val datosIniciales = usuario?.let {
            mapOf("cedula" to it.cedula, "clave" to "", "tipo" to it.tipo)
        } ?: emptyMap()

        val dialog = DialogFormularioFragment.newInstance(
            titulo = if (usuario == null) "Nuevo Usuario" else "Editar Usuario",
            campos = campos,
            datosIniciales = datosIniciales
        )

        dialog.setOnGuardarListener { datosMap ->
            val cedulaNueva = datosMap["cedula"].orEmpty()
            val claveIngresada = datosMap["clave"]?.trim().orEmpty()
            val tipoNuevo = datosMap["tipo"].orEmpty()

            if (usuario == null) {
                if (claveIngresada.isBlank()) {
                    Notificador.show(
                        requireView(),
                        "La clave no puede estar vacía",
                        R.color.colorError
                    )
                    return@setOnGuardarListener
                }
                viewModel.createUsuario(Usuario(0, cedulaNueva, claveIngresada, tipoNuevo))
                return@setOnGuardarListener
            }

            fun continuarActualizacion() {
                val hayCambios = usuario.cedula != cedulaNueva ||
                        usuario.tipo != tipoNuevo ||
                        (claveIngresada.isNotBlank() && !esUsuarioActual)

                if (!hayCambios) {
                    Notificador.show(requireView(), "No se realizaron cambios", R.color.colorAccent)
                    adapter.notifyItemChanged(index)
                    return
                }

                val claveFinal = if (claveIngresada.isNotBlank() && !esUsuarioActual) {
                    claveIngresada
                } else {
                    usuario.clave ?: ""
                }

                val usuarioModificado = usuario.copy(
                    cedula = cedulaNueva,
                    clave = claveFinal,
                    tipo = tipoNuevo
                )

                viewModel.updateUsuario(usuarioModificado)

                if (esUsuarioActual) {
                    val actualizado = SessionManager.getUsuario(requireContext())?.copy(
                        cedula = cedulaNueva,
                        tipo = tipoNuevo
                    ) ?: usuarioModificado

                    SessionManager.setUsuario(requireContext(), actualizado)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            if (esUsuarioActual) {
                if (claveIngresada.isBlank()) {
                    Notificador.show(
                        requireView(),
                        "Debe ingresar su clave actual",
                        R.color.colorError
                    )
                    adapter.notifyItemChanged(index)
                    return@setOnGuardarListener
                }

                val cedulaOriginal = usuario.cedula
                viewModel.validarClaveActual(cedulaOriginal, claveIngresada) { esValida ->
                    if (!esValida) {
                        Notificador.show(
                            requireView(),
                            "La clave actual es incorrecta",
                            R.color.colorError
                        )
                        adapter.notifyItemChanged(index)
                        return@validarClaveActual
                    }
                    continuarActualizacion()
                }
            } else {
                continuarActualizacion()
            }
        }

        dialog.setOnCancelListener {
            if (index != -1) adapter.notifyItemChanged(index)
        }

        dialog.show(parentFragmentManager, "DialogFormularioUsuario")
    }
}
