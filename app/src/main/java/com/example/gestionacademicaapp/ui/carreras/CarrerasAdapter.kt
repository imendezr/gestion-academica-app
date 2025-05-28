package com.example.gestionacademicaapp.ui.carreras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera

class CarrerasAdapter(
    private val carreras: MutableList<Carrera>
) : RecyclerView.Adapter<CarrerasAdapter.CarreraViewHolder>(), Filterable {

    private var carrerasFiltradas: MutableList<Carrera> = carreras.toMutableList()

    inner class CarreraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarreraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrera, parent, false)
        return CarreraViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarreraViewHolder, position: Int) {
        val carrera = carrerasFiltradas[position]
        holder.tvNombre.text = "${carrera.nombre} (${carrera.codigo})"
        holder.tvDescripcion.text = carrera.titulo
    }

    override fun getItemCount(): Int = carrerasFiltradas.size


    fun agregarItem(carrera: Carrera) {
        carreras.add(carrera)
        carrerasFiltradas.add(carrera)
        notifyItemInserted(carrerasFiltradas.size - 1)
    }


    fun eliminarItem(position: Int) {
        val carrera = carrerasFiltradas[position]
        carreras.remove(carrera)
        carrerasFiltradas.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    carreras.toList()
                } else {
                    carreras.filter {
                        it.nombre.lowercase().contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                carrerasFiltradas = (results?.values as? List<Carrera>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
