package com.example.gestionacademicaapp.ui.cursos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class CursosAdapter(
    onEdit: (Curso) -> Unit,
    onDelete: (Curso) -> Unit
) : BaseAdapter<Curso, CursosAdapter.CursoViewHolder>(DiffCallback, onEdit, onDelete) {

    inner class CursoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)

        fun bind(curso: Curso) {
            tvNombre.text = curso.nombre
            tvDescripcion.text = itemView.context.getString(
                R.string.label_curso_detalle,
                curso.codigo,
                curso.creditos,
                curso.horasSemanales
            )
            setupDefaultClickListener(itemView, curso)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curso, parent, false)
        return CursoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Curso>() {
            override fun areItemsTheSame(oldItem: Curso, newItem: Curso): Boolean =
                oldItem.idCurso == newItem.idCurso

            override fun areContentsTheSame(oldItem: Curso, newItem: Curso): Boolean =
                oldItem == newItem
        }
    }
}
