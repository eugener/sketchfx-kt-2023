package org.sketchfx.cmd

import org.sketchfx.canvas.CanvasContext
import org.sketchfx.infra.Command
import org.sketchfx.shape.Shape

data class CmdAppendShape( val shape: Shape ): Command<CanvasContext> {

    override fun run(context: CanvasContext) {
        context.shapes().add(shape)
        context.selection.set(shape)
    }

    override fun undo(context: CanvasContext) {
        context.shapes().remove(shape)
        context.selection.items().remove(shape)
    }

}
