package org.sketchfx.fx

import javafx.beans.value.ChangeListener
import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.shape.Rectangle
import java.util.*
import kotlin.math.max
import kotlin.math.min

object BoundsExt {

    fun Bounds.union(other: Bounds?): Bounds {
        if (other == null) return this
        val minX = min(this.minX, other.minX)
        val minY = min(this.minY, other.minY)
        val minZ = min(this.minZ, other.minZ)
        val maxX = max(this.maxX, other.maxX)
        val maxY = max(this.maxY, other.maxY)
        val maxZ = max(this.maxZ, other.maxZ)
        return BoundingBox(minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ)
    }

}

object NodeExt {

    private const val clipperId = "clipper"

    fun Node.enableAutoClipping() {
        val clip = Rectangle()
        val boundsListener = ChangeListener<Bounds> { _, _, bounds ->
            clip.width  = bounds.width
            clip.height = bounds.height
        }
        setClip(clip)
        layoutBoundsProperty().addListener(boundsListener)
        properties[clipperId] = boundsListener
    }

    fun Node.disableAutoClipping() {

        properties.remove(clipperId)?.apply{
            setClip(null)
            @Suppress("UNCHECKED_CAST")
            layoutBoundsProperty().removeListener(this as ChangeListener<Bounds>)
        }


    }

}