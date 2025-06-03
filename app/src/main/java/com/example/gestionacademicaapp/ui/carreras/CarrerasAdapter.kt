package com.example.gestionacademicaapp.ui.carreras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class CarrerasAdapter(
    onEdit: (Carrera) -> Unit,
    onDelete: (Carrera) -> Unit,
    private val onViewCursosCarrera: (Carrera) -> Unit
) : BaseAdapter<Carrera, CarrerasAdapter.CarreraViewHolder>(DiffCallback, onEdit, onDelete) {

    inner class CarreraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val btnViewCursos: ImageButton = itemView.findViewById(R.id.btnViewCursos)

        fun bind(carrera: Carrera) {
            tvNombre.text = carrera.nombre
            val descripcion = itemView.context.getString(R.string.descripcion_carrera, carrera.codigo, carrera.titulo)
            tvDescripcion.text = descripcion
            tvNombre.contentDescription = itemView.context.getString(R.string.content_desc_nombre_carrera, carrera.nombre)
            tvDescripcion.contentDescription = itemView.context.getString(R.string.content_desc_descripcion_carrera, descripcion)
            btnViewCursos.contentDescription = itemView.context.getString(R.string.content_desc_ver_cursos, carrera.nombre)

            setupDefaultClickListener(itemView, carrera)
            btnViewCursos.setOnClickListener { onViewCursosCarrera(carrera) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarreraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrera, parent, false)
        return CarreraViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarreraViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Carrera>() {
            override fun areItemsTheSame(oldItem: Carrera, newItem: Carrera): Boolean =
                oldItem.idCarrera == newItem.idCarrera

            override fun areContentsTheSame(oldItem: Carrera, newItem: Carrera): Boolean =
                oldItem == newItem
        }
    }
}
