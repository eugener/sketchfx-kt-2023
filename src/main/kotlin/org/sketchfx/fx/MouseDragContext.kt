package org.sketchfx.fx

import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import kotlin.math.abs
import kotlin.math.min

data class MouseDragContext(
    val startScenePos: Point2D,
    val trackingScenePos: Point2D,
    val nextTrackingScenePos: Point2D,
    val scale: Double) {

    fun currentDelta(): Point2D = nextTrackingScenePos.subtract(trackingScenePos).multiply(1 / scale)
    fun totalDelta(): Point2D = nextTrackingScenePos.subtract(startScenePos).multiply(1 / scale)

    fun currentBounds(): Bounds {
        val x = min(startScenePos.x, trackingScenePos.x)
        val y = min(startScenePos.y, trackingScenePos.y)
        val w = abs(startScenePos.x - nextTrackingScenePos.x)
        val h = abs(startScenePos.y - nextTrackingScenePos.y)
        return BoundingBox(x, y, w, h)
    }

}
