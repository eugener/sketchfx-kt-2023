package org.sketchfx.shape

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
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
import org.sketchfx.event.SelectionUpdate
import org.sketchfx.event.ShapeHover
import org.sketchfx.event.ShapeRelocated
import org.sketchfx.fx.MouseDragSupport
import java.util.*

typealias ShapeBuilder = (Bounds) -> Collection<Node>


data class Shape(
    val name: String,
    private val bounds: Bounds,
    private val buildShape: ShapeBuilder,
    private val context: CanvasContext
    ): Group() {

    val sid: String = UUID.randomUUID().toString()

    companion object {

        val defaultFill: Color = Color.DARKGREY
        val defaultStroke: Color = Color.LIGHTGREY

        private const val highlight: String = "#0D99FF"

        private val highlightFill: Color = Color.web(highlight, 0.05)
        private val highlightStroke: Color = Color.web(highlight)

        private val selectionFill: Color = Color.TRANSPARENT
        private val selectionStroke: Color = Color.web(highlight)

        private val selectionBandFill: Color = Color.web(highlight, 0.1)
        private val selectionBandStroke: Color = Color.web(highlight, 0.2)

        private const val handleSize: Int = 8

        private val rectangleBuilder: ShapeBuilder = { b ->
            listOf( Rectangle( b.minX, b.minY, b.width, b.height ))
        }
        private val ovalBuilder: ShapeBuilder = { b ->
            listOf(Ellipse(b.centerX, b.centerY, b.width / 2, b.height / 2))
        }

        // common shapes
        fun rectangle(bounds:Bounds,context: CanvasContext) = Shape( name = "Rectangle", bounds = bounds, buildShape = rectangleBuilder, context = context)
        fun oval(bounds:Bounds,context: CanvasContext)      = Shape( name = "Oval", bounds = bounds, buildShape = ovalBuilder, context = context)

        // support shapes

        fun hover( source: Shape): Node {
            return source.makeCopy { s ->
                s.fill = highlightFill
                s.stroke = highlightStroke
                s.strokeWidth = 2.0
                s.isMouseTransparent = true
            }
        }

        fun selection(source: Shape): Node  {
            return source.makeCopy { s ->
                s.fill = Color.TRANSPARENT
                s.stroke = selectionStroke
                s.isMouseTransparent = true
            }
        }

        fun selectionBand(bounds: Bounds, context: CanvasContext): Node {
            return Rectangle(bounds.minX, bounds.minY, bounds.width, bounds.height).apply {
                fill = selectionBandFill
                stroke = selectionBandStroke
                strokeWidth = 1 / context.scale
                isMouseTransparent = true
            }
        }

        fun selectionBounds( bounds: Bounds, context: CanvasContext): Node {
            return Rectangle( bounds.minX, bounds.minY, bounds.width, bounds.height ).apply {
                fill = selectionFill
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

    private val fillColorProperty: ObjectProperty<Paint> =  AttrProperty<Paint>(defaultStroke)
    var fill : Paint
        get() = fillColorProperty.get()
        set(value) = fillColorProperty.set(value)


    private val strokeColorProperty: ObjectProperty<Paint> = AttrProperty<Paint>(defaultFill)
    var stroke : Paint
        get() = strokeColorProperty.get()
        set(value) = strokeColorProperty.set(value)

    private val strokeWidthProperty: DoubleProperty = SimpleDoubleProperty(1.0)
    var strokeWidth : Double
        get() = strokeWidthProperty.get()
        set(value) = strokeWidthProperty.set(value)

    private val dragSupport = object: MouseDragSupport(this, context) {
        override fun onDrag(temp: Boolean ){
            val delta = if (temp) currentDelta() else totalDelta()
            context.eventBus.publish(ShapeRelocated( this@Shape, delta.x, delta.y, temp))
        }
    }

    init {
        isPickOnBounds = false
        children.setAll( buildShape(bounds).map(::updateAttrs) )

        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                addEventHandler(MouseEvent.MOUSE_ENTERED, ::mouseEnterHandler)
                addEventHandler(MouseEvent.MOUSE_EXITED,  ::mouseExitHandler)
                addEventHandler(MouseEvent.MOUSE_PRESSED, ::mousePressHandler)
                dragSupport.enable()
            } else {
                removeEventHandler(MouseEvent.MOUSE_ENTERED,  ::mouseEnterHandler)
                removeEventHandler(MouseEvent.MOUSE_EXITED,   ::mouseExitHandler)
                removeEventHandler(MouseEvent.MOUSE_RELEASED, ::mousePressHandler)
                dragSupport.disable()
            }
        }


    }

    override fun toString(): String {
        return "$name :   $sid"
    }

    fun makeCopy( update: (Shape) -> Unit = { _ -> } ): Shape {
        return Shape( name = "copy", bounds = boundsInParent, buildShape = buildShape, context = context).apply {
            update(this)
        }
    }

    private fun updateAttrs(node: Node): Node {
        if (node is javafx.scene.shape.Shape) {
            node.fill   = fill
            node.stroke = stroke
            node.strokeWidth = strokeWidth / context.scale
        }
        return node
    }


    private fun mouseEnterHandler(event: MouseEvent) {
        context.eventBus.publish( ShapeHover( this, true ))
    }

    private fun mouseExitHandler(event: MouseEvent) {
        context.eventBus.publish( ShapeHover( this, false ))
    }

    private fun mousePressHandler(event: MouseEvent) {
        context.eventBus.publish( SelectionUpdate( this, event.isShiftDown ))
    }

    private inner class AttrProperty<T>( value: T): SimpleObjectProperty<T>(value) {
        override fun invalidated(): Unit = children.forEach(::updateAttrs)
    }
}