package com.example.gestionacademicaapp.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.databinding.ActivityLoginBinding
import com.example.gestionacademicaapp.ui.MainActivity
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.etUsername.error = getString(R.string.ingrese_cedula)
                    Notificador.show(
                        view = binding.root,
                        mensaje = getString(R.string.ingrese_cedula),
                        colorResId = R.color.colorError,
                        anchorView = null,
                        duracion = 2000
                    )
                }

                password.isEmpty() -> {
                    binding.etPassword.error = getString(R.string.ingrese_contrasena)
                    Notificador.show(
                        view = binding.root,
                        mensaje = getString(R.string.ingrese_contrasena),
                        colorResId = R.color.colorError,
                        anchorView = null,
                        duracion = 2000
                    )
                }

                else -> viewModel.login(email, password)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    updateUiState(state)
                }
            }
        }
    }

    private fun updateUiState(state: UiState<Usuario>) {
        binding.progressBar.isVisible = state is UiState.Loading
        binding.btnLogin.isEnabled = state !is UiState.Loading

        when (state) {
            is UiState.Success -> {
                state.data?.let { onLoginSuccess(it) }
            }

            is UiState.Error -> {
                val message = when (state.type) {
                    ErrorType.VALIDATION -> state.message
                    ErrorType.DEPENDENCY -> getString(R.string.error_dependencia_login)
                    ErrorType.GENERAL -> state.message
                }
                Notificador.show(
                    view = binding.root,
                    mensaje = message,
                    colorResId = R.color.colorError,
                    anchorView = null,
                    duracion = 2000
                )
            }

            else -> {} // Loading or Empty
        }
    }

    private fun onLoginSuccess(usuario: Usuario) {
        SessionManager.setUsuario(this, usuario)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (SessionManager.isLoggedIn(this)) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
