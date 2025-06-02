package com.example.gestionacademicaapp.ui.ciclos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter
import com.example.gestionacademicaapp.utils.Notificador

class CiclosAdapter(
    onEdit: (Ciclo) -> Unit,
    onDelete: (Ciclo) -> Unit,
    private val onActivateCiclo: (Ciclo) -> Unit
) : BaseAdapter<Ciclo, CiclosAdapter.CicloViewHolder>(DiffCallback, onEdit, onDelete) {

    inner class CicloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val btnActivate: ImageButton = itemView.findViewById(R.id.btnActivate)

        fun bind(ciclo: Ciclo) {
            tvNombre.text =
                itemView.context.getString(R.string.label_ciclo_nombre, ciclo.anio, ciclo.numero)
            tvDescripcion.text = itemView.context.getString(
                R.string.label_ciclo_detalle,
                ciclo.fechaInicio,
                ciclo.fechaFin,
                ciclo.estado
            )

            btnActivate.isVisible = true
            btnActivate.setImageResource(
                if (ciclo.estado.equals("ACTIVO", ignoreCase = true)) R.drawable.ic_active
                else R.drawable.ic_activate
            )
            btnActivate.contentDescription = itemView.context.getString(R.string.desc_activar_ciclo)

            btnActivate.setOnClickListener(null)
            if (ciclo.estado.equals("ACTIVO", ignoreCase = true)) {
                btnActivate.setOnClickListener {
                    Notificador.show(
                        itemView,
                        "El ciclo ya está activo.",
                        R.color.colorPrimary,
                        anchorView = btnActivate
                    )
                }
            } else {
                btnActivate.setOnClickListener { onActivateCiclo(ciclo) }
            }

            setupDefaultClickListener(itemView, ciclo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CicloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ciclo, parent, false)
        return CicloViewHolder(view)
    }

    override fun onBindViewHolder(holder: CicloViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Ciclo>() {
            override fun areItemsTheSame(oldItem: Ciclo, newItem: Ciclo): Boolean =
                oldItem.idCiclo == newItem.idCiclo

            override fun areContentsTheSame(oldItem: Ciclo, newItem: Ciclo): Boolean =
                oldItem == newItem
        }
    }
}
