package com.example.gestionacademicaapp.ui.common

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.databinding.FragmentDialogFormularioBinding
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class DialogFormularioFragment : DialogFragment() {

    private var _binding: FragmentDialogFormularioBinding? = null
    private val binding get() = _binding!!
    private val inputs: MutableMap<String, Triple<View, Any?, TextView?>> = mutableMapOf() // (vista, contenedor padre, label)
    private val currentValues: MutableMap<String, String> = mutableMapOf()

    private lateinit var titulo: String
    private lateinit var campos: MutableList<CampoFormulario>
    private var datosIniciales: Map<String, String> = emptyMap()
    private lateinit var onGuardar: (Map<String, String>) -> Unit
    private var onCancel: () -> Unit = {}
    private var onDismiss: () -> Unit = {}

    companion object {
        fun newInstance(
            titulo: String,
            campos: List<CampoFormulario>,
            datosIniciales: Map<String, String> = emptyMap()
        ): DialogFormularioFragment {
            return DialogFormularioFragment().apply {
                this.titulo = titulo
                this.campos = campos.toMutableList()
                this.datosIniciales = datosIniciales
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogFormularioBinding.inflate(layoutInflater)
        val contenedor = binding.linearFormulario
        inputs.clear()
        currentValues.clear()
        datosIniciales.forEach { (key, value) -> currentValues[key] = value }

        renderCampos(contenedor)

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
                        val camposConError = mutableListOf<String>()

                        // Limpiar errores previos en todos los campos
                        campos.forEach { campo ->
                            val (view, parentLayout, label) = inputs[campo.key] ?: return@forEach
                            when {
                                parentLayout is TextInputLayout -> parentLayout.error = null
                                view is Spinner && label != null -> label.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                            }
                        }

                        // Validar cada campo
                        campos.forEach { campo ->
                            val (view, parentLayout, label) = inputs[campo.key] ?: return@forEach
                            val valor = when (view) {
                                is EditText -> view.text.toString().trim()
                                is Spinner -> campo.opciones.getOrNull(view.selectedItemPosition)?.first ?: ""
                                else -> currentValues[campo.key] ?: ""
                            }

                            val rule = campo.rules?.invoke(valor)
                            if (campo.editable && !rule.isNullOrEmpty()) {
                                esValido = false
                                tvError.text = rule
                                tvError.visibility = View.VISIBLE
                                if (parentLayout is TextInputLayout) parentLayout.error = rule
                                return@forEach
                            }

                            if (campo.obligatorio && campo.editable && valor.isEmpty()) {
                                esValido = false
                                camposConError.add(campo.label)
                                when {
                                    parentLayout is TextInputLayout -> parentLayout.error = getString(R.string.error_campo_obligatorio)
                                    view is Spinner && label != null -> label.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
                                }
                            }

                            resultado[campo.key] = valor
                        }

                        // Mostrar mensaje de error si hay campos obligatorios vacíos
                        if (camposConError.isNotEmpty()) {
                            tvError.text = getString(R.string.error_campos_obligatorios, camposConError.joinToString(", "))
                            tvError.visibility = View.VISIBLE
                        } else if (esValido) {
                            tvError.visibility = View.GONE
                            onGuardar(resultado)
                            dialog.dismiss()
                        } else {
                            tvError.visibility = View.GONE // Asegurar que si no hay campos vacíos pero hay otras reglas, se oculte
                        }
                    }
                }
                dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_window)
            }
    }

    private fun renderCampos(contenedor: LinearLayout) {
        contenedor.removeAllViews()
        inputs.clear() // Limpiar los inputs para evitar referencias obsoletas

        campos.forEach { campo ->
            val inputLayout = when (campo.tipo) {
                "spinner" -> {
                    layoutInflater.inflate(R.layout.item_spinner_field, contenedor, false).also { spinnerLayout ->
                        val spinner = spinnerLayout.findViewById<Spinner>(R.id.spinner)
                        val label = spinnerLayout.findViewById<TextView>(R.id.tvSpinnerLabel)
                        label.text = campo.label

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            campo.opciones.map { it.second }
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        spinner.adapter = adapter

                        // Cargar el valor preservado o inicial
                        val valorInicial = currentValues[campo.key] ?: datosIniciales[campo.key] ?: ""
                        val index = campo.opciones.indexOfFirst { it.first == valorInicial }
                        if (index != -1) spinner.setSelection(index, false)

                        spinner.isEnabled = campo.editable
                        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                val selectedValue = campo.opciones.getOrNull(position)?.first ?: ""
                                currentValues[campo.key] = selectedValue
                                campo.onValueChanged?.invoke(selectedValue)
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                        inputs[campo.key] = Triple(spinner, null, label)
                    }
                }
                "date" -> {
                    layoutInflater.inflate(R.layout.item_date_field, contenedor, false).also { dateLayout ->
                        val dateInput = dateLayout.findViewById<EditText>(R.id.editTextDate)
                        val textInputLayout = dateLayout.findViewById<TextInputLayout>(R.id.textInputLayoutDate)
                        textInputLayout.hint = campo.label
                        textInputLayout.isHintAnimationEnabled = false
                        textInputLayout.isHintEnabled = true

                        // Cargar el valor preservado o inicial
                        dateInput.setText(currentValues[campo.key] ?: datosIniciales[campo.key] ?: "")
                        dateInput.keyListener = null
                        dateInput.isEnabled = campo.editable
                        dateInput.isFocusable = false
                        dateInput.isFocusableInTouchMode = false

                        if (campo.editable) {
                            dateInput.setOnClickListener {
                                DatePickerDialog(
                                    requireContext(),
                                    { _, year, month, day ->
                                        val selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                        dateInput.setText(selectedDate)
                                        currentValues[campo.key] = selectedDate
                                        campo.onValueChanged?.invoke(selectedDate)
                                    },
                                    2023, 0, 1
                                ).show()
                            }
                        }
                        inputs[campo.key] = Triple(dateInput, textInputLayout, null)
                    }
                }
                else -> {
                    layoutInflater.inflate(R.layout.item_input_field, contenedor, false).also { textLayout ->
                        val inputText = textLayout.findViewById<EditText>(R.id.editText)
                        val textInputLayout = textLayout.findViewById<TextInputLayout>(R.id.textInputLayout)
                        textInputLayout.hint = campo.label
                        textInputLayout.isHintAnimationEnabled = false
                        textInputLayout.isHintEnabled = true

                        inputText.inputType = when (campo.tipo) {
                            "number" -> InputType.TYPE_CLASS_NUMBER
                            else -> InputType.TYPE_CLASS_TEXT
                        }
                        if (campo.tipo == "number") inputText.keyListener = DigitsKeyListener.getInstance("0123456789")

                        // Cargar el valor preservado o inicial
                        inputText.setText(currentValues[campo.key] ?: datosIniciales[campo.key] ?: "")
                        inputText.isEnabled = campo.editable
                        inputText.isFocusable = campo.editable
                        inputText.isFocusableInTouchMode = campo.editable

                        inputText.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(s: Editable?) {
                                val newValue = s.toString()
                                currentValues[campo.key] = newValue
                                campo.onValueChanged?.invoke(newValue)
                            }
                        })
                        inputs[campo.key] = Triple(inputText, textInputLayout, null)
                    }
                }
            }
            contenedor.addView(inputLayout)
        }
    }

    fun updateDynamicFields(newCampos: List<CampoFormulario>) {
        // Preservar los valores actuales de los campos existentes
        campos.forEach { campo ->
            val (view, _, _) = inputs[campo.key] ?: return@forEach
            val valor = when (view) {
                is EditText -> view.text.toString().trim()
                is Spinner -> campo.opciones.getOrNull(view.selectedItemPosition)?.first ?: ""
                else -> currentValues[campo.key] ?: ""
            }
            currentValues[campo.key] = valor
        }

        // Actualizar la lista de campos
        this.campos = newCampos.toMutableList()

        // Volver a renderizar el formulario con los valores preservados
        renderCampos(binding.linearFormulario)
    }

    fun setOnGuardarListener(listener: (Map<String, String>) -> Unit) {
        onGuardar = listener
    }

    fun setOnCancelListener(listener: () -> Unit) {
        onCancel = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismiss = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
