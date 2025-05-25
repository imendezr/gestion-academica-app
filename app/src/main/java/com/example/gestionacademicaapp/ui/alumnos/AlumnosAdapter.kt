package com.example.gestionacademicaapp.ui.alumnos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Alumno

class AlumnosAdapter(
    private val alumnos: MutableList<Alumno>
) : RecyclerView.Adapter<AlumnosAdapter.AlumnoViewHolder>(), Filterable {

    private var alumnosFiltrados: MutableList<Alumno> = alumnos.toMutableList()

    inner class AlumnoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCarrera: TextView = itemView.findViewById(R.id.tvCarrera)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alumno, parent, false)
        return AlumnoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        val alumno = alumnosFiltrados[position]
        holder.tvNombre.text = alumno.nombre
        holder.tvCarrera.text = alumno.carrera
    }

    override fun getItemCount(): Int = alumnosFiltrados.size

    fun agregarItem(alumno: Alumno) {
        alumnos.add(alumno)
        alumnosFiltrados.add(alumno)
        notifyItemInserted(alumnosFiltrados.size - 1)
    }

    fun eliminarItem(position: Int) {
        val alumno = alumnosFiltrados[position]
        alumnos.remove(alumno)
        alumnosFiltrados.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    alumnos.toList()
                } else {
                    alumnos.filter {
                        it.nombre.lowercase().contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                alumnosFiltrados = (results?.values as? List<Alumno>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
