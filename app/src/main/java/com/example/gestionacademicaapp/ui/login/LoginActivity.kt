package com.example.gestionacademicaapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.ui.MainActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etCedula = findViewById<EditText>(R.id.etUsername)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val cedula = etCedula.text.toString().trim()

            if (cedula.isEmpty()) {
                etCedula.error = "Ingrese su cédula"
                return@setOnClickListener
            }

            // Usuario simulado (reemplazar por autenticación real)
            val usuario = Usuario(
                idUsuario = 1L,
                cedula = cedula,
                clave = "123456",
                tipo = "Alumno"
            )

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuarioTO", usuario)
            startActivity(intent)
            finish()
        }
    }
}
