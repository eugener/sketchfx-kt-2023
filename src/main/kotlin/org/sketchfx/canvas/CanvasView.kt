package org.sketchfx.canvas

import org.sketchfx.cmd.CmdRelocateShapes
import org.sketchfx.infra.Event
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.input.ScrollEvent
import javafx.scene.input.ZoomEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.scene.transform.Transform
import org.sketchfx.event.SelectionUpdate
import org.sketchfx.event.ShapeRelocated
import org.sketchfx.event.SelectionBounds


class CanvasView(val context: CanvasViewModel) : StackPane() {

    private val clip = Rectangle()
    private val shapeLayer = ShapeCanvasLayer()
    private val overlayLayer = OverlayCanvasLayer(context)
    private val catchAllLayer = CatchAllLayer(context)


    // canvas transform - calculated from scale and translation
    val canvasTransformProperty: ObjectProperty<Transform> =
        object : SimpleObjectProperty<Transform>(Transform.scale(1.0, 1.0)) {
            override fun invalidated() {
                val transform = get()
                shapeLayer.setTransform(transform)
                overlayLayer.setTransform(transform)
                //allows to adjust selection elements by scale
                context.fireSelectionChange()
            }
        }

    init {
        styleClass.setAll("canvas-view")
        setClip(clip)
        layoutBoundsProperty().addListener { _, _, bounds ->
            clip.width = bounds.width
            clip.height = bounds.height
        }


        // setup layers
        children.setAll(
            catchAllLayer,
            shapeLayer,
            overlayLayer,
        )

        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                addEventFilter(ZoomEvent.ANY, ::zoomHandler)
                addEventFilter(ScrollEvent.ANY, ::scrollHandler)

                context.eventBus.subscribe<SelectionUpdate>(::selectionAddHandler)
                context.eventBus.subscribe<ShapeRelocated>(::shapeRelocatedHandler)
                context.eventBus.subscribe<SelectionBounds>(::selectionBoundsHandler)

                context.boundsInParentProperty.bind(this.boundsInParentProperty())
                this.canvasTransformProperty.bind(context.transformProperty)
                Bindings.bindContentBidirectional(shapeLayer.shapes(), context.shapes())
            } else {
                removeEventFilter(ZoomEvent.ANY, ::zoomHandler)
                removeEventFilter(ScrollEvent.ANY, ::scrollHandler)

                context.eventBus.unsubscribe<SelectionUpdate>(::selectionAddHandler)
                context.eventBus.unsubscribe<ShapeRelocated>(::shapeRelocatedHandler)
                context.eventBus.unsubscribe<SelectionBounds>(::selectionBoundsHandler)

                context.boundsInParentProperty.unbind()
                this.canvasTransformProperty.unbind()
                Bindings.unbindContentBidirectional(shapeLayer.shapes(), context.shapes())
            }
        }

    }

    private fun selectionBoundsHandler(e: Event) {
        if (e is SelectionBounds) {
            val selectedShapes = shapeLayer.shapes().filter{it.boundsInParent.intersects(e.bounds)}
            context.selection.set(*selectedShapes.toTypedArray())
        }
    }

    private fun selectionAddHandler(e: Event) {
        if (e is SelectionUpdate) {
            if (!e.toggle) {
                if (!context.selection.contains(e.shape)) {
                    context.selection.set(e.shape)
                }
            } else {
                context.selection.toggle(e.shape)
            }
        }
    }

    private fun shapeRelocatedHandler(e: Event) {
        if (e is ShapeRelocated) {
            val cmd = CmdRelocateShapes(context.selection.items(), e.dx, e.dy, context)
            if (e.temp) {
                cmd.run()
            } else {
                context.commandManager.add(cmd)
            }
        }
    }

    private fun zoomHandler(e: ZoomEvent) {
        if (!e.zoomFactor.isNaN()) {
            context.scale = context.scale * e.zoomFactor
            e.consume()
        }
    }

    private fun scrollHandler(e: ScrollEvent) {
        if (e.isMetaDown) {
            context.scale = context.scale + (0.01 * e.deltaY)
            e.consume()
        } else {
            val (tx: Double, ty: Double) = context.translate
            context.translate = Pair(tx + e.deltaX, ty + e.deltaY)
            e.consume()
        }
    }

}