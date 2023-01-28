package org.sketchfx.fx

import javafx.event.EventHandler
import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import org.sketchfx.canvas.CanvasContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private typealias MouseEventHandler = EventHandler<MouseEvent>

abstract class MouseDragSupport( private val base: Node, private val context: CanvasContext) {

    private var startPos: Point2D? = null
    private var prevPos: Point2D? = null
    private var nextPos: Point2D? = null

    private val mousePressHandler = MouseEventHandler{ e ->
        prevPos = getPos(e)
        startPos = prevPos
        onDragStart()
    }

    private val mouseDragHandler = MouseEventHandler{ e ->
        if ( prevPos != null ) {
            nextPos = getPos(e)
            onDrag(true)
        }
        prevPos = nextPos
    }

    private val mouseReleaseHandler = MouseEventHandler{e ->
        if ( startPos != null ) {
            nextPos = getPos(e)
            onDrag(false)
        }
        startPos = null
        prevPos = null
        nextPos = null
    }

    private fun getPos( e: MouseEvent ): Point2D {
        return context.transform.inverseTransform(e.sceneX, e.sceneY)
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
     * @param temp    is true if the drag is not yet finished
     */
    abstract fun onDrag( temp: Boolean )

    fun onDragStart() {}

    protected fun currentDelta(): Point2D = nextPos!!.subtract(prevPos)
    protected fun totalDelta(): Point2D = nextPos!!.subtract(prevPos)

    protected fun currentBounds(): Bounds {
        val minX = min(startPos!!.x, nextPos!!.x)
        val minY = min(startPos!!.y, nextPos!!.y)
        val maxX = max(startPos!!.x, nextPos!!.x)
        val maxY = max(startPos!!.y, nextPos!!.y)
        return BoundingBox(minX, minY, abs(minX - maxX), abs(minY - maxY))
    }
}