package com.example.gestionacademicaapp.ui.alumnos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Alumno

class AlumnosAdapter(
    private val alumnos: MutableList<Alumno>,
    private val onEditar: (Alumno, Int) -> Unit,
    private val onEliminar: (Alumno) -> Unit
) : RecyclerView.Adapter<AlumnosAdapter.AlumnoViewHolder>(), Filterable {

    private var alumnosFiltrados: MutableList<Alumno> = alumnos.toMutableList()

    inner class AlumnoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCarrera: TextView = itemView.findViewById(R.id.tvCarrera)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alumno, parent, false)
        return AlumnoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        val alumno = alumnosFiltrados[position]
        holder.tvNombre.text = "${alumno.nombre} (${alumno.cedula})"
        holder.tvCarrera.text = "Carrera ID: ${alumno.pkCarrera}"
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
        onEliminar(alumno)
    }

    fun actualizarItem(position: Int, alumnoActualizado: Alumno) {
        val original = alumnosFiltrados[position]
        val index = alumnos.indexOfFirst { it.idAlumno == original.idAlumno }
        if (index != -1) alumnos[index] = alumnoActualizado
        alumnosFiltrados[position] = alumnoActualizado
        notifyItemChanged(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) alumnos.toList() else alumnos.filter {
                    it.nombre.lowercase().contains(filtro) ||
                            it.cedula.lowercase().contains(filtro)
                }
                return FilterResults().apply { values = resultados }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                alumnosFiltrados =
                    (results?.values as? List<Alumno>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
