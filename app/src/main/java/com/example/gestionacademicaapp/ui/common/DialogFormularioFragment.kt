package com.example.gestionacademicaapp.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.gestionacademicaapp.databinding.FragmentDialogFormularioBinding
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.R

class DialogFormularioFragment(
    private val titulo: String,
    private val campos: List<CampoFormulario>,
    private val datosIniciales: Map<String, String> = emptyMap(),
    private val onGuardar: (Map<String, String>) -> Unit
) : DialogFragment() {

    private var _binding: FragmentDialogFormularioBinding? = null
    private val binding get() = _binding!!
    private val inputs: MutableMap<String, EditText> = mutableMapOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogFormularioBinding.inflate(LayoutInflater.from(context))
        val contenedor = binding.linearFormulario

        // Generar campos dinámicamente
        campos.forEach { campo ->
            val inputLayout = layoutInflater.inflate(R.layout.item_input_field, contenedor, false)
            val inputText = inputLayout.findViewById<EditText>(R.id.editText)
            inputLayout.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.textInputLayout).hint = campo.label

            inputText.inputType = when (campo.tipo) {
                "number" -> android.text.InputType.TYPE_CLASS_NUMBER
                else -> android.text.InputType.TYPE_CLASS_TEXT
            }
            inputText.setText(datosIniciales[campo.key] ?: "")

            contenedor.addView(inputLayout)
            inputs[campo.key] = inputText
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setView(binding.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val resultado = mutableMapOf<String, String>()
                        var esValido = true

                        campos.forEach { campo ->
                            val valor = inputs[campo.key]?.text.toString().trim()
                            if (campo.obligatorio && valor.isEmpty()) {
                                esValido = false
                            }
                            resultado[campo.key] = valor
                        }

                        if (esValido) {
                            onGuardar(resultado)
                            dialog.dismiss()
                        } else {
                            Notificador.show(requireView(), "Completa los campos obligatorios", R.color.colorError)
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
