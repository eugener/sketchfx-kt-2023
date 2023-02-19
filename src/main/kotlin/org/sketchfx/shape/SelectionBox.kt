package org.sketchfx.shape

import javafx.geometry.Bounds
import javafx.scene.Group
import javafx.scene.Node
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.BoundsExt.union

class SelectionBox(selection: Collection<Shape>, context: CanvasViewModel): Group() {

    init {

        val (selectionShapes, selectionBounds) = selection
        .fold( Pair(listOf<Node>(), null as Bounds?)){ (selShapes, selBounds), shape ->
            val shapeBounds = shape.boundsInParent

            if (selBounds == null) {
                Pair(selShapes + Shape.selection(shape), shapeBounds)
            } else {
                Pair(selShapes + Shape.selection(shape), selBounds.union(shapeBounds))
            }
        }

        children.setAll((selectionShapes + Shape.selectionBounds(selectionBounds!!, context)))

        children.addAll(
            Shape.selectionHandle(selectionBounds.minX, selectionBounds.minY, context),
            Shape.selectionHandle(selectionBounds.maxX, selectionBounds.maxY, context),
            Shape.selectionHandle(selectionBounds.maxX, selectionBounds.minY, context),
            Shape.selectionHandle(selectionBounds.minX, selectionBounds.maxY, context),
        )
    }


}
