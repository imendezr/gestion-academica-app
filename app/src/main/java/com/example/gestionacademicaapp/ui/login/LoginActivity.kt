package com.example.gestionacademicaapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.ui.MainActivity
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var etCedula: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar vistas
        etCedula = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        // Observar estados del ViewModel
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is SingleUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnLogin.isEnabled = false
                }

                is SingleUiState.Success -> {
                    progressBar.visibility = View.INVISIBLE
                    btnLogin.isEnabled = true
                    onLoginSuccess(state.data)
                }

                is SingleUiState.Error -> {
                    progressBar.visibility = View.INVISIBLE
                    btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Configurar clic del botón
        btnLogin.setOnClickListener {
            val cedula = etCedula.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (cedula.isEmpty()) {
                etCedula.error = getString(R.string.ingrese_cedula)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = getString(R.string.ingrese_contrasena)
                return@setOnClickListener
            }

            viewModel.login(cedula, password)
        }
    }

    private fun onLoginSuccess(usuario: Usuario) {
        SessionManager.setUsuario(this, usuario)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
