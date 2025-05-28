package com.example.gestionacademicaapp.ui.ciclos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Ciclo

class CicloAdapter(
    private val ciclos: MutableList<Ciclo>
) : RecyclerView.Adapter<CicloAdapter.CicloViewHolder>(), Filterable {

    private var ciclosFiltrados: MutableList<Ciclo> = ciclos.toMutableList()

    inner class CicloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val tvFechaInicio: TextView = itemView.findViewById(R.id.tvFechaInicio)
        val tvFechaFin: TextView = itemView.findViewById(R.id.tvFechaFin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CicloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ciclo, parent, false)
        return CicloViewHolder(view)
    }

    override fun onBindViewHolder(holder: CicloViewHolder, position: Int) {
        val ciclo = ciclosFiltrados[position]
        holder.tvNombre.text = "Año ${ciclo.anio}, Ciclo ${ciclo.numero}"
        holder.tvDescripcion.text = "Estado: ${ciclo.estado}"
        holder.tvFechaInicio.text = "Inicio: ${ciclo.fechaInicio}"
        holder.tvFechaFin.text = "Fin: ${ciclo.fechaFin}"
    }

    override fun getItemCount(): Int = ciclosFiltrados.size

    fun agregarItem(ciclo: Ciclo) {
        ciclos.add(ciclo)
        ciclosFiltrados.add(ciclo)
        notifyItemInserted(ciclosFiltrados.size - 1)
    }

    fun eliminarItem(position: Int) {
        val ciclo = ciclosFiltrados[position]
        ciclos.remove(ciclo)
        ciclosFiltrados.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    ciclos.toList()
                } else {
                    ciclos.filter {
                        "${it.anio}${it.numero}".contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                ciclosFiltrados = (results?.values as? List<Ciclo>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
