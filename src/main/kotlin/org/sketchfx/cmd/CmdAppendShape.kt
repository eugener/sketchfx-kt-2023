package org.sketchfx.cmd

import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.infra.Command
import org.sketchfx.shape.Shape

data class CmdAppendShape( val shape: Shape ): Command<CanvasViewModel> {

    override fun run(context: CanvasViewModel) {
        context.shapes().add(shape)
        context.selection.set(shape)
    }

    override fun undo(context: CanvasViewModel) {
        context.shapes().remove(shape)
        context.selection.items().remove(shape)
    }

}
