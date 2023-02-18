package org.sketchfx.canvas

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.scene.transform.Transform
import org.sketchfx.cmd.CmdAppendShape
import org.sketchfx.event.*
import org.sketchfx.fx.MouseDragSupport
import org.sketchfx.shape.Shape
import org.sketchfx.shape.SelectionBox as SelectionBoxShape

abstract class CanvasLayer : Region() {

    protected val group = Group()

    init {
        super.getChildren().add(group)

        // layer should be transparent but its shapes should not be
        isMouseTransparent = false
        isPickOnBounds = false
    }

    fun setTransform(transform: Transform){
        group.transforms.setAll(transform)
    }

}

class ShapeCanvasLayer : CanvasLayer() {
    fun shapes(): ObservableList<Shape> {
        @Suppress("UNCHECKED_CAST")
        return group.children as ObservableList<Shape>
    }

}

enum class MouseDragMode {
    SELECTION,
    BASIC_SHAPE_ADD
}

class CatchAllLayer(context: CanvasContext): CanvasLayer() {

    private val dragSupport = object: MouseDragSupport(this, context) {

        override fun onDrag(mousePosition: Point2D, temp: Boolean) {

            when (context.mouseDragMode) {
                MouseDragMode.SELECTION -> {
                    val event = SelectionBand(currentBounds(), temp)
                    context.eventBus.publish(event)
                }

                MouseDragMode.BASIC_SHAPE_ADD -> {
                    val shape = Shape.basicShape(context.basicShape, currentBounds(), context).apply {
                        // temp shape has to be mouse-transparent for selection to work properly
                        isMouseTransparent = temp
                    }
                    val event = BasicSelectionShapeAdd(shape, mousePosition, temp,)
                    context.eventBus.publish(event)
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


        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressHandler)
                dragSupport.enable()
            } else {
                removeEventHandler(MouseEvent.MOUSE_RELEASED, mousePressHandler)
                dragSupport.disable()
            }
        }

        addEventHandler(MouseEvent.MOUSE_PRESSED) {
            context.selection.clear()
        }
    }

}

class OverlayCanvasLayer(private val context: CanvasContext): CanvasLayer() {

    private val hoverGroup = Group()
    private val selectionGroup = Group()
    private val bandGroup = Group()

    private val sizeLabel = Label().apply {
        styleClass.add("shape-size-label")
        isMouseTransparent = true
    }

    init {
        group.children.addAll(hoverGroup, selectionGroup, bandGroup)
        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                context.eventBus.subscribe(::shapeHoverHandler)
                context.eventBus.subscribe(::selectionChangeHandler)
                context.eventBus.subscribe(::selectionRelocatedHandler)
                context.eventBus.subscribe(::selectionBandHandler)
                context.eventBus.subscribe(::basicShapeAddHandler)
            } else {
                context.eventBus.unsubscribe(::shapeHoverHandler)
                context.eventBus.unsubscribe(::selectionChangeHandler)
                context.eventBus.unsubscribe(::selectionRelocatedHandler)
                context.eventBus.unsubscribe(::selectionBandHandler)
                context.eventBus.unsubscribe(::basicShapeAddHandler)
            }
        }
    }

    private fun shapeHoverHandler(e: ShapeHover) {
        if (!e.on) {
            hideHover()

        } else if (!context.selection.contains(e.base)) {
            showHover(e.base)
        }
    }

    private fun selectionBandHandler( e: SelectionBand) {
       showBand(e.bounds, e.on)
    }

    private fun basicShapeAddHandler( e: BasicSelectionShapeAdd) {
        showBasicShape(e.shape, e.mousePosition, e.temp)
    }

    private fun showBasicShape(shape: Shape, mousePos: Point2D, temp: Boolean) {
        if (temp) {

            with(sizeLabel) {
                val shapeBounds = shape.boundsInParent
                text = "%.0f x %.0f".format(shapeBounds.width,shapeBounds.height)
                layoutX = mousePos.x + 5
                layoutY = mousePos.y + 5
            }
            bandGroup.children.setAll(shape, sizeLabel)
        } else {
            try {
                bandGroup.children.clear()
            } finally {
                context.mouseDragMode = MouseDragMode.SELECTION
                context.commandManager.execute(CmdAppendShape(shape, context))
            }

        }
    }

    private fun selectionChangeHandler(e: SelectionChanged) {
        showSelection(e.selection)
    }

    private fun selectionRelocatedHandler(e: SelectionRelocated) {
        showSelection(e.selection)
    }

    private fun hideHover() {
        hoverGroup.children.clear()
    }

    private fun showHover( shape: Shape) {
        hoverGroup.children.setAll(Shape.hover(shape))
    }

    private fun showSelection(selection: Collection<Shape>) {
        if (selection.isEmpty()) {
            selectionGroup.children.clear()
        } else {
            hideHover()
            selectionGroup.children.setAll( SelectionBoxShape(selection, context))
        }
    }

    private fun showBand(bounds: Bounds, on: Boolean ) {
        if (on) {
            bandGroup.children.setAll(Shape.selectionBand(bounds, context))
            context.eventBus.publish(SelectionBounds(bounds))
        } else {
            bandGroup.children.clear()
        }
    }

}