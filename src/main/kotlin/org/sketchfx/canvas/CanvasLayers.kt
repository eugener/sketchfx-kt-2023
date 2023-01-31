package org.sketchfx.canvas

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Bounds
import javafx.scene.Group
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.scene.transform.Transform
import org.sketchfx.event.SelectionBand
import org.sketchfx.event.SelectionBounds
import org.sketchfx.event.SelectionChanged
import org.sketchfx.event.ShapeHover
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

class CatchAllLayer(context: CanvasContext): CanvasLayer() {

    private val dragSupport = object: MouseDragSupport(this, context) {
        override fun onDrag(temp: Boolean){
            context.eventBus.publish(SelectionBand(currentBounds(), temp))
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

    init {
        group.children.addAll(hoverGroup, selectionGroup, bandGroup)
        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                context.eventBus.subscribe(::shapeHoverHandler)
                context.eventBus.subscribe(::selectionChangeHandler)
                context.eventBus.subscribe(::selectionBandHandler)
            } else {
                context.eventBus.unsubscribe(::shapeHoverHandler)
                context.eventBus.unsubscribe(::selectionChangeHandler)
                context.eventBus.unsubscribe(::selectionBandHandler)
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

    private fun selectionChangeHandler(e: SelectionChanged) {
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