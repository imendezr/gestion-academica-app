package com.example.gestionacademicaapp.ui.usuarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario

class UsuariosAdapter(
    private val idUsuarioActual: Long,
    private val onEdit: (Usuario) -> Unit
) : ListAdapter<Usuario, UsuariosAdapter.UsuarioViewHolder>(DiffCallback) {

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCedula: TextView = itemView.findViewById(R.id.tvCedula)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)

        fun bind(usuario: Usuario) {
            val context = itemView.context
            val cedulaTexto = if (usuario.idUsuario == idUsuarioActual) {
                context.getString(R.string.cedula_actual, usuario.cedula)
            } else {
                usuario.cedula
            }
            val rolTexto = context.getString(R.string.rol_usuario, usuario.tipo)

            tvCedula.text = cedulaTexto
            tvTipo.text = rolTexto

            itemView.setOnClickListener { onEdit(usuario) }
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

    fun getUsuarioAt(position: Int): Usuario = getItem(position)

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Usuario>() {
            override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean =
                oldItem.idUsuario == newItem.idUsuario

            override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean =
                oldItem == newItem
        }
    }
}
