package org.sketchfx.shape

import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.BoundsExt.union
import org.sketchfx.fx.NodeExt.setupSceneLifecycle
import org.sketchfx.fx.ShapeExt.bounds
import org.sketchfx.fx.bindingLifecycle

class SelectionBox(val context: CanvasViewModel) : Group() {

    private val shapeGroup = Group()
    private val selectionGroup = Group()
    private val selectionShape = Shape.selectionBounds(BoundingBox(0.0, 0.0, 0.0, 0.0), context)

    init {
        children.setAll(shapeGroup, selectionGroup)
        setupSceneLifecycle(
            context.selection.items().bindingLifecycle { _ -> update() },
            context.scaleProperty.bindingLifecycle { _, _, _ -> update() }
        )
    }

    private fun update() {

        if (context.selection.items().isEmpty()) {
            shapeGroup.children.clear()
            selectionGroup.children.clear()
            return
        }

        val (selectionShapes, selectionBounds) =
            context.selection.items().fold(Pair(listOf<Node>(), null as Bounds?)) { (selShapes, selBounds), shape ->
                val shapeBounds = shape.boundsInParent
                val b = selBounds?.let { selBounds.union(shapeBounds) } ?: shapeBounds
                Pair(selShapes + Shape.selection(shape), b)
            }

        shapeGroup.children.setAll(selectionShapes)

        if (selectionGroup.children.isEmpty()) {
            selectionGroup.children.setAll(selectionShape)
            selectionGroup.children.addAll(
                SelectionHandleType.values().map { SelectionHandle(it, selectionShape, context) }
            )
        }

        // has to be after binding handles to selectionShape
        // which allows the handles to be updated when selectionShape is updated
        selectionShape.bounds = selectionBounds!!
        selectionShape.strokeWidth = 1 / context.scale

    }

}

class SelectionHandle(
    private val type: SelectionHandleType,
    private val parent: Rectangle, val context: CanvasViewModel) : Group() {

    companion object {
        private const val handleSize: Double = 8.0
    }

//    private val dragSupport = object: MouseDragSupport(this, context) {
//        override fun onDrag(mousePosition: Point2D, temp: Boolean) {
//            when(type) {
//                SelectionHandleType.SE -> {
//                    val delta = currentDelta()
//                    println("delta: $delta")
//                    context.selection.items().forEach { shape ->
//                        val b = shape.boundsInParent
//                        shape.resize( b.width + delta.x, b.height + delta.y )
//                    }
//                }
//                else -> {}
//            }
//        }
//    }

    init {
        isMouseTransparent = false

        val handle = Rectangle(0.0, 0.0, handleSize, handleSize).apply {
            this.isResizable
            fill = Color.WHITE
            stroke = Shape.selectionStroke
            strokeWidth = 1.0
            this.cursor = type.cursor
        }

        // using group to allow for future expansion
        // e.g. change a handle's shape on the fly
        children.add(handle)

        setupSceneLifecycle(
            parent.boundsInParentProperty().bindingLifecycle { update() },
            context.scaleProperty.bindingLifecycle { _, _, _ -> update() }
        )

//        dragSupport.enable()
    }

    private fun update() {
        val handle = children.first() as Rectangle
        val p = type.point(parent.boundsInParent)
        val size = handleSize / context.scale
        val offset = size / 2
        handle.bounds = BoundingBox(p.x - offset, p.y - offset, size, size)
        handle.strokeWidth = 1 / context.scale
    }

}

enum class SelectionHandleType(val cursor: Cursor, val point: (Bounds) -> Point2D) {
    NW(Cursor.NW_RESIZE, { b -> Point2D(b.minX, b.minY) }),
    N(Cursor.N_RESIZE, { b -> Point2D(b.centerX, b.minY) }),
    NE(Cursor.NE_RESIZE, { b -> Point2D(b.maxX, b.minY) }),
    SW(Cursor.SW_RESIZE, { b -> Point2D(b.minX, b.maxY) }),
    S(Cursor.S_RESIZE, { b -> Point2D(b.centerX, b.maxY) }),
    SE(Cursor.SE_RESIZE, { b -> Point2D(b.maxX, b.maxY) }),
    W(Cursor.W_RESIZE, { b -> Point2D(b.minX, b.centerY) }),
    E(Cursor.E_RESIZE, { b -> Point2D(b.maxX, b.centerY) });
}




