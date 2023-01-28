package org.sketchfx.fx

import org.sketchfx.canvas.CanvasContext
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.input.MouseEvent

private typealias MouseEventHandler = EventHandler<MouseEvent>

abstract class MouseDragSupport( private val base: Node, private val context: CanvasContext) {

    private var startPos: Point2D? = null
    private var trackingPos: Point2D? = null

    private val mousePressHandler = MouseEventHandler{ e ->
        trackingPos = Point2D(e.sceneX, e.sceneY)
        startPos = trackingPos
        onMouseDragStart()
    }

    private val mouseReleaseHandler = MouseEventHandler{e ->
        if ( startPos != null ) {
            onMouseDrag(buildDragContext(e), false)
        }
        trackingPos = null
    }

    private val mouseDragHandler = MouseEventHandler{ e ->
        if ( trackingPos != null ) {
            onMouseDrag(buildDragContext(e), true)
        }
        trackingPos = Point2D(e.sceneX, e.sceneY)
    }


    private fun buildDragContext( e: MouseEvent ): MouseDragContext {
        val pos = Point2D(e.sceneX, e.sceneY)
        return MouseDragContext(startPos!!, trackingPos!!, pos, context.scale)
    }

    fun enable() {
        base.addEventHandler(MouseEvent.MOUSE_PRESSED,  mousePressHandler)
        base.addEventHandler(MouseEvent.MOUSE_DRAGGED,  mouseDragHandler)
        base.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleaseHandler)
    }

    fun disable() {
        base.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressHandler)
        base.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDragHandler)
        base.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleaseHandler)
    }

    /** Override this method to handle mouse drag events
     *
     * @param ctx contains the start and end position of the drag
     * @param temp    is true if the drag is not yet finished
     */
    abstract fun onMouseDrag(ctx: MouseDragContext, temp: Boolean )

    fun onMouseDragStart() {}
}