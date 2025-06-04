package com.example.gestionacademicaapp.ui.matricula

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.databinding.ItemMatriculaGrupoBinding
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class MatriculaCursoGrupoAdapter(
    private val onSelect: (GrupoDto) -> Unit
) : BaseAdapter<GrupoDto, MatriculaCursoGrupoAdapter.GrupoViewHolder>(
    diffCallback = object : DiffUtil.ItemCallback<GrupoDto>() {
        override fun areItemsTheSame(oldItem: GrupoDto, newItem: GrupoDto): Boolean =
            oldItem.idGrupo == newItem.idGrupo
        override fun areContentsTheSame(oldItem: GrupoDto, newItem: GrupoDto): Boolean =
            oldItem == newItem
    },
    onEdit = { onSelect(it) },
    onDelete = {}
) {

    private var selectedGrupoId: Long? = null

    fun updateSelection(newSelectedId: Long?) {
        val oldSelectedId = selectedGrupoId
        selectedGrupoId = newSelectedId
        // Notify changes for old and new selected items
        currentList.indexOfFirst { it.idGrupo == oldSelectedId }.takeIf { it >= 0 }?.let { notifyItemChanged(it) }
        currentList.indexOfFirst { it.idGrupo == newSelectedId }.takeIf { it >= 0 }?.let { notifyItemChanged(it) }
    }

    inner class GrupoViewHolder(private val binding: ItemMatriculaGrupoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(grupo: GrupoDto) {
            with(binding) {
                txtNumeroGrupo.text = grupo.numeroGrupo.toString()
                txtProfesor.text = grupo.nombreProfesor
                txtNumeroGrupo.contentDescription = itemView.context.getString(R.string.numero_grupo)
                txtProfesor.contentDescription = itemView.context.getString(R.string.nombre_profe)
                root.isSelected = grupo.idGrupo == selectedGrupoId
                btnSelectGrupo.apply {
                    contentDescription = itemView.context.getString(R.string.seleccionar_grupo)
                    setOnClickListener {
                        onSelect(grupo)
                        updateSelection(grupo.idGrupo)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoViewHolder {
        val binding = ItemMatriculaGrupoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GrupoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrupoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}