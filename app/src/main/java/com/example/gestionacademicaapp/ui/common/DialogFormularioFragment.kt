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
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Locale

class DialogFormularioFragment : DialogFragment() {

    private var _binding: FragmentDialogFormularioBinding? = null
    private val binding get() = _binding!!
    private val inputs: MutableMap<String, Triple<View, Any?, TextView?>> = mutableMapOf()
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
            .setNegativeButton(getString(R.string.cancelar), null) // Sin lógica directa
            .create().also { dialog ->
                dialog.setOnShowListener {
                    // Botón Guardar
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val resultado = validateAndGetValues()
                        if (resultado.isValid) {
                            tvError.visibility = View.GONE
                            onGuardar(resultado.values)
                            dialog.dismiss()
                        } else {
                            tvError.text = resultado.errorMessage
                            tvError.visibility = View.VISIBLE
                        }
                    }
                    // Botón Cancelar
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                        onCancel()
                        dialog.dismiss()
                    }
                }
                dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_window)
            }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCancel()
        dismiss() // Asegura que el diálogo se cierre y dispare onDismiss
    }

    private fun validateAndGetValues(): ValidationResult {
        val resultado = mutableMapOf<String, String>()
        var esValido = true
        val erroresPorCampo = mutableMapOf<String, String>()

        campos.forEach { campo ->
            val (view, parentLayout, label) = inputs[campo.key] ?: return@forEach
            // Limpiar errores previos
            when {
                parentLayout is TextInputLayout -> parentLayout.error = null
                view is Spinner && label != null -> label.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
                )
            }

            val valor = when (view) {
                is EditText -> view.text.toString().trim()
                is Spinner -> campo.opciones.getOrNull(view.selectedItemPosition)?.first ?: ""
                else -> currentValues[campo.key] ?: ""
            }

            if (campo.editable) {
                // Validar campo obligatorio primero
                if (campo.obligatorio && valor.isEmpty()) {
                    esValido = false
                    val errorMsg = campo.obligatorioError ?: getString(R.string.error_campo_obligatorio)
                    erroresPorCampo[campo.key] = errorMsg
                    when {
                        parentLayout is TextInputLayout -> parentLayout.error = errorMsg
                        view is Spinner && label != null -> label.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorError)
                        )
                    }
                    return@forEach
                }

                // Validar reglas personalizadas si el campo no está vacío
                if (valor.isNotEmpty()) {
                    val ruleError = campo.rules?.invoke(valor, currentValues)
                    if (!ruleError.isNullOrEmpty()) {
                        esValido = false
                        erroresPorCampo[campo.key] = ruleError
                        if (parentLayout is TextInputLayout) parentLayout.error = ruleError
                        else if (view is Spinner && label != null) label.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorError)
                        )
                        return@forEach
                    }
                }
            }

            resultado[campo.key] = valor
        }

        // Construir mensaje de error general
        val errorMessage = if (erroresPorCampo.isNotEmpty()) {
            erroresPorCampo.values.joinToString(", ")
        } else {
            null
        }

        return ValidationResult(esValido, resultado, errorMessage)
    }

    private fun renderCampos(contenedor: LinearLayout) {
        contenedor.removeAllViews()
        inputs.clear()

        campos.forEach { campo ->
            val inputLayout = when (campo.tipo) {
                CampoTipo.SPINNER -> renderSpinnerField(campo, contenedor)
                CampoTipo.DATE -> renderDateField(campo, contenedor)
                else -> renderTextField(campo, contenedor)
            }
            contenedor.addView(inputLayout)
        }
    }

    private fun renderSpinnerField(campo: CampoFormulario, contenedor: LinearLayout): View {
        return layoutInflater.inflate(R.layout.item_spinner_field, contenedor, false).also { spinnerLayout ->
            val spinner = spinnerLayout.findViewById<Spinner>(R.id.spinner)
            val label = spinnerLayout.findViewById<TextView>(R.id.tvSpinnerLabel)
            label.text = campo.label
            label.contentDescription = "${campo.label} spinner"

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                campo.opciones.map { it.second }
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spinner.adapter = adapter

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

    private fun renderDateField(campo: CampoFormulario, contenedor: LinearLayout): View {
        return layoutInflater.inflate(R.layout.item_date_field, contenedor, false).also { dateLayout ->
            val dateInput = dateLayout.findViewById<EditText>(R.id.editTextDate)
            val textInputLayout = dateLayout.findViewById<TextInputLayout>(R.id.textInputLayoutDate)
            textInputLayout.hint = campo.label
            textInputLayout.isHintAnimationEnabled = false
            textInputLayout.isHintEnabled = true
            dateInput.contentDescription = "${campo.label} date input"

            dateInput.setText(currentValues[campo.key] ?: datosIniciales[campo.key] ?: "")
            dateInput.keyListener = null
            dateInput.isEnabled = campo.editable
            dateInput.isFocusable = false
            dateInput.isFocusableInTouchMode = false

            if (campo.editable) {
                dateInput.setOnClickListener {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                    val initialDate = currentValues[campo.key]?.let { dateStr ->
                        try {
                            LocalDate.parse(dateStr, formatter)
                        } catch (e: DateTimeParseException) {
                            LocalDate.now()
                        }
                    } ?: LocalDate.now()

                    DatePickerDialog(
                        requireContext(),
                        { _, year, month, day ->
                            val selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                            dateInput.setText(selectedDate)
                            currentValues[campo.key] = selectedDate
                            campo.onValueChanged?.invoke(selectedDate)
                        },
                        initialDate.year,
                        initialDate.monthValue - 1,
                        initialDate.dayOfMonth
                    ).show()
                }
            }
            inputs[campo.key] = Triple(dateInput, textInputLayout, null)
        }
    }

    private fun renderTextField(campo: CampoFormulario, contenedor: LinearLayout): View {
        return layoutInflater.inflate(R.layout.item_input_field, contenedor, false).also { textLayout ->
            val inputText = textLayout.findViewById<EditText>(R.id.editText)
            val textInputLayout = textLayout.findViewById<TextInputLayout>(R.id.textInputLayout)
            textInputLayout.hint = campo.label
            textInputLayout.isHintAnimationEnabled = false
            textInputLayout.isHintEnabled = true
            inputText.contentDescription = "${campo.label} text input"

            inputText.inputType = when (campo.tipo) {
                CampoTipo.NUMBER -> InputType.TYPE_CLASS_NUMBER
                else -> InputType.TYPE_CLASS_TEXT
            }
            if (campo.tipo == CampoTipo.NUMBER) {
                inputText.keyListener = DigitsKeyListener.getInstance("0123456789")
            }

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

    fun updateDynamicFields(newCampos: List<CampoFormulario>) {
        campos.forEach { campo ->
            val (view, _, _) = inputs[campo.key] ?: return@forEach
            val valor = when (view) {
                is EditText -> view.text.toString().trim()
                is Spinner -> campo.opciones.getOrNull(view.selectedItemPosition)?.first ?: ""
                else -> currentValues[campo.key] ?: ""
            }
            currentValues[campo.key] = valor
        }

        this.campos = newCampos.toMutableList()
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

    private data class ValidationResult(
        val isValid: Boolean,
        val values: Map<String, String>,
        val errorMessage: String?
    )
}
