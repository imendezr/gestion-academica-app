package com.example.gestionacademicaapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionacademicaapp.MainActivity
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Usuario

class InscripcionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscripcion)

        val usuario = intent.getSerializableExtra("usuarioTO") as Usuario

        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        btnGuardar.setOnClickListener {
            // Aquí también deberías actualizar o guardar el usuario en el sistema
            // Si es necesario modificar los campos en la inscripción:
            val usuarioActualizado = Usuario(
                id = usuario.id,
                username = usuario.username,
                nombre = "Nombre actualizado",
                correo = "nuevo_correo@correo.com",
                password = "nueva_contraseña",
                tipoUsuario = usuario.tipoUsuario
            )

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuarioTO", usuarioActualizado)
            startActivity(intent)
            finish()
        }
    }
}
