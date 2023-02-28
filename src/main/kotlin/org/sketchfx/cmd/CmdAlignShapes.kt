package org.sketchfx.cmd

import javafx.geometry.Point2D
import org.sketchfx.canvas.Alignment
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.infra.Command
import org.sketchfx.shape.Shape

class CmdAlignShapes(
    originalShapes: Collection<Shape>,
    private val alignment: Alignment): Command<CanvasViewModel> {

    private val shapes = originalShapes.toList()
    private val oldPositions = mutableListOf<Point2D>()

    override fun run(context: CanvasViewModel)  {
        if (shapes.size < 2 ) {
            throw IllegalArgumentException("Selection must contain at least 2 shapes")
        }

        oldPositions.clear()
        oldPositions.addAll(shapes.map { Point2D(it.boundsInParent.minX, it.boundsInParent.minY) })

        when (alignment) {
            Alignment.LEFT -> {
                val x = shapes.minOfOrNull { it.boundsInParent.minX } ?: return
                shapes.forEach {it.relocate(x, it.boundsInParent.minY)}
            }
            Alignment.RIGHT -> {
                val x = shapes.maxOfOrNull { it.boundsInParent.maxX } ?: return
                shapes.forEach {it.relocate(x-it.boundsInParent.width, it.boundsInParent.minY)}
            }
            else -> return
        }
    }

    override fun undo(context: CanvasViewModel) {
        shapes.zip(oldPositions).forEach { (shape, position) ->
            shape.relocate(position.x, position.y)
        }
    }

}