package org.sketchfx.shape

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Rectangle
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.canvas.MouseDragSupport
import org.sketchfx.fx.delegate
import java.util.*

data class Shape(
    private val bounds: Bounds,
    private val context: CanvasViewModel,
    private val buildShape: (Bounds) -> Collection<Node>
    ): Group() {

    val sid: String = UUID.randomUUID().toString()

    companion object {

        val defaultFill: Color = Color.LIGHTGREY
        val defaultStroke: Color = Color.DARKGREY

        private const val highlight: String = "#0D99FF"

        private val highlightFill: Color = Color.web(highlight, 0.05)
        private val highlightStroke: Color = Color.web(highlight)

        val selectionFill: Color = Color.TRANSPARENT
        val selectionStroke: Color = Color.web(highlight)

        private val selectionBandFill: Color = Color.web(highlight, 0.1)
        private val selectionBandStroke: Color = Color.web(highlight, 0.2)

        // common shapes
        fun rectangle(bounds: Bounds, context: CanvasViewModel): Shape {
                return Shape(bounds = bounds,context = context){
                    listOf(Rectangle(it.minX, it.minY, it.width, it.height))
                }.apply {
                    name = "Rectangle"
                }
        }

        fun oval(bounds:Bounds,context: CanvasViewModel): Shape {
            return Shape( bounds = bounds, context = context ) {
                listOf(Ellipse(it.centerX, it.centerY, it.width / 2, it.height / 2))
            }.apply {
                name = "Oval"
            }
        }


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

        fun selectionBand(bounds: Bounds, context: CanvasViewModel): Node {
            return Rectangle(bounds.minX, bounds.minY, bounds.width, bounds.height).apply {
                fill = selectionBandFill
                stroke = selectionBandStroke
                strokeWidth = 1 / context.scale
                isMouseTransparent = true
            }
        }

        fun basicShape( shapeType: BasicShapeType, bounds: Bounds, context: CanvasViewModel): Shape {
            return when(shapeType) {
                BasicShapeType.RECTANGLE -> rectangle(bounds, context)
                BasicShapeType.OVAL     -> oval(bounds, context)
            }
        }

        fun selectionBounds( bounds: Bounds, context: CanvasViewModel): Node {
            return Rectangle( bounds.minX, bounds.minY, bounds.width, bounds.height ).apply {
                fill = selectionFill
                stroke = selectionStroke
                strokeWidth = 1 / context.scale
                isMouseTransparent = true
            }
        }



    }

    var name        : String by SimpleStringProperty("").delegate()
    var fill        : Paint  by AttrProperty<Paint>(defaultFill).delegate()
    var stroke      : Paint  by AttrProperty<Paint>(defaultStroke).delegate()
    var strokeWidth : Double by AttrDoubleProperty(1.0).delegate()

    // shape dragging
    private val dragSupport = object: MouseDragSupport(this, context) {
        override fun onDrag(mousePosition: Point2D, temp: Boolean) {
            val delta = if (temp) currentDelta() else totalDelta()
            context.relocateSelection( this@Shape, delta.x, delta.y, temp)
        }
    }

    init {
        isPickOnBounds = false
        children.setAll( buildShape(bounds).map(::updateAttrs) )

        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                addEventHandler(MouseEvent.ANY, ::mouseEventHandler)
                dragSupport.enable()
            } else {
                removeEventHandler(MouseEvent.ANY, ::mouseEventHandler)
                dragSupport.disable()
            }
        }

    }

    override fun toString(): String {
       // return "$name :   $sid"
        return name
    }

    fun makeCopy( update: (Shape) -> Unit = { _ -> } ): Shape {
        return Shape(
            bounds = boundsInParent,
            context = context,
            buildShape = buildShape
        ).apply {
            name = "copy"
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

    private fun mouseEventHandler(event: MouseEvent) {
        when(event.eventType) {
            MouseEvent.MOUSE_ENTERED -> context.hoveredShape = this
            MouseEvent.MOUSE_EXITED  -> context.hoveredShape = null
            MouseEvent.MOUSE_PRESSED -> context.updateSelection(this, event.isShiftDown )
        }
    }

    private inner class AttrProperty<T>( value: T): SimpleObjectProperty<T>(value) {
        override fun invalidated(): Unit = children.forEach(::updateAttrs)
    }

    private inner class AttrDoubleProperty( value: Double): SimpleDoubleProperty(value) {
        override fun invalidated(): Unit = children.forEach(::updateAttrs)
    }
}