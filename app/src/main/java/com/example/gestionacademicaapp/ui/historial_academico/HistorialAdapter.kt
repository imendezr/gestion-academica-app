package com.example.gestionacademicaapp.ui.historial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Historial

class HistorialAdapter(
    private val historiales: MutableList<Historial>
) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>(), Filterable {

    private var historialesFiltrados: MutableList<Historial> = historiales.toMutableList()

    inner class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCurso: TextView = itemView.findViewById(R.id.tvCurso)
        val tvNota: TextView = itemView.findViewById(R.id.tvNota)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val historial = historialesFiltrados[position]
        holder.tvCurso.text = historial.curso
        holder.tvNota.text = historial.nota.toString()
        holder.tvFecha.text = historial.fecha
    }

    override fun getItemCount(): Int = historialesFiltrados.size

    fun agregarItem(historial: Historial) {
        historiales.add(historial)
        historialesFiltrados.add(historial)
        notifyItemInserted(historialesFiltrados.size - 1)
    }

    fun eliminarItem(position: Int) {
        val historial = historialesFiltrados[position]
        historiales.remove(historial)
        historialesFiltrados.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    historiales.toList()
                } else {
                    historiales.filter {
                        it.curso.lowercase().contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                historialesFiltrados = (results?.values as? List<Historial>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
