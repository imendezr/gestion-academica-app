package com.example.gestionacademicaapp.ui.oferta

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.databinding.ItemCursoOfertaBinding // New binding
import com.example.gestionacademicaapp.databinding.ItemGrupoBinding

class OfertaAcademicaAdapter(
    private val onVerGrupos: (CursoDto) -> Unit,
    private val onEditGrupo: (GrupoDto) -> Unit,
    private val onDeleteGrupo: (GrupoDto) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Any> = emptyList()
    private var isCursoView = true

    fun submitCursos(cursos: List<CursoDto>) {
        isCursoView = true
        val newItems = cursos.toList()
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                when {
                    items[oldItemPosition] is CursoDto ->
                        (items[oldItemPosition] as CursoDto).idCurso == newItems[newItemPosition].idCurso
                    else -> false
                }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                items[oldItemPosition] == newItems[newItemPosition]
        })
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    fun submitGrupos(grupos: List<GrupoDto>) {
        isCursoView = false
        val newItems = grupos.toList()
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                when {
                    items[oldItemPosition] is GrupoDto ->
                        (items[oldItemPosition] as GrupoDto).idGrupo == newItems[newItemPosition].idGrupo
                    else -> false
                }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                items[oldItemPosition] == newItems[newItemPosition]
        })
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    fun getItemAt(position: Int): Any? = items.getOrNull(position)
    fun getItemPosition(item: Any): Int = items.indexOf(item)

    override fun getItemViewType(position: Int): Int {
        return if (isCursoView) VIEW_TYPE_CURSO else VIEW_TYPE_GRUPO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CURSO) {
            val binding = ItemCursoOfertaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CursoViewHolder(binding)
        } else {
            val binding = ItemGrupoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            GrupoViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CursoViewHolder -> holder.bind(items[position] as CursoDto)
            is GrupoViewHolder -> holder.bind(items[position] as GrupoDto)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class CursoViewHolder(private val binding: ItemCursoOfertaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(curso: CursoDto) {
            with(binding) {
                txtCodigo.text = curso.codigo
                txtNombre.text = curso.nombre
                txtCodigo.contentDescription = itemView.context.getString(R.string.codigo_curso)
                txtNombre.contentDescription = itemView.context.getString(R.string.nombre_curso)
                root.setOnClickListener { onVerGrupos(curso) }
            }
        }
    }

    inner class GrupoViewHolder(private val binding: ItemGrupoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(grupo: GrupoDto) {
            with(binding) {
                txtNumeroGrupo.text = grupo.numeroGrupo.toString()
                txtHorario.text = grupo.horario
                txtProfesor.text = grupo.nombreProfesor
                txtNumeroGrupo.contentDescription = itemView.context.getString(R.string.numero_grupo)
                txtHorario.contentDescription = itemView.context.getString(R.string.horario)
                txtProfesor.contentDescription = itemView.context.getString(R.string.profesor)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_CURSO = 1
        private const val VIEW_TYPE_GRUPO = 2
    }
}