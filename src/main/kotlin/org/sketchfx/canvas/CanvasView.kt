package org.sketchfx.canvas

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Cursor
import javafx.scene.input.ScrollEvent
import javafx.scene.input.ZoomEvent
import javafx.scene.layout.StackPane
import javafx.scene.transform.Transform
import org.sketchfx.canvas.layer.CatchAllCanvasLayer
import org.sketchfx.canvas.layer.OverlayCanvasLayer
import org.sketchfx.canvas.layer.ShapeCanvasLayer
import org.sketchfx.fx.NodeExt.autoClipping
import org.sketchfx.fx.NodeExt.setupSceneLifecycle
import org.sketchfx.fx.bindingLifecycle
import org.sketchfx.fx.contentBindingLifecycle
import org.sketchfx.fx.eventFilterBindingLifecycle
import org.sketchfx.shape.BasicShapeType


class CanvasView(val context: CanvasViewModel) : StackPane()  {

    private val shapeLayer = ShapeCanvasLayer()
    private val overlayLayer = OverlayCanvasLayer(context)
    private val catchAllLayer = CatchAllCanvasLayer(context)

    // canvas transform - calculated from scale and translation
    private val canvasTransformProperty: ObjectProperty<Transform> =
        object : SimpleObjectProperty<Transform>(Transform.scale(1.0, 1.0)) {
            override fun invalidated() {
                val transform = get()
                shapeLayer.setTransform(transform)
                overlayLayer.setTransform(transform)
            }
        }

    private val mouseDragModeProperty: ObjectProperty<MouseDragMode> =
        object : SimpleObjectProperty<MouseDragMode>() {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            override fun invalidated() = when (get()) {
                MouseDragMode.SELECTION -> {
                    cursor = Cursor.DEFAULT
                }
                MouseDragMode.BASIC_SHAPE_ADD -> {
                    cursor = Cursor.CROSSHAIR
                }
            }
        }


    private val lifeCycleBindings = listOf(
        bindingLifecycle({ autoClipping = true }, { autoClipping = false }),
        eventFilterBindingLifecycle(ZoomEvent.ANY, ::zoomHandler),
        eventFilterBindingLifecycle(ScrollEvent.ANY, ::scrollHandler),
        context.boundsInParentProperty.bindingLifecycle(this.boundsInParentProperty()),
        canvasTransformProperty.bindingLifecycle(context.transformProperty),
        shapeLayer.shapes().contentBindingLifecycle(context.shapes()),
        mouseDragModeProperty.bindingLifecycle(context.mouseDragModeProperty)
    )

    init {

        styleClass.add("canvas-view")

        // setup layers
        children.setAll(
            catchAllLayer,
            shapeLayer,
            overlayLayer,
        )

        setupSceneLifecycle(lifeCycleBindings)

    }

    fun addBasicShape(basicShape: BasicShapeType) {
        context.mouseDragMode = MouseDragMode.BASIC_SHAPE_ADD
        context.basicShapeToAdd = basicShape
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