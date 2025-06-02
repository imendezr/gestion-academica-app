package com.example.gestionacademicaapp.ui.common.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val onEdit: (T) -> Unit,
    private val onDelete: (T) -> Unit
) : ListAdapter<T, VH>(diffCallback) {

    fun getItemAt(position: Int): T = getItem(position)

    protected fun setupDefaultClickListener(itemView: View, item: T) {
        itemView.setOnClickListener { onEdit(item) }
    }
}
