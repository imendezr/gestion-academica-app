package com.example.gestionacademicaapp.ui.usuarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class UsuariosAdapter(
    onEdit: (Usuario) -> Unit,
    onDelete: (Usuario) -> Unit
) : BaseAdapter<Usuario, UsuariosAdapter.UsuarioViewHolder>(DiffCallback, onEdit, onDelete) {

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCedula: TextView = itemView.findViewById(R.id.tvCedula)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)

        fun bind(usuario: Usuario) {
            tvCedula.text = usuario.cedula
            tvTipo.text = usuario.tipo
            setupDefaultClickListener(itemView, usuario)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Usuario>() {
            override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean =
                oldItem.idUsuario == newItem.idUsuario

            override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean =
                oldItem == newItem
        }
    }
}
