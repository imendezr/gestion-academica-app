package com.example.gestionacademicaapp.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R

fun RecyclerView.enableSwipeActions(
    onSwipeLeft: (position: Int) -> Unit,
    onSwipeRight: (position: Int) -> Unit
) {
    val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            when (direction) {
                ItemTouchHelper.LEFT -> onSwipeLeft(position)
                ItemTouchHelper.RIGHT -> onSwipeRight(position)
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val paint = Paint().apply { isAntiAlias = true }
            // Convertir radios a píxeles
            val outerCornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10f,
                context.resources.displayMetrics
            )
            val innerCornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4f,
                context.resources.displayMetrics
            )
            val icon = when {
                dX > 0 -> ContextCompat.getDrawable(context, R.drawable.ic_reorder)?.apply {
                    setTint(ContextCompat.getColor(context, android.R.color.white))
                }

                dX < 0 -> ContextCompat.getDrawable(context, R.drawable.ic_delete)?.apply {
                    setTint(ContextCompat.getColor(context, android.R.color.white))
                }

                else -> null
            }

            val path = Path()
            when {
                dX > 0 -> { // Swipe derecho (editar/reordenar) - Amarillo
                    paint.color = ContextCompat.getColor(context, android.R.color.holo_orange_light)
                    val rect = RectF(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        itemView.left + dX,
                        itemView.bottom.toFloat()
                    )
                    // Redondear esquinas izquierda (externas) con 10dp, derecha (internas) con 2dp
                    val radii = floatArrayOf(
                        outerCornerRadius, outerCornerRadius, // superior-izquierda
                        innerCornerRadius, innerCornerRadius, // superior-derecha
                        innerCornerRadius, innerCornerRadius, // inferior-derecha
                        outerCornerRadius, outerCornerRadius  // inferior-izquierda
                    )
                    path.addRoundRect(rect, radii, Path.Direction.CW)
                    c.drawPath(path, paint)

                    icon?.let {
                        val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                        it.setBounds(
                            itemView.left + iconMargin,
                            itemView.top + iconMargin,
                            itemView.left + iconMargin + it.intrinsicWidth,
                            itemView.bottom - iconMargin
                        )
                        it.draw(c)
                    }
                }

                dX < 0 -> { // Swipe izquierdo (eliminar) - Rojo
                    paint.color = ContextCompat.getColor(context, R.color.colorError)
                    val rect = RectF(
                        itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    // Redondear esquinas derecha (externas) con 10dp, izquierda (internas) con 2dp
                    val radii = floatArrayOf(
                        innerCornerRadius, innerCornerRadius, // superior-izquierda
                        outerCornerRadius, outerCornerRadius, // superior-derecha
                        outerCornerRadius, outerCornerRadius, // inferior-derecha
                        innerCornerRadius, innerCornerRadius  // inferior-izquierda
                    )
                    path.addRoundRect(rect, radii, Path.Direction.CW)
                    c.drawPath(path, paint)

                    icon?.let {
                        val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                        it.setBounds(
                            itemView.right - iconMargin - it.intrinsicWidth,
                            itemView.top + iconMargin,
                            itemView.right - iconMargin,
                            itemView.bottom - iconMargin
                        )
                        it.draw(c)
                    }
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    })
    itemTouchHelper.attachToRecyclerView(this)
}

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }
