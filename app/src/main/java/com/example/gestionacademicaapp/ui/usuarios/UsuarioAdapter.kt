package com.example.gestionacademicaapp.ui.usuarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Usuario

class UsuarioAdapter(
    private val usuarios: MutableList<Usuario>
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>(), Filterable {

    private var usuariosFiltrados: MutableList<Usuario> = usuarios.toMutableList()

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCedula: TextView = itemView.findViewById(R.id.tvCedula)
        val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuariosFiltrados[position]
        holder.tvCedula.text = "Cédula: ${usuario.cedula}"
        holder.tvTipo.text = "Tipo: ${usuario.tipo}"
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
                        it.cedula.lowercase().contains(filtro)
                    }
                }

                return FilterResults().apply { values = resultados }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                usuariosFiltrados = (results?.values as? List<Usuario>)?.toMutableList() ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
