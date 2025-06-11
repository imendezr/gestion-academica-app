package com.example.gestionacademicaapp.ui.oferta_academica

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.databinding.ItemCursoOfertaBinding
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class CursoAdapter(
    private val onVerGrupos: (CursoDto) -> Unit
) : BaseAdapter<CursoDto, CursoAdapter.CursoViewHolder>(
    diffCallback = object : DiffUtil.ItemCallback<CursoDto>() {
        override fun areItemsTheSame(oldItem: CursoDto, newItem: CursoDto): Boolean =
            oldItem.idCurso == newItem.idCurso

        override fun areContentsTheSame(oldItem: CursoDto, newItem: CursoDto): Boolean =
            oldItem == newItem
    },
    onEdit = { onVerGrupos(it) },
    onDelete = {}
) {

    inner class CursoViewHolder(private val binding: ItemCursoOfertaBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(curso: CursoDto) {
            with(binding) {
                txtCodigo.text = curso.codigo
                txtNombre.text = curso.nombre
                root.setOnClickListener { onVerGrupos(curso) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val binding =
            ItemCursoOfertaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CursoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
