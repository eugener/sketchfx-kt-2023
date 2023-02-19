package org.sketchfx.canvas

import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.event.EventHandler
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min



private typealias MouseEventHandler = EventHandler<MouseEvent>
private typealias KeyEventHandler = EventHandler<KeyEvent>

abstract class MouseDragSupport(private val base: Node, private val context: CanvasViewModel) {

    private var startPos: Point2D? = null
    private var prevPos: Point2D? = null
    private var nextPos: Point2D? = null

    private val mouseEventHandler = MouseEventHandler{e ->
        when (e.eventType) {

            MouseEvent.MOUSE_PRESSED -> {
                startPos = getPos(e)
                prevPos = startPos
                nextPos = startPos
                base.requestFocus() // to receive key events
                onDragStart()
                e.consume()
            }

            MouseEvent.MOUSE_DRAGGED -> {
                startPos?.let {
                    val pos = getPos(e)
                    nextPos = pos
                    onDrag(pos,true)
                    prevPos = nextPos
                    e.consume()
                }
            }

            MouseEvent.MOUSE_RELEASED -> {
                startPos?.let {
                    val pos = getPos(e)
                    nextPos = pos
                    onDrag(pos,false)
                    prevPos = nextPos
                    e.consume()
                }
                startPos = null
                prevPos = null
                nextPos = null
            }
        }

    }

    private val keyPressHandler = KeyEventHandler { e ->
        if (e.code == KeyCode.ESCAPE) {
            onDragCancel()
            e.consume()
        }
    }

    private fun getPos( e: MouseEvent): Point2D {
        val localPos = base.localToParent(base.sceneToLocal(e.sceneX, e.sceneY))
        return context.transform.inverseTransform(localPos)
    }

    fun enable() {
        base.addEventHandler(MouseEvent.ANY, mouseEventHandler)
        base.addEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler)
    }

    fun disable() {
        base.removeEventHandler(MouseEvent.ANY, mouseEventHandler)
        base.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler)
    }

    /**
     * Override this method to handle mouse drag events
     * @param temp    is true if the drag is not yet finished
     */
    abstract fun onDrag(mousePosition: Point2D, temp: Boolean )

    /**
     * Override this method to handle mouse drag start event
     */
    open fun onDragStart() {}

    /**
     * Override this method to handle mouse drag cancel event
     */
    open fun onDragCancel() {}

    protected fun currentDelta(): Point2D = nextPos!!.subtract(prevPos).multiply(context.scale)
    protected fun totalDelta(): Point2D = nextPos!!.subtract(startPos).multiply(context.scale)

    protected fun currentBounds(): Bounds {
        val minX = min(startPos!!.x, nextPos!!.x)
        val minY = min(startPos!!.y, nextPos!!.y)
        val maxX = max(startPos!!.x, nextPos!!.x)
        val maxY = max(startPos!!.y, nextPos!!.y)
        return BoundingBox(minX, minY, abs(minX - maxX), abs(minY - maxY))
    }
}