package com.example.gestionacademicaapp.ui.matricula

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Matricula

class MatriculaAdapter(
    private val matriculas: MutableList<Matricula>
) : RecyclerView.Adapter<MatriculaAdapter.MatriculaViewHolder>(), Filterable {

    private var matriculasFiltradas: MutableList<Matricula> = matriculas.toMutableList()

    inner class MatriculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCurso: TextView = itemView.findViewById(R.id.tvCurso)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatriculaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matricula, parent, false)
        return MatriculaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatriculaViewHolder, position: Int) {
        val matricula = matriculasFiltradas[position]
        holder.tvCurso.text = matricula.curso
        holder.tvFecha.text = matricula.fecha
    }

    override fun getItemCount(): Int = matriculasFiltradas.size

    fun agregarItem(matricula: Matricula) {
        matriculas.add(matricula)
        matriculasFiltradas.add(matricula)
        notifyItemInserted(matriculasFiltradas.size - 1)
    }

    fun eliminarItem(position: Int) {
        val matricula = matriculasFiltradas[position]
        matriculas.remove(matricula)
        matriculasFiltradas.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    matriculas.toList()
                } else {
                    matriculas.filter {
                        it.curso.lowercase().contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                matriculasFiltradas = (results?.values as? List<Matricula>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
