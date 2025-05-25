package com.example.gestionacademicaapp.ui.profesores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Profesor

class ProfesoresAdapter(
    private val profesores: MutableList<Profesor>
) : RecyclerView.Adapter<ProfesoresAdapter.ProfesorViewHolder>(), Filterable {

    private var profesoresFiltrados: MutableList<Profesor> = profesores.toMutableList()

    inner class ProfesorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvEspecialidad: TextView = itemView.findViewById(R.id.tvEspecialidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profesor, parent, false)
        return ProfesorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfesorViewHolder, position: Int) {
        val profesor = profesoresFiltrados[position]
        holder.tvNombre.text = profesor.nombre
        holder.tvEspecialidad.text = profesor.especialidad
    }

    override fun getItemCount(): Int = profesoresFiltrados.size

    fun agregarItem(profesor: Profesor) {
        profesores.add(profesor)
        profesoresFiltrados.add(profesor)
        notifyItemInserted(profesoresFiltrados.size - 1)
    }

    fun eliminarItem(position: Int) {
        val profesor = profesoresFiltrados[position]
        profesores.remove(profesor)
        profesoresFiltrados.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    profesores.toList()
                } else {
                    profesores.filter {
                        it.nombre.lowercase().contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                profesoresFiltrados = (results?.values as? List<Profesor>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
