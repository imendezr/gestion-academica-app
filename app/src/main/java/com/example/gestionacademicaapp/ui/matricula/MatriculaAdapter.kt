package com.example.gestionacademicaapp.ui.matricula

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Matricula

class MatriculaAdapter(
    private val matriculas: MutableList<Matricula>
) : RecyclerView.Adapter<MatriculaAdapter.MatriculaViewHolder>(), Filterable {

    private var matriculasFiltradas: MutableList<Matricula> = matriculas.toMutableList()

    inner class MatriculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGrupo: TextView = itemView.findViewById(R.id.tvGrupo)
        val tvNota: TextView = itemView.findViewById(R.id.tvNota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatriculaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matricula, parent, false)
        return MatriculaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatriculaViewHolder, position: Int) {
        val matricula = matriculasFiltradas[position]
        holder.tvGrupo.text = "Grupo: ${matricula.pkGrupo} · Alumno: ${matricula.pkAlumno}"
        holder.tvNota.text = "Nota: ${matricula.nota}"
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
                        it.pkGrupo.toString().contains(filtro) || it.pkAlumno.toString().contains(filtro)
                    }
                }

                return FilterResults().apply { values = resultados }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                matriculasFiltradas = (results?.values as? List<Matricula>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
