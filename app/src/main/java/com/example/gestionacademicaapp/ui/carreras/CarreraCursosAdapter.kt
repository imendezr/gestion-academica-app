package com.example.gestionacademicaapp.ui.carreras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.ui.carreras.model.CarreraCursoUI

class CarreraCursosAdapter(
    private val onDelete: (CarreraCursoUI) -> Unit,
    private val onReorder: (CarreraCursoUI, Ciclo) -> Unit,
    private val onReorderRequest: (CarreraCursoUI) -> Unit,
    private val ciclosDisponibles: List<Ciclo>
) : RecyclerView.Adapter<CarreraCursosAdapter.CarreraCursoViewHolder>() {

    private val carreraCursos: MutableList<CarreraCursoUI> = mutableListOf()

    inner class CarreraCursoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val btnReorder: ImageButton = itemView.findViewById(R.id.btnReorder)

        fun bind(carreraCurso: CarreraCursoUI) {
            tvNombre.text = carreraCurso.curso.nombre
            val cicloText = carreraCurso.ciclo?.let { "${it.anio} - ${it.numero}" }
                ?: "Ciclo: ${carreraCurso.cicloId}"
            tvDescripcion.text = "Ciclo: $cicloText"
            btnDelete.setOnClickListener { onDelete(carreraCurso) }
            btnReorder.setOnClickListener { onReorderRequest(carreraCurso) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarreraCursoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrera_curso, parent, false)
        return CarreraCursoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarreraCursoViewHolder, position: Int) {
        holder.bind(carreraCursos[position])
    }

    override fun getItemCount(): Int = carreraCursos.size

    fun updateCarreraCursos(newCarreraCursos: List<CarreraCursoUI>) {
        carreraCursos.clear()
        carreraCursos.addAll(newCarreraCursos)
        notifyDataSetChanged() // Asegura que la UI se actualice
    }
}
