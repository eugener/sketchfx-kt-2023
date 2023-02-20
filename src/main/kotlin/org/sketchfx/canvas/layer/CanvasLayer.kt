package org.sketchfx.canvas.layer

import javafx.collections.ObservableList
import javafx.scene.Group
import javafx.scene.layout.Region
import javafx.scene.transform.Transform
import org.sketchfx.shape.Shape

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

