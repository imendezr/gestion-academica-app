package com.example.gestionacademicaapp.ui.common

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
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
import java.util.Locale

class DialogFormularioFragment : DialogFragment() {

    private var _binding: FragmentDialogFormularioBinding? = null
    private val binding get() = _binding!!
    private val inputs: MutableMap<String, View> = mutableMapOf()

    private lateinit var titulo: String
    private lateinit var campos: List<CampoFormulario>
    private var datosIniciales: Map<String, String> = emptyMap()
    private lateinit var onGuardar: (Map<String, String>) -> Unit
    private var onCancel: () -> Unit = {}

    companion object {
        fun newInstance(
            titulo: String,
            campos: List<CampoFormulario>,
            datosIniciales: Map<String, String> = emptyMap()
        ): DialogFormularioFragment {
            return DialogFormularioFragment().apply {
                this.titulo = titulo
                this.campos = campos
                this.datosIniciales = datosIniciales
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogFormularioBinding.inflate(layoutInflater)
        val contenedor = binding.linearFormulario
        inputs.clear()

        campos.forEach { campo ->
            val inputLayout = when (campo.tipo) {
                "spinner" -> {
                    layoutInflater.inflate(R.layout.item_spinner_field, contenedor, false)
                        .also { spinnerLayout ->
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

                            spinner.isEnabled = campo.editable
                            spinner.isClickable = campo.editable

                            inputs[campo.key] = spinner
                        }
                }

                "date" -> {
                    layoutInflater.inflate(R.layout.item_date_field, contenedor, false)
                        .also { dateLayout ->
                            val dateInput = dateLayout.findViewById<EditText>(R.id.editTextDate)
                            val label =
                                dateLayout.findViewById<TextInputLayout>(R.id.textInputLayoutDate)
                            label.hint = campo.label

                            dateInput.setText(datosIniciales[campo.key] ?: "")
                            dateInput.inputType = InputType.TYPE_NULL
                            dateInput.keyListener = null
                            dateInput.isFocusable = false
                            dateInput.isFocusableInTouchMode = false
                            dateInput.isCursorVisible = false
                            dateInput.isEnabled = campo.editable

                            if (campo.editable) {
                                dateInput.setOnClickListener {
                                    val datePicker = DatePickerDialog(
                                        requireContext(), { _, year, month, day ->
                                            val selectedDate = String.format(
                                                Locale.US,
                                                "%04d-%02d-%02d",
                                                year,
                                                month + 1,
                                                day
                                            )
                                            dateInput.setText(selectedDate)
                                        }, 2023, 0, 1
                                    )
                                    datePicker.show()
                                }
                            }

                            inputs[campo.key] = dateInput
                        }
                }

                else -> {
                    layoutInflater.inflate(R.layout.item_input_field, contenedor, false)
                        .also { textLayout ->
                            val inputText = textLayout.findViewById<EditText>(R.id.editText)
                            textLayout.findViewById<TextInputLayout>(R.id.textInputLayout).hint =
                                campo.label

                            inputText.inputType = when (campo.tipo) {
                                "number" -> InputType.TYPE_CLASS_NUMBER
                                else -> InputType.TYPE_CLASS_TEXT
                            }

                            if (campo.tipo == "number") {
                                inputText.keyListener = DigitsKeyListener.getInstance("0123456789")
                            }

                            inputText.setText(datosIniciales[campo.key] ?: "")
                            inputText.isEnabled = campo.editable
                            inputText.isFocusable = campo.editable
                            inputText.isFocusableInTouchMode = campo.editable

                            inputs[campo.key] = inputText
                        }
                }
            }

            contenedor.addView(inputLayout)
        }

        val tvError = binding.tvErrorFormulario

        return AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setView(binding.root)
            .setPositiveButton(getString(R.string.guardar), null)
            .setNegativeButton(getString(R.string.cancelar)) { dialog, _ ->
                onCancel()
                dialog.dismiss()
            }
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val resultado = mutableMapOf<String, String>()
                        var esValido = true

                        campos.forEach { campo ->
                            val valor = when (val view = inputs[campo.key]) {
                                is EditText -> view.text.toString().trim()
                                is Spinner -> {
                                    val pos = view.selectedItemPosition
                                    campo.opciones.getOrNull(pos)?.first ?: ""
                                }

                                else -> ""
                            }

                            val rule = campo.rules?.invoke(valor)
                            if (campo.editable && !rule.isNullOrEmpty()) {
                                esValido = false
                                tvError.text = rule
                                tvError.visibility = View.VISIBLE
                                return@forEach
                            }

                            if (campo.obligatorio && campo.editable && valor.isEmpty()) {
                                esValido = false
                                tvError.text = getString(R.string.error_campos_obligatorios)
                                tvError.visibility = View.VISIBLE
                                return@forEach
                            }

                            resultado[campo.key] = valor
                        }

                        if (esValido) {
                            tvError.visibility = View.GONE
                            onGuardar(resultado)
                            dialog.dismiss()
                        }
                    }
                }
                dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_window)
            }
    }

    fun setOnGuardarListener(listener: (Map<String, String>) -> Unit) {
        onGuardar = listener
    }

    fun setOnCancelListener(listener: () -> Unit) {
        onCancel = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
