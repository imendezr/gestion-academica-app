package com.example.gestionacademicaapp.ui.carreras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Carrera

class CarrerasAdapter(
    private val onEdit: (Carrera, Int) -> Unit,
    private val onDelete: (Carrera, Int) -> Unit,
    private val onViewCursos: (Carrera) -> Unit
) : RecyclerView.Adapter<CarrerasAdapter.CarreraViewHolder>(), Filterable {

    private val carreras: MutableList<Carrera> = mutableListOf()
    private var carrerasFiltradas: MutableList<Carrera> = mutableListOf()

    inner class CarreraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val btnViewCursos: ImageButton = itemView.findViewById(R.id.btnViewCursos)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEdit(carrerasFiltradas[position], position)
                }
            }
            btnViewCursos.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onViewCursos(carrerasFiltradas[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarreraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrera, parent, false)
        return CarreraViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarreraViewHolder, position: Int) {
        val carrera = carrerasFiltradas[position]
        holder.tvNombre.text = carrera.nombre
        holder.tvDescripcion.text = "Código: ${carrera.codigo} · Título: ${carrera.titulo}"
    }

    override fun getItemCount(): Int = carrerasFiltradas.size

    fun updateCarreras(newCarreras: List<Carrera>) {
        carreras.clear()
        carreras.addAll(newCarreras)
        carrerasFiltradas.clear()
        carrerasFiltradas.addAll(newCarreras)
        notifyDataSetChanged()
    }

    fun restoreFilteredList() {
        carrerasFiltradas.clear()
        carrerasFiltradas.addAll(carreras)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    carreras.toList()
                } else {
                    carreras.filter {
                        it.nombre.lowercase().contains(filtro) ||
                                it.codigo.lowercase().contains(filtro)
                    }
                }
                return FilterResults().apply { values = resultados }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                carrerasFiltradas =
                    (results?.values as? List<Carrera>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    fun onSwipeDelete(position: Int) {
        val carrera = carrerasFiltradas[position]
        onDelete(carrera, position)
    }

    fun triggerEdit(carrera: Carrera, position: Int) {
        onEdit(carrera, position)
    }

    fun getCarreraAt(position: Int): Carrera {
        return carrerasFiltradas[position]
    }
}
