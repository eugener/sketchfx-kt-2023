package org.sketchfx.cmd

import org.sketchfx.canvas.CanvasContext
import org.sketchfx.infra.Command
import org.sketchfx.shape.Shape

data class CmdRelocateShapes(val shapes: Collection<Shape>, val deltaX: Double, val deltaY: Double): Command<CanvasContext> {

    override fun run(context: CanvasContext) = relocateShapes(deltaX, deltaY, context)

    override fun undo(context: CanvasContext): Unit = relocateShapes(-deltaX, -deltaY, context)

    private fun relocateShapes( x: Double, y: Double,context: CanvasContext)  {
        shapes.forEach { s ->
            s.layoutX += x
            s.layoutY += y
        }
        context.fireSelectionRelocated()
    }
}