package com.example.gestionacademicaapp.ui.alumnos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class AlumnosAdapter(
    private val onEditListener: (Alumno, Int) -> Unit,
    private val onViewHistorialListener: (Alumno, Int) -> Unit
) : BaseAdapter<Alumno, AlumnosAdapter.AlumnoViewHolder>(
    diffCallback = AlumnoDiffCallback,
    onEdit = { alumno -> onEditListener(alumno, 0) },
    onDelete = {}
) {

    inner class AlumnoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvCedula: TextView = itemView.findViewById(R.id.tvCedula)
        private val btnViewHistorial: ImageButton = itemView.findViewById(R.id.btnViewHistorial)

        fun bind(alumno: Alumno) {
            tvNombre.text = alumno.nombre
            tvCedula.text = itemView.context.getString(
                R.string.alumno_details,
                alumno.cedula,
                alumno.telefono
            )
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditListener(alumno, position)
                }
            }
            btnViewHistorial.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onViewHistorialListener(alumno, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alumno, parent, false)
        return AlumnoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun triggerEdit(alumno: Alumno, position: Int) {
        onEditListener(alumno, position)
    }

    companion object {
        private val AlumnoDiffCallback = object : DiffUtil.ItemCallback<Alumno>() {
            override fun areItemsTheSame(oldItem: Alumno, newItem: Alumno): Boolean {
                return oldItem.idAlumno == newItem.idAlumno
            }

            override fun areContentsTheSame(oldItem: Alumno, newItem: Alumno): Boolean {
                return oldItem == newItem
            }
        }
    }
}
