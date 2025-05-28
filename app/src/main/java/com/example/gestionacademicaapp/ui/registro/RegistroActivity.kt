package com.example.gestionacademicaapp.ui.registro

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.ui.MainActivity

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val etCedula = findViewById<EditText>(R.id.etCedula)
        val etClave = findViewById<EditText>(R.id.etClave)
        val etRol = findViewById<AutoCompleteTextView>(R.id.etRol)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        val roles = listOf("Administrador", "Matriculador", "Profesor", "Alumno")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roles)
        etRol.setAdapter(adapter)

        btnRegistrar.setOnClickListener {
            val cedula = etCedula.text.toString().trim()
            val clave = etClave.text.toString().trim()
            val tipo = etRol.text.toString().trim()

            if (cedula.isEmpty() || clave.isEmpty() || tipo.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoUsuario = Usuario(
                idUsuario = 0L, // Simulado. Reemplazar por ID real desde backend en integración
                cedula = cedula,
                clave = clave,
                tipo = tipo
            )

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuarioTO", nuevoUsuario)
            startActivity(intent)
            finish()
        }
    }
}
