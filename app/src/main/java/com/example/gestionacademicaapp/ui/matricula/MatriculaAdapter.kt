// MatriculaAdapter.kt
package com.example.gestionacademicaapp.ui.matricula

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.databinding.ItemMatriculaBinding
import com.example.gestionacademicaapp.ui.common.adapter.BaseAdapter

class MatriculaAdapter(
    private val onClick: (Alumno) -> Unit,
    private val onMatricular: (Alumno) -> Unit
) : BaseAdapter<Alumno, MatriculaAdapter.MatriculaViewHolder>(
    diffCallback = object : DiffUtil.ItemCallback<Alumno>() {
        override fun areItemsTheSame(oldItem: Alumno, newItem: Alumno): Boolean =
            oldItem.idAlumno == newItem.idAlumno
        override fun areContentsTheSame(oldItem: Alumno, newItem: Alumno): Boolean =
            oldItem == newItem
    },
    onEdit = { onClick(it) },
    onDelete = {}
) {

    inner class MatriculaViewHolder(private val binding: ItemMatriculaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alumno: Alumno) {
            with(binding) {
                txtNombreAlumno.text = alumno.nombre
                txtCedula.text = alumno.cedula
                txtNombreAlumno.contentDescription = itemView.context.getString(R.string.nombre_alumno)
                txtCedula.contentDescription = itemView.context.getString(R.string.cedula_alumno)
                btnMatricular.apply {
                    contentDescription = itemView.context.getString(R.string.matricular_curso)
                    setOnClickListener { onMatricular(alumno) }
                }
                root.setOnClickListener { onClick(alumno) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatriculaViewHolder {
        val binding = ItemMatriculaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatriculaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatriculaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
