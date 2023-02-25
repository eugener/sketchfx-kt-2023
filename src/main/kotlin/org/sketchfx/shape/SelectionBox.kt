package org.sketchfx.shape

import javafx.geometry.Bounds
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.BoundsExt.union

class SelectionBox(selection: Collection<Shape>, context: CanvasViewModel): Group() {



    init {

        val (selectionShapes, selectionBounds) =
            selection.fold(Pair(listOf<Node>(), null as Bounds?)) { (selShapes, selBounds), shape ->
                val shapeBounds = shape.boundsInParent
                val b = selBounds?.let { selBounds.union(shapeBounds) } ?: shapeBounds
                Pair(selShapes + Shape.selection(shape), b)
            }

        children.setAll((selectionShapes + Shape.selectionBounds(selectionBounds!!, context)))

        children.addAll(
            SelectionHandle(selectionBounds.minX, selectionBounds.minY, Cursor.NW_RESIZE, context),
            SelectionHandle(selectionBounds.maxX, selectionBounds.maxY, Cursor.SE_RESIZE, context),
            SelectionHandle(selectionBounds.maxX, selectionBounds.minY, Cursor.NE_RESIZE, context),
            SelectionHandle(selectionBounds.minX, selectionBounds.maxY, Cursor.SW_RESIZE, context),
        )
    }

}

class SelectionHandle( x: Double, y: Double, cursor: Cursor, context: CanvasViewModel ): Group() {

    companion object {
        private const val handleSize: Int = 8
    }

    init{
        val size = handleSize / context.scale
        val offset = size / 2
        val lineWidth = 1/ context.scale

        val rect = Rectangle(x-offset, y-offset, size, size).apply{
            fill = Color.WHITE
            stroke = Shape.selectionStroke
            strokeWidth = lineWidth
            this.cursor = cursor
        }
        children.setAll(rect)
    }

}


