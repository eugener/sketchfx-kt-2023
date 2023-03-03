package org.sketchfx.shape

import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.BoundsExt.union

class SelectionBox(selection: Collection<Shape>, context: CanvasViewModel) : Group() {



    init {

        val (selectionShapes, selectionBounds) =
            selection.fold(Pair(listOf<Node>(), null as Bounds?)) { (selShapes, selBounds), shape ->
                val shapeBounds = shape.boundsInParent
                val b = selBounds?.let { selBounds.union(shapeBounds) } ?: shapeBounds
                Pair(selShapes + Shape.selection(shape), b)
            }

        children.setAll((selectionShapes + Shape.selectionBounds(selectionBounds!!, context)))

        children.addAll(
            SelectionHandleType.values().map { SelectionHandle(selectionBounds, it, context) }
        )
    }

}

class SelectionHandle(bounds: Bounds, type: SelectionHandleType, context: CanvasViewModel) : Group() {

    companion object {
        private const val handleSize: Int = 8
    }

    init {
        val size = handleSize / context.scale
        val offset = size / 2
        val lineWidth = 1 / context.scale
        val p = type.point(bounds)

        val rect = Rectangle(p.x - offset, p.y - offset, size, size).apply {
            fill = Color.WHITE
            stroke = Shape.selectionStroke
            strokeWidth = lineWidth
            this.cursor = type.cursor
        }

        children.setAll(rect)
    }

}

enum class SelectionHandleType(val cursor: Cursor, val point: (Bounds) -> Point2D) {
    NW(Cursor.NW_RESIZE, {b -> Point2D(b.minX, b.minY)}),
    N(Cursor.N_RESIZE,   {b -> Point2D(b.centerX, b.minY) }),
    NE(Cursor.NE_RESIZE, {b -> Point2D(b.maxX, b.minY)}),
    SW(Cursor.SW_RESIZE, {b -> Point2D(b.minX, b.maxY)}),
    S(Cursor.S_RESIZE,   {b -> Point2D(b.centerX, b.maxY)}),
    SE(Cursor.SE_RESIZE, {b -> Point2D(b.maxX, b.maxY)}),
    W(Cursor.W_RESIZE,   {b -> Point2D(b.minX, b.centerY)}),
    E(Cursor.E_RESIZE,   {b -> Point2D(b.maxX, b.centerY)});
}




