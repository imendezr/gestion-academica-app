package com.example.gestionacademicaapp.ui.profesores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.Profesor

class ProfesoresAdapter(
    private val onEdit: (Profesor, Int) -> Unit,
    private val onDelete: (Profesor, Int) -> Unit
) : RecyclerView.Adapter<ProfesoresAdapter.ProfesorViewHolder>(), Filterable {
    private val profesores: MutableList<Profesor> = mutableListOf()

    private var profesoresFiltrados: MutableList<Profesor> = mutableListOf()

    inner class ProfesorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCedula: TextView = itemView.findViewById(R.id.tvCedula)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEdit(profesoresFiltrados[position], position)
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
        val profesor = profesoresFiltrados[position]
        holder.tvNombre.text = profesor.nombre
        holder.tvCedula.text = "Cédula: ${profesor.cedula} · Tel: ${profesor.telefono}"
    }

    override fun getItemCount(): Int = profesoresFiltrados.size

    fun updateProfesores(newProfesores: List<Profesor>) {
        profesores.clear()
        profesores.addAll(newProfesores)
        // Restaurar la lista filtrada al actualizar
        profesoresFiltrados.clear()
        profesoresFiltrados.addAll(newProfesores)
        notifyDataSetChanged()
    }

    fun restoreFilteredList() {
        // Restaurar la lista filtrada a la lista completa
        profesoresFiltrados.clear()
        profesoresFiltrados.addAll(profesores)
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    profesores.toList()
                } else {
                    profesores.filter {
                        it.nombre.lowercase().contains(filtro) ||
                                it.cedula.lowercase().contains(filtro)
                    }
                }
                return FilterResults().apply { values = resultados }
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                profesoresFiltrados =
                    (results?.values as? List<Profesor>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }

        }

    }
    fun onSwipeDelete(position: Int) {
        val profesor = profesoresFiltrados[position]
        onDelete(profesor, position)
    }

    fun triggerEdit(profesor: Profesor, position: Int) {
        onEdit(profesor, position)
    }

    fun getProfesorAt(position: Int): Profesor {
        return profesoresFiltrados[position]
    }

}
