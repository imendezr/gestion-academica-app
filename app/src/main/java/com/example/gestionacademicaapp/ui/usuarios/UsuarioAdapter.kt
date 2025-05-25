package com.example.gestionacademicaapp.ui.usuarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.model.Usuario

class UsuarioAdapter(
    private val usuarios: MutableList<Usuario>
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>(), Filterable {

    private var usuariosFiltrados: MutableList<Usuario> = usuarios.toMutableList()

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreo)
        val tvTipoUsuario: TextView = itemView.findViewById(R.id.tvTipoUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuariosFiltrados[position]
        holder.tvUsername.text = usuario.username
        holder.tvNombre.text = usuario.nombre
        holder.tvCorreo.text = usuario.correo
        holder.tvTipoUsuario.text = usuario.tipoUsuario
    }

    override fun getItemCount(): Int = usuariosFiltrados.size

    fun agregarItem(usuario: Usuario) {
        usuarios.add(usuario)
        usuariosFiltrados.add(usuario)
        notifyItemInserted(usuariosFiltrados.size - 1)
    }

    fun eliminarItem(position: Int) {
        val usuario = usuariosFiltrados[position]
        usuarios.remove(usuario)
        usuariosFiltrados.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.lowercase() ?: ""
                val resultados = if (filtro.isEmpty()) {
                    usuarios.toList()
                } else {
                    usuarios.filter {
                        it.username.lowercase().contains(filtro)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultados
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                usuariosFiltrados = (results?.values as? List<Usuario>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
