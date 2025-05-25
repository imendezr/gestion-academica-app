package com.example.gestionacademicaapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionacademicaapp.MainActivity
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Usuario

class LoginActivity : AppCompatActivity() {

    private fun estaInscrito(username: String): Boolean {
        return username == "admin" // Simulación
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()

            // Aquí estás creando el usuario con todos los parámetros requeridos
            val usuario = Usuario(
                id = 1, // Asignar un ID de forma predeterminada
                username = username,
                nombre = "Nombre Generado",
                correo = "$username@correo.com",  // Correo simulado
                password = "password123",  // Contraseña simulada
                tipoUsuario = "Alumno" // Tipo de usuario simulado
            )

            val intent = if (estaInscrito(username)) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, InscripcionActivity::class.java)
            }
            intent.putExtra("usuarioTO", usuario)
            startActivity(intent)
            finish()
        }
    }
}
