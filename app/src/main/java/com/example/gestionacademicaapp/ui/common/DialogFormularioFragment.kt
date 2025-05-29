package com.example.gestionacademicaapp.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.databinding.FragmentDialogFormularioBinding
import com.google.android.material.textfield.TextInputLayout

class DialogFormularioFragment(
    private val titulo: String,
    private val campos: List<CampoFormulario>,
    private val datosIniciales: Map<String, String> = emptyMap(),
    private val onGuardar: (Map<String, String>) -> Unit,
    private val onCancel: () -> Unit = {}
) : DialogFragment() {

    private var _binding: FragmentDialogFormularioBinding? = null
    private val binding get() = _binding!!
    private val inputs: MutableMap<String, View> = mutableMapOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogFormularioBinding.inflate(LayoutInflater.from(context))
        val contenedor = binding.linearFormulario
        inputs.clear()

        campos.forEach { campo ->
            val inputLayout = when (campo.tipo) {
                "spinner" -> {
                    val spinnerLayout =
                        layoutInflater.inflate(R.layout.item_spinner_field, contenedor, false)
                    val spinner = spinnerLayout.findViewById<Spinner>(R.id.spinner)
                    val label = spinnerLayout.findViewById<TextView>(R.id.tvSpinnerLabel)
                    label.text = campo.label

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        campo.opciones.map { it.second }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    val valorInicial = datosIniciales[campo.key]
                    val index = campo.opciones.indexOfFirst { it.first == valorInicial }
                    if (index != -1) spinner.setSelection(index)

                    inputs[campo.key] = spinner
                    spinnerLayout
                }

                else -> {
                    val textLayout =
                        layoutInflater.inflate(R.layout.item_input_field, contenedor, false)
                    val inputText = textLayout.findViewById<EditText>(R.id.editText)
                    textLayout.findViewById<TextInputLayout>(R.id.textInputLayout).hint =
                        campo.label

                    inputText.inputType = when (campo.tipo) {
                        "number" -> android.text.InputType.TYPE_CLASS_NUMBER
                        else -> android.text.InputType.TYPE_CLASS_TEXT
                    }
                    inputText.setText(datosIniciales[campo.key] ?: "")
                    inputText.isEnabled = campo.editable

                    inputs[campo.key] = inputText
                    textLayout
                }
            }

            contenedor.addView(inputLayout)
        }

        val tvError = binding.tvErrorFormulario

        return AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setView(binding.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar") { dialog, _ ->
                onCancel()
                dialog.dismiss()
            }
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val resultado = mutableMapOf<String, String>()
                        var esValido = true

                        campos.forEach { campo ->
                            val view = inputs[campo.key]
                            val valor = when (view) {
                                is EditText -> view.text.toString().trim()
                                is Spinner -> {
                                    val pos = view.selectedItemPosition
                                    campo.opciones.getOrNull(pos)?.first ?: ""
                                }

                                else -> ""
                            }

                            if (campo.obligatorio && valor.isEmpty()) {
                                esValido = false
                            }
                            resultado[campo.key] = valor
                        }

                        if (esValido) {
                            tvError.visibility = View.GONE
                            onGuardar(resultado)
                            dialog.dismiss()
                        } else {
                            tvError.text = "Completa los campos obligatorios"
                            tvError.visibility = View.VISIBLE
                        }
                    }
                }
                dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_window)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
