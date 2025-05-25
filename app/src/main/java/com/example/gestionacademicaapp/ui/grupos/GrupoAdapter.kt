package com.example.gestionacademicaapp.ui.grupos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Grupo

class GrupoAdapter(
    private val grupos: MutableList<Grupo>
) : RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder>(), Filterable {

    private var gruposFiltrados: MutableList<Grupo> = grupos.toMutableList()

    inner class GrupoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCiclo: TextView = itemView.findViewById(R.id.tvCiclo)
        val tvCantidadAlumnos: TextView = itemView.findViewById(R.id.tvCantidadAlumnos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grupo, parent, false)
        return GrupoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GrupoViewHolder, position: Int) {
        val grupo = gruposFiltrados[position]
        holder.tvNombre.text = grupo.nombre
        holder.tvCiclo.text = grupo.ciclo
        holder.tvCantidadAlumnos.text = grupo.cantidadAlumnos.toString()
    }

    override fun getItemCount(): Int = gruposFiltrados.size

    fun agregarItem(grupo: Grupo) {
        grupos.add(grupo)
        gruposFiltrados.add(grupo)
        notifyItemInserted(gruposFiltrados.size - 1)
    }

    fun eliminarItem(position: Int) {
        val grupo = gruposFiltrados[position]
        grupos.remove(grupo)
        gruposFiltrados.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    grupos.toList()
                } else {
                    grupos.filter {
                        it.nombre.lowercase().contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                gruposFiltrados = (results?.values as? List<Grupo>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
