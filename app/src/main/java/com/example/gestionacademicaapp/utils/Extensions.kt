package com.example.gestionacademicaapp.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionacademicaapp.R

/**
 * Habilita acciones de deslizamiento en un RecyclerView con indicadores visuales.
 *
 * @param onSwipeLeft Acción a ejecutar al deslizar a la izquierda (por ejemplo, eliminar).
 * @param onSwipeRight Acción a ejecutar al deslizar a la derecha (por ejemplo, editar).
 * @param leftIcon Ícono para el deslizamiento a la izquierda (por defecto, ic_delete).
 * @param rightIcon Ícono para el deslizamiento a la derecha (por defecto, ic_reorder).
 * @param leftBackgroundColor Color de fondo para el deslizamiento a la izquierda (por defecto, colorError).
 * @param rightBackgroundColor Color de fondo para el deslizamiento a la derecha (por defecto, holo_orange_light).
 */
fun RecyclerView.enableSwipeActions(
    onSwipeLeft: (position: Int) -> Unit,
    onSwipeRight: (position: Int) -> Unit,
    @DrawableRes leftIcon: Int = R.drawable.ic_delete,
    @DrawableRes rightIcon: Int = R.drawable.ic_reorder,
    @ColorRes leftBackgroundColor: Int = R.color.colorError,
    @ColorRes rightBackgroundColor: Int = android.R.color.holo_orange_light
): ItemTouchHelper {
    val paint = Paint().apply { isAntiAlias = true }
    val path = Path()
    val outerCornerRadius = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics
    )
    val innerCornerRadius = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics
    )

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
            // Limpiar estado visual del ViewHolder
            clearView(this@enableSwipeActions, viewHolder)
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
            val isSwipeLeft = dX < 0
            val icon = ContextCompat.getDrawable(
                context,
                if (isSwipeLeft) leftIcon else rightIcon
            )?.apply { setTint(ContextCompat.getColor(context, android.R.color.white)) }

            paint.color = ContextCompat.getColor(
                context,
                if (isSwipeLeft) leftBackgroundColor else rightBackgroundColor
            )

            val rect = if (isSwipeLeft) {
                RectF(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
            } else {
                RectF(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    itemView.left + dX,
                    itemView.bottom.toFloat()
                )
            }

            val radii = if (isSwipeLeft) {
                floatArrayOf(
                    innerCornerRadius, innerCornerRadius, // superior-izquierda
                    outerCornerRadius, outerCornerRadius, // superior-derecha
                    outerCornerRadius, outerCornerRadius, // inferior-derecha
                    innerCornerRadius, innerCornerRadius  // inferior-izquierda
                )
            } else {
                floatArrayOf(
                    outerCornerRadius, outerCornerRadius, // superior-izquierda
                    innerCornerRadius, innerCornerRadius, // superior-derecha
                    innerCornerRadius, innerCornerRadius, // inferior-derecha
                    outerCornerRadius, outerCornerRadius  // inferior-izquierda
                )
            }

            path.reset()
            path.addRoundRect(rect, radii, Path.Direction.CW)
            c.drawPath(path, paint)

            icon?.let {
                val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                val bounds = if (isSwipeLeft) {
                    Rect(
                        itemView.right - iconMargin - it.intrinsicWidth,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin
                    )
                } else {
                    Rect(
                        itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin + it.intrinsicWidth,
                        itemView.bottom - iconMargin
                    )
                }
                it.bounds = bounds
                it.draw(c)
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    })
    itemTouchHelper.attachToRecyclerView(this)
    return itemTouchHelper
}

/**
 * Clears the swipe state for a specific item in the RecyclerView.
 *
 * @param position The adapter position of the item to clear.
 * @param itemTouchHelper The ItemTouchHelper instance attached to the RecyclerView.
 */
fun RecyclerView.clearSwipe(position: Int, itemTouchHelper: ItemTouchHelper?) {
    findViewHolderForAdapterPosition(position)?.let { viewHolder ->
        itemTouchHelper?.let { helper ->
            // Use reflection to access the internal clearView method or trigger a reset
            try {
                val callbackField = ItemTouchHelper::class.java.getDeclaredField("mCallback")
                callbackField.isAccessible = true
                val callback = callbackField.get(helper) as ItemTouchHelper.Callback
                callback.clearView(this, viewHolder)
            } catch (e: Exception) {
                // Fallback: Notify adapter to force redraw
                adapter?.notifyItemChanged(position)
            }
        }
    }
}
