package com.example.gestionacademicaapp.ui.profesores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class ProfesoresAdapter(
    private val onEditListener: (Profesor, Int) -> Unit
) : BaseAdapter<Profesor, ProfesoresAdapter.ProfesorViewHolder>(
    diffCallback = ProfesorDiffCallback,
    onEdit = { /* Handled in bind or triggerEdit */ },
    onDelete = {} // No-op: Delete not exposed in UI
) {

    inner class ProfesorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvCedula: TextView = itemView.findViewById(R.id.tvCedula)

        fun bind(profesor: Profesor) {
            tvNombre.text = profesor.nombre
            tvCedula.text = itemView.context.getString(
                R.string.profesor_details,
                profesor.cedula,
                profesor.telefono
            )
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditListener(profesor, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profesor, parent, false)
        return ProfesorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfesorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun triggerEdit(profesor: Profesor, position: Int) {
        onEditListener(profesor, position)
    }

    companion object {
        private val ProfesorDiffCallback = object : DiffUtil.ItemCallback<Profesor>() {
            override fun areItemsTheSame(oldItem: Profesor, newItem: Profesor): Boolean {
                return oldItem.idProfesor == newItem.idProfesor
            }

            override fun areContentsTheSame(oldItem: Profesor, newItem: Profesor): Boolean {
                return oldItem == newItem
            }
        }
    }
}
