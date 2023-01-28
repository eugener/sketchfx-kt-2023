package org.sketchfx.cmd

import org.sketchfx.canvas.CanvasContext
import org.sketchfx.event.SelectionChanged
import org.sketchfx.infra.Command
import org.sketchfx.shape.Shape

data class CmdRelocateShapes(val shapes: Set<Shape>, val deltaX: Double, val deltaY: Double, val context: CanvasContext):
    Command {

    override fun run() = relocateShapes(deltaX, deltaY)

    override fun undo(): Unit = relocateShapes(-deltaX, -deltaY)

    private fun relocateShapes( x: Double, y: Double)  {
        shapes.forEach { s ->
            s.layoutX += x
            s.layoutY += y
        }
        context.eventBus.publish(SelectionChanged(shapes))
    }
}