package com.example.gestionacademicaapp.ui.notas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.databinding.ItemNotaBinding

class NotasAdapter(
    private val onEditNota: (MatriculaAlumnoDto) -> Unit,
    private val getStudentName: (Long) -> String
) : RecyclerView.Adapter<NotasAdapter.ViewHolder>() {

    private var items: List<MatriculaAlumnoDto> = emptyList()

    fun submitList(matriculas: List<MatriculaAlumnoDto>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = matriculas.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                items[oldItemPosition].idMatricula == matriculas[newItemPosition].idMatricula
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                items[oldItemPosition] == matriculas[newItemPosition]
        })
        items = matriculas
        diffResult.dispatchUpdatesTo(this)
    }

    fun getItemAt(position: Int): MatriculaAlumnoDto? = items.getOrNull(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemNotaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(matricula: MatriculaAlumnoDto) {
            with(binding) {
                txtNombre.text = getStudentName(matricula.idMatricula)
                txtNota.text = matricula.nota.toString()
                root.setOnClickListener { onEditNota.invoke(matricula) }
            }
        }
    }
}
