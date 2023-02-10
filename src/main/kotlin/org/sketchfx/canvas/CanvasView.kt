package org.sketchfx.canvas

import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.input.ScrollEvent
import javafx.scene.input.ZoomEvent
import javafx.scene.layout.StackPane
import javafx.scene.transform.Transform
import org.sketchfx.fx.NodeExt.autoClipping


class CanvasView(val context: CanvasViewModel) : StackPane()  {

    private val shapeLayer = ShapeCanvasLayer()
    private val overlayLayer = OverlayCanvasLayer(context)
    private val catchAllLayer = CatchAllLayer(context)

    // canvas transform - calculated from scale and translation
    private val canvasTransformProperty: ObjectProperty<Transform> =
        object : SimpleObjectProperty<Transform>(Transform.scale(1.0, 1.0)) {
            override fun invalidated() {
                val transform = get()
                shapeLayer.setTransform(transform)
                overlayLayer.setTransform(transform)
                //allows to adjust selection elements by scale
                context.fireSelectionRelocated()
            }
        }

    init {

        styleClass.add("canvas-view")

        // setup layers
        children.setAll(
            catchAllLayer,
            shapeLayer,
            overlayLayer,
        )

        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                autoClipping = true
                addEventFilter(ZoomEvent.ANY, ::zoomHandler)
                addEventFilter(ScrollEvent.ANY, ::scrollHandler)

                context.boundsInParentProperty.bind(this.boundsInParentProperty())
                this.canvasTransformProperty.bind(context.transformProperty)
                Bindings.bindContent(shapeLayer.shapes(), context.shapes())
            } else {
                autoClipping = false
                removeEventFilter(ZoomEvent.ANY, ::zoomHandler)
                removeEventFilter(ScrollEvent.ANY, ::scrollHandler)

                context.boundsInParentProperty.unbind()
                this.canvasTransformProperty.unbind()
                Bindings.unbindContent(shapeLayer.shapes(), context.shapes())
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