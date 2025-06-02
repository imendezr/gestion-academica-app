package com.example.gestionacademicaapp.ui.carreras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.ui.carreras.model.CarreraCursoUI
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class CarreraCursosAdapter(
    onEdit: (CarreraCursoUI) -> Unit,
    onDelete: (CarreraCursoUI) -> Unit,
    private val onReorderCarreraCurso: (CarreraCursoUI) -> Unit
) : BaseAdapter<CarreraCursoUI, CarreraCursosAdapter.CarreraCursoViewHolder>(DiffCallback, onEdit, onDelete) {

    inner class CarreraCursoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val btnReorder: ImageButton = itemView.findViewById(R.id.btnReorder)

        fun bind(carreraCurso: CarreraCursoUI) {
            tvNombre.text = carreraCurso.curso.nombre
            val cicloText = carreraCurso.ciclo?.let { "${it.anio} - ${it.numero}" }
                ?: "Ciclo: ${carreraCurso.cicloId}"
            tvDescripcion.text = itemView.context.getString(R.string.label_ciclo, cicloText)

            btnReorder.setOnClickListener { onReorderCarreraCurso(carreraCurso) }
            btnReorder.contentDescription = itemView.context.getString(R.string.desc_reordenar_curso)

            setupDefaultClickListener(itemView, carreraCurso)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarreraCursoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrera_curso, parent, false)
        return CarreraCursoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarreraCursoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CarreraCursoUI>() {
            override fun areItemsTheSame(oldItem: CarreraCursoUI, newItem: CarreraCursoUI): Boolean =
                oldItem.idCarreraCurso == newItem.idCarreraCurso

            override fun areContentsTheSame(oldItem: CarreraCursoUI, newItem: CarreraCursoUI): Boolean =
                oldItem == newItem
        }
    }
}
