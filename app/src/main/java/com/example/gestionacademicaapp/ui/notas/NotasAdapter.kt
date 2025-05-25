package com.example.gestionacademicaapp.ui.notas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Nota

class NotasAdapter(
    private val notas: MutableList<Nota>
) : RecyclerView.Adapter<NotasAdapter.NotaViewHolder>(), Filterable {

    private var notasFiltradas: MutableList<Nota> = notas.toMutableList()

    inner class NotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCurso: TextView = itemView.findViewById(R.id.tvCurso)
        val tvNota: TextView = itemView.findViewById(R.id.tvNota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = notasFiltradas[position]
        holder.tvCurso.text = nota.curso
        holder.tvNota.text = nota.nota.toString()
    }

    override fun getItemCount(): Int = notasFiltradas.size

    fun agregarItem(nota: Nota) {
        notas.add(nota)
        notasFiltradas.add(nota)
        notifyItemInserted(notasFiltradas.size - 1)
    }

    fun eliminarItem(position: Int) {
        val nota = notasFiltradas[position]
        notas.remove(nota)
        notasFiltradas.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    notas.toList()
                } else {
                    notas.filter {
                        it.curso.lowercase().contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notasFiltradas = (results?.values as? List<Nota>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
