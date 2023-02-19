package org.sketchfx.cmd

import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.infra.Command
import org.sketchfx.shape.Shape

data class CmdRelocateShapes(val shapes: Collection<Shape>, val deltaX: Double, val deltaY: Double): Command<CanvasViewModel> {

    override fun run(context: CanvasViewModel) = relocateShapes(deltaX, deltaY)

    override fun undo(context: CanvasViewModel): Unit = relocateShapes(-deltaX, -deltaY)

    private fun relocateShapes( x: Double, y: Double)  {
        shapes.forEach { s ->
            s.layoutX += x
            s.layoutY += y
        }
    }
}