package com.example.gestionacademicaapp.ui.cursos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Curso

class CursosAdapter(
    private val onEdit: (Curso, Int) -> Unit,
    private val onDelete: (Curso, Int) -> Unit
) : RecyclerView.Adapter<CursosAdapter.CursoViewHolder>(), Filterable {

    private val cursos: MutableList<Curso> = mutableListOf()
    private var cursosFiltrados: MutableList<Curso> = mutableListOf()

    inner class CursoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEdit(cursosFiltrados[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curso, parent, false)
        return CursoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        val curso = cursosFiltrados[position]
        holder.tvNombre.text = curso.nombre
        holder.tvDescripcion.text =
            "Código: ${curso.codigo} · Créditos: ${curso.creditos} · Horas: ${curso.horasSemanales}"
    }

    override fun getItemCount(): Int = cursosFiltrados.size

    fun updateCursos(newCursos: List<Curso>) {
        cursos.clear()
        cursos.addAll(newCursos)
        cursosFiltrados.clear()
        cursosFiltrados.addAll(newCursos)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    cursos.toList()
                } else {
                    cursos.filter {
                        it.nombre.lowercase().contains(filtro) ||
                                it.codigo.lowercase().contains(filtro)
                    }
                }
                return FilterResults().apply { values = resultados }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                cursosFiltrados =
                    (results?.values as? List<Curso>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    fun onSwipeDelete(position: Int) {
        val curso = cursosFiltrados[position]
        onDelete(curso, position)
    }

    fun triggerEdit(curso: Curso, position: Int) {
        onEdit(curso, position)
    }

    fun getCursoAt(position: Int): Curso {
        return cursosFiltrados[position]
    }

    fun removeItem(position: Int) {
        cursosFiltrados.removeAt(position)
        notifyItemRemoved(position)
    }
}
