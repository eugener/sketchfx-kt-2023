package org.sketchfx.shape

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Bounds
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Rectangle
import org.sketchfx.canvas.CanvasContext
import org.sketchfx.event.SelectionAdd
import org.sketchfx.event.ShapeHover
import org.sketchfx.event.ShapeRelocated
import org.sketchfx.fx.MouseDragSupport
import java.util.*

typealias ShapeBuilder = (Bounds) -> Collection<Node>
typealias MouseEventHandler = EventHandler<MouseEvent>


data class Shape(
    val sid: String = UUID.randomUUID().toString(),
    private val bounds: Bounds,
    private val buildShape: ShapeBuilder,
    private val context: CanvasContext
    ): Group() {

    companion object {

        val defaultFill: Color = Color.DARKGREY
        val defaultStroke: Color = Color.LIGHTGREY

        private val highlight: String = "#0D99FF"

        val highlightFill: Color = Color.web(highlight, 0.05)
        val highlightStroke: Color = Color.web(highlight)

        val selectionStroke: Color = Color.web(highlight)

        val handleSize: Int = 8

        private val rectangleBuilder: ShapeBuilder = { b ->
            listOf( Rectangle( b.minX, b.minY, b.width, b.height ))
        }
        private val ovalBuilder: ShapeBuilder = { b ->
            listOf(Ellipse(b.centerX, b.centerY, b.width / 2, b.height / 2))
        }

        // common shapes
        fun rectangle(bounds:Bounds,context: CanvasContext) = Shape(bounds = bounds, buildShape = rectangleBuilder, context = context)
        fun oval(bounds:Bounds,context: CanvasContext)      = Shape(bounds = bounds, buildShape = ovalBuilder, context = context)

        // support shapes

        fun hover( source: Shape): Node {
            return source.makeCopy { s ->
                s.fillColorProperty.set(highlightFill)
                s.strokeColorProperty.set(highlightStroke)
                s.isMouseTransparent = true
            }
        }

        fun selection(source: Shape): Node  {
            return source.makeCopy { s ->
                s.fillColorProperty.set(Color.TRANSPARENT)
                s.strokeColorProperty.set(selectionStroke)
                s.isMouseTransparent = true
            }
        }

        fun selectionBand(bounds: Bounds, context: CanvasContext): Node {
            return Rectangle(bounds.minX, bounds.minY, bounds.width, bounds.height).apply {
                fill = highlightFill
                stroke = highlightStroke
                strokeWidth = 1 / context.scale
                isMouseTransparent = true
            }
        }

        fun selectionBounds( bounds: Bounds, context: CanvasContext): Node {
            return Rectangle( bounds.minX, bounds.minY, bounds.width, bounds.height ).apply {
                fill = Color.TRANSPARENT
                stroke = selectionStroke
                strokeWidth = 1 / context.scale
                isMouseTransparent = true
            }
        }

        fun selectionHandle(x: Double, y: Double, context: CanvasContext): Node {

            val size = handleSize / context.scale
            val offset = size / 2
            val lineWidth = 1/ context.scale

            return Rectangle(x-offset, y-offset, size, size).apply{
                fill = Color.WHITE
                stroke = selectionStroke
                strokeWidth = lineWidth
            }
        }

    }

    val fillColorProperty: ObjectProperty<Paint> =  AttrProperty<Paint>(defaultStroke)
    val strokeColorProperty: ObjectProperty<Paint> = AttrProperty<Paint>(defaultFill)

    private val dragSupport = object: MouseDragSupport(this, context) {
        override fun onDrag(temp: Boolean ){
            val delta = if (temp) currentDelta() else totalDelta()
            context.eventBus.publish(ShapeRelocated( this@Shape, delta.x, delta.y, temp))
        }
    }

    init {
        isPickOnBounds = false
        children.setAll( buildShape(bounds).map(::updateAttrs) )

        val mouseEnterHandler = MouseEventHandler{context.eventBus.publish( ShapeHover( this, true ))}
        val mouseExitHandler  = MouseEventHandler{context.eventBus.publish( ShapeHover( this, false ))}
        val mousePressHandler = MouseEventHandler{context.eventBus.publish( SelectionAdd( this, it.isShiftDown ))}

        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnterHandler)
                addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitHandler)
                addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressHandler)
                dragSupport.enable()
            } else {
                removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnterHandler)
                removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitHandler)
                removeEventHandler(MouseEvent.MOUSE_RELEASED, mousePressHandler)
                dragSupport.disable()
            }
        }


    }

    fun makeCopy( update: (Shape) -> Unit = { _ -> } ): Shape {
        return Shape( bounds = boundsInParent, buildShape = buildShape, context = context).apply {
            update(this)
        }
    }

    private fun updateAttrs(node: Node): Node {
        when(node) {
            is javafx.scene.shape.Shape -> {
                node.fill = fillColorProperty.get()
                node.stroke = strokeColorProperty.get()
            }
        }
        return node
    }

    private inner class AttrProperty<T>( value: T): SimpleObjectProperty<T>(value) {
        override fun invalidated(): Unit = children.forEach(::updateAttrs)
    }
}