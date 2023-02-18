package org.sketchfx.cmd

import org.sketchfx.canvas.CanvasContext
import org.sketchfx.infra.Command
import org.sketchfx.shape.Shape

data class CmdAppendShape( val shape: Shape, val context: CanvasContext): Command {

    override fun run() {
        context.shapes().add(shape)
        context.selection.set(shape)
    }

    override fun undo() {
        context.shapes().remove(shape)
        context.selection.items().remove(shape)
    }

}
