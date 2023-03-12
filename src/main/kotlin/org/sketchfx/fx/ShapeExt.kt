package org.sketchfx.fx

import javafx.geometry.Bounds
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape

object ShapeExt {

    var Shape.bounds: Bounds
        get() = layoutBounds
        set(value) {
            when(this) {
                is Rectangle -> {
                    x = value.minX
                    y = value.minY
                    width = value.width
                    height = value.height
                }
                is javafx.scene.shape.Ellipse -> {
                    centerX = value.centerX
                    centerY = value.centerY
                    radiusX = value.width / 2
                    radiusY = value.height / 2
                }
                else -> {
                }
            }
        }
}