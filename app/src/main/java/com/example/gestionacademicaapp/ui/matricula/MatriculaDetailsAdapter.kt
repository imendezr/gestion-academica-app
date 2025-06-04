package com.example.gestionacademicaapp.ui.matricula

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.databinding.ItemMatriculaDetailBinding
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class MatriculaDetailsAdapter(
    onEdit: (MatriculaAlumnoDto) -> Unit
) : BaseAdapter<MatriculaAlumnoDto, MatriculaDetailsAdapter.MatriculaViewHolder>(
    diffCallback = object : DiffUtil.ItemCallback<MatriculaAlumnoDto>() {
        override fun areItemsTheSame(oldItem: MatriculaAlumnoDto, newItem: MatriculaAlumnoDto): Boolean =
            oldItem.idMatricula == newItem.idMatricula
        override fun areContentsTheSame(oldItem: MatriculaAlumnoDto, newItem: MatriculaAlumnoDto): Boolean =
            oldItem == newItem
    },
    onEdit = onEdit,
    onDelete = {}
) {

    inner class MatriculaViewHolder(private val binding: ItemMatriculaDetailBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(matricula: MatriculaAlumnoDto) {
            with(binding) {
                txtCurso.text = matricula.nombreCurso
                txtGrupo.text = matricula.numeroGrupo
                txtNota.text = matricula.nota.toString()
                txtCurso.contentDescription = itemView.context.getString(R.string.nombre_curso)
                txtGrupo.contentDescription = itemView.context.getString(R.string.numero_grupo)
                txtNota.contentDescription = itemView.context.getString(R.string.nota_matricula)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatriculaViewHolder {
        val binding = ItemMatriculaDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatriculaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatriculaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
