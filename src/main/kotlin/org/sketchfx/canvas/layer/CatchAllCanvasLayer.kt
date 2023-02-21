package org.sketchfx.canvas.layer

import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.input.MouseEvent
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.canvas.MouseDragMode
import org.sketchfx.canvas.MouseDragSupport
import org.sketchfx.canvas.NewShapeAvatar
import org.sketchfx.cmd.CmdAppendShape
import org.sketchfx.fx.NodeExt.setupSceneLifecycle
import org.sketchfx.fx.eventHandlerBindingLifecycle
import org.sketchfx.shape.Shape

class CatchAllCanvasLayer(private val context: CanvasViewModel): CanvasLayer() {

    private val dragSupport = object: MouseDragSupport(this, context) {

        override fun onDrag(mousePosition: Point2D, temp: Boolean) {

            when (context.mouseDragMode) {
                MouseDragMode.SELECTION -> {
                    context.selectionBand = if (temp) currentBounds() else null
                }

                MouseDragMode.BASIC_SHAPE_ADD -> {
                    val shape = Shape.basicShape(context.basicShapeToAdd, currentBounds(), context).apply {
                        // temp shape has to be mouse-transparent for selection to work properly
                        isMouseTransparent = temp
                    }
                    if (temp) {
                        context.newShapeAvatar = NewShapeAvatar(shape, mousePosition)
                    } else {
                        context.newShapeAvatar = null
                        context.mouseDragMode = MouseDragMode.SELECTION
                        context.commandManager.execute(CmdAppendShape(shape))
                    }

                }
            }

        }

        override fun onDragCancel() {
            when(context.mouseDragMode) {
                MouseDragMode.SELECTION -> context.selection.clear()
                MouseDragMode.BASIC_SHAPE_ADD -> context.mouseDragMode = MouseDragMode.SELECTION
            }

        }
    }

    private val mousePressHandler = EventHandler<MouseEvent> {context.selection.clear()}

    init {
        isPickOnBounds = true
        setupSceneLifecycle(
            dragSupport,
            this.eventHandlerBindingLifecycle(MouseEvent.MOUSE_PRESSED, mousePressHandler),
        )

        addEventHandler(MouseEvent.MOUSE_PRESSED) {
            context.selection.clear()
        }
    }

}