package com.example.gestionacademicaapp.ui.cursos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Curso

class CursosAdapter(
    private val cursos: MutableList<Curso>
) : RecyclerView.Adapter<CursosAdapter.CursoViewHolder>(), Filterable {

    private var cursosFiltrados: MutableList<Curso> = cursos.toMutableList()

    inner class CursoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
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

    fun agregarItem(curso: Curso) {
        cursos.add(curso)
        cursosFiltrados.add(curso)
        notifyItemInserted(cursosFiltrados.size - 1)
    }

    fun actualizarItem(position: Int, cursoActualizado: Curso) {
        val cursoOriginal = cursosFiltrados[position]
        val indexEnListaPrincipal = cursos.indexOfFirst { it.idCurso == cursoOriginal.idCurso }
        if (indexEnListaPrincipal != -1) {
            cursos[indexEnListaPrincipal] = cursoActualizado
        }
        cursosFiltrados[position] = cursoActualizado
        notifyItemChanged(position)
    }

    fun eliminarItem(position: Int) {
        val curso = cursosFiltrados[position]
        cursos.remove(curso)
        cursosFiltrados.removeAt(position)
        notifyItemRemoved(position)
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
                cursosFiltrados = (results?.values as? List<Curso>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
