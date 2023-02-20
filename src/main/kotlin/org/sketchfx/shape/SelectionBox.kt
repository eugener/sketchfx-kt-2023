package org.sketchfx.shape

import javafx.geometry.Bounds
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.BoundsExt.union

class SelectionBox(selection: Collection<Shape>, private val context: CanvasViewModel): Group() {

    companion object {
        private const val handleSize: Int = 8
    }

    init {

        val (selectionShapes, selectionBounds) =
            selection.fold( Pair(listOf<Node>(), null as Bounds?)){ (selShapes, selBounds), shape ->
            val shapeBounds = shape.boundsInParent

            if (selBounds == null) {
                Pair(selShapes + Shape.selection(shape), shapeBounds)
            } else {
                Pair(selShapes + Shape.selection(shape), selBounds.union(shapeBounds))
            }
        }

        children.setAll((selectionShapes + Shape.selectionBounds(selectionBounds!!, context)))

        children.addAll(
            selectionHandle(selectionBounds.minX, selectionBounds.minY, Cursor.NW_RESIZE),
            selectionHandle(selectionBounds.maxX, selectionBounds.maxY, Cursor.SE_RESIZE),
            selectionHandle(selectionBounds.maxX, selectionBounds.minY, Cursor.NE_RESIZE),
            selectionHandle(selectionBounds.minX, selectionBounds.maxY, Cursor.SW_RESIZE),
        )
    }

    fun selectionHandle(x: Double, y: Double, cursor: Cursor): Node {

        val size = handleSize / context.scale
        val offset = size / 2
        val lineWidth = 1/ context.scale

        return Rectangle(x-offset, y-offset, size, size).apply{
            fill = Color.WHITE
            stroke = Shape.selectionStroke
            strokeWidth = lineWidth
            this.cursor = cursor
        }
    }


}
