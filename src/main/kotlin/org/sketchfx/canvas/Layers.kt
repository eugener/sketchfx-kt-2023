package org.sketchfx.canvas

import org.sketchfx.event.SelectionBand
import org.sketchfx.event.SelectionChanged
import org.sketchfx.event.ShapeHover
import org.sketchfx.infra.Event
import javafx.collections.ObservableList
import javafx.geometry.Bounds
import javafx.scene.Group
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.scene.transform.Transform
import org.sketchfx.shape.SelectionShape
import org.sketchfx.shape.Shape

abstract class CanvasLayer(): Region() {

    protected val group = Group()

//    override protected fun getChildren: ObservableList[Node] = super.getChildren

    init {
        super.getChildren().add(group)

        // layer should be transparent but its shapes should not be
        isMouseTransparent = false
        isPickOnBounds = false
    }

    fun shapes(): ObservableList<Shape> {
        @Suppress("UNCHECKED_CAST")
        return group.children as ObservableList<Shape>
    }

    fun setTransform(transform: Transform){
        group.transforms.setAll(transform)
    }

}

class ShapeCanvasLayer(): CanvasLayer()

class CatchAllLayer(context: CanvasContext): CanvasLayer() {

    init {
        isPickOnBounds = true
        //    setOnMouseClicked( _ => context.selection.clear())

        addEventHandler(MouseEvent.MOUSE_PRESSED) {
            context.selection.clear()
        }
    }



//    override def onMouseDragStart(): Unit = {
//         println("onMouseDragStart")
//         context.selection.clear()
//    }
//
//    override def onMouseDrag(ctx: MouseDragContext, temp: Boolean): Unit = {
//        println(s"onMouseDrag: $ctx, temp: $temp")
//        context.eventBus.publish(SelectionBand(ctx.currentBounds, temp))
//    }

}

class OverlayCanvasLayer(private val context: CanvasContext): CanvasLayer() {

    private val hoverGroup = Group()
    private val selectionGroup = Group()
    private val bandGroup = Group()


    init {
        group.children.addAll(hoverGroup, selectionGroup, bandGroup)
        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                context.eventBus.subscribe<ShapeHover>(::shapeHoverHandler)
                context.eventBus.subscribe<SelectionChanged>(::selectionChangeHandler)
                context.eventBus.subscribe<SelectionBand>(::selectionBandHandler)
            } else {
                context.eventBus.unsubscribe<ShapeHover>(::shapeHoverHandler)
                context.eventBus.unsubscribe<SelectionChanged>(::selectionChangeHandler)
                context.eventBus.unsubscribe<SelectionBand>(::selectionBandHandler)
            }
        }
    }

    private fun shapeHoverHandler( e: Event) {
        when (e) {
            is ShapeHover -> {
                if (!e.on) {
                    hideHover()

                } else if (!context.selection.contains(e.base)) {
                    showHover(e.base)
                }
            }
        }
    }

    private fun selectionBandHandler( e: Event) {
        when (e) {
            is SelectionBand -> showBand(e.bounds, e.on)
        }
    }

    private fun selectionChangeHandler(e: Event) {
        when (e) {
            is SelectionChanged -> showSelection(e.selection)
        }
    }

    private fun hideHover() {
        hoverGroup.children.clear()
    }

    private fun showHover( shape: Shape) {
        hoverGroup.children.setAll(Shape.hover(shape))
    }

    private fun showSelection(selection: Set<Shape>) {
        if (selection.isEmpty()) {
            selectionGroup.children.clear()
        } else {
            hideHover()
            selectionGroup.children.setAll( SelectionShape(selection, context))
        }
    }

    private fun showBand(bounds: Bounds, on: Boolean ) {
        if (on)
            bandGroup.children.setAll(Shape.selectionBand(bounds, context))
        else
            bandGroup.children.clear()
    }

}