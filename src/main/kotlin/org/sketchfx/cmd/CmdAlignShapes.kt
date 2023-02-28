package org.sketchfx.cmd

import javafx.geometry.Point2D
import org.sketchfx.canvas.Alignment
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.BoundsExt.union
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
                val minX = shapes.minOf { it.boundsInParent.minX }
                shapes.forEach {it.relocate(minX, it.boundsInParent.minY)}
            }
            Alignment.RIGHT -> {
                val maxX = shapes.maxOf { it.boundsInParent.maxX }
                shapes.forEach {it.relocate(maxX-it.boundsInParent.width, it.boundsInParent.minY)}
            }
            Alignment.CENTER -> {
                val centerX = shapes.map{it.boundsInParent}.reduce { b1, b2 -> b1.union(b2)}.centerX
                shapes.forEach {
                    it.relocate(centerX - it.boundsInParent.width/2, it.boundsInParent.minY )
                }
            }
            Alignment.TOP -> {
                val minY = shapes.minOf { it.boundsInParent.minY }
                shapes.forEach {it.relocate(it.boundsInParent.minX, minY)}
            }
            Alignment.BOTTOM -> {
                val maxY = shapes.maxOf { it.boundsInParent.maxY }
                shapes.forEach {it.relocate(it.boundsInParent.minX, maxY-it.boundsInParent.height)}
            }
            Alignment.MIDDLE -> {
                val centerY = shapes.map{it.boundsInParent}.reduce { b1, b2 -> b1.union(b2)}.centerY
                shapes.forEach {
                    it.relocate(it.boundsInParent.minX, centerY - it.boundsInParent.height/2 )
                }
            }
        }
    }

    override fun undo(context: CanvasViewModel) {
        shapes.zip(oldPositions).forEach { (shape, position) ->
            shape.relocate(position.x, position.y)
        }
    }

}