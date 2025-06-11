package com.example.gestionacademicaapp.ui.oferta_academica

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.databinding.ItemGrupoBinding
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class GrupoAdapter(
    private val onEditGrupo: (GrupoDto) -> Unit,
    private val onDeleteGrupo: (GrupoDto) -> Unit
) : BaseAdapter<GrupoDto, GrupoAdapter.GrupoViewHolder>(
    diffCallback = object : DiffUtil.ItemCallback<GrupoDto>() {
        override fun areItemsTheSame(oldItem: GrupoDto, newItem: GrupoDto): Boolean =
            oldItem.idGrupo == newItem.idGrupo

        override fun areContentsTheSame(oldItem: GrupoDto, newItem: GrupoDto): Boolean =
            oldItem == newItem
    },
    onEdit = { onEditGrupo(it) },
    onDelete = { onDeleteGrupo(it) }
) {

    inner class GrupoViewHolder(private val binding: ItemGrupoBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(grupo: GrupoDto) {
            with(binding) {
                txtNumeroGrupo.text = grupo.numeroGrupo.toString()
                txtHorario.text = grupo.horario
                txtProfesor.text = grupo.nombreProfesor
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoViewHolder {
        val binding = ItemGrupoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GrupoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrupoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
