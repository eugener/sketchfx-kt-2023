package org.sketchfx.cmd

import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.infra.Command
import org.sketchfx.shape.Shape


class CmdRelocateShapes(
    originalShapes: Collection<Shape>,
    private val deltaX: Double,
    private val deltaY: Double): Command<CanvasViewModel> {

    private val shapes = originalShapes.toList()

    override fun run(context: CanvasViewModel) = relocateShapes(deltaX, deltaY)

    override fun undo(context: CanvasViewModel): Unit = relocateShapes(-deltaX, -deltaY)

    private fun relocateShapes( x: Double, y: Double)  {
        shapes.forEach { it.relocate(it.boundsInParent.minX + x, it.boundsInParent.minY + y) }
    }
}