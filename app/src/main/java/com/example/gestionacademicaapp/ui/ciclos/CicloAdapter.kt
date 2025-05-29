package com.example.gestionacademicaapp.ui.ciclos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.utils.isVisible

class CiclosAdapter(
    private val onEdit: (Ciclo, Int) -> Unit,
    private val onDelete: (Ciclo, Int) -> Unit,
    private val onActivate: (Ciclo) -> Unit
) : RecyclerView.Adapter<CiclosAdapter.CicloViewHolder>(), Filterable {

    private val ciclos: MutableList<Ciclo> = mutableListOf()
    private var ciclosFiltrados: MutableList<Ciclo> = mutableListOf()

    inner class CicloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val btnActivate: ImageButton = itemView.findViewById(R.id.btnActivate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEdit(ciclosFiltrados[position], position)
                }
            }
            btnActivate.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onActivate(ciclosFiltrados[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CicloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ciclo, parent, false)
        return CicloViewHolder(view)
    }

    override fun onBindViewHolder(holder: CicloViewHolder, position: Int) {
        val ciclo = ciclosFiltrados[position]
        holder.tvNombre.text = "${ciclo.anio} - ${ciclo.numero}"
        holder.tvDescripcion.text =
            "Inicio: ${ciclo.fechaInicio} · Fin: ${ciclo.fechaFin} · Estado: ${ciclo.estado}"
        holder.btnActivate.isVisible =
            ciclo.estado != "ACTIVO" // Mostrar botón solo si no está activo
    }

    override fun getItemCount(): Int = ciclosFiltrados.size

    fun updateCiclos(newCiclos: List<Ciclo>) {
        ciclos.clear()
        ciclos.addAll(newCiclos)
        ciclosFiltrados.clear()
        ciclosFiltrados.addAll(newCiclos)
        notifyDataSetChanged()
    }

    fun restoreFilteredList() {
        ciclosFiltrados.clear()
        ciclosFiltrados.addAll(ciclos)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    ciclos.toList()
                } else {
                    ciclos.filter {
                        it.anio.toString().contains(filtro) ||
                                it.numero.toString().contains(filtro)
                    }
                }
                return FilterResults().apply { values = resultados }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                ciclosFiltrados =
                    (results?.values as? List<Ciclo>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    fun onSwipeDelete(position: Int) {
        val ciclo = ciclosFiltrados[position]
        onDelete(ciclo, position)
    }

    fun triggerEdit(ciclo: Ciclo, position: Int) {
        onEdit(ciclo, position)
    }

    fun getCicloAt(position: Int): Ciclo {
        return ciclosFiltrados[position]
    }
}
