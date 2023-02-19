package org.sketchfx.canvas

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.scene.transform.Transform
import org.sketchfx.cmd.CmdAppendShape
import org.sketchfx.event.BasicSelectionShapeAdd
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

class CatchAllLayer(private val context: CanvasViewModel): CanvasLayer() {

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
                    val event = BasicSelectionShapeAdd(shape, mousePosition, temp)
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

class OverlayCanvasLayer(private val context: CanvasViewModel): CanvasLayer() {

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
                context.hoveredShapeProperty.addListener(shapeHoverHandler)
                context.selection.items().addListener(selectionChangeHandler)
                context.selectionBandProperty.addListener(selectionBandHandler)
                context.eventBus.subscribe(::basicShapeAddHandler)
            } else {
                context.hoveredShapeProperty.removeListener(shapeHoverHandler)
                context.selection.items().removeListener(selectionChangeHandler)
                context.selectionBandProperty.removeListener(selectionBandHandler)
                context.eventBus.unsubscribe(::basicShapeAddHandler)
            }
        }
    }

    private val selectionBandHandler = ChangeListener{_, _, bounds ->
        if (bounds != null) {
            bandGroup.children.setAll(Shape.selectionBand(bounds, context))
            context.updateSelection(bounds)
        } else {
            bandGroup.children.clear()
        }
    }

    private val shapeHoverHandler: ChangeListener<Shape?> = ChangeListener { _, _, shape ->
        if (shape != null) {
            if (!context.selection.contains(shape)) {
                showHover(shape)
            }
        } else {
            hideHover()
        }
    }
    private val selectionChangeHandler = InvalidationListener {
        showSelection(context.selection.items())
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
                context.commandManager.execute(CmdAppendShape(shape))
            }

        }
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

}