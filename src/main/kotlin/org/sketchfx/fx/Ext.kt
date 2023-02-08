package org.sketchfx.fx

import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.control.MultipleSelectionModel
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

    var Node.autoClipping: Boolean
        get() = properties.containsKey(clipperId)
        set(value) {
            if (value) enableAutoClipping() else disableAutoClipping()
        }

    private fun Node.enableAutoClipping() {
        val clipShape = Rectangle()
        val boundsListener = ChangeListener<Bounds> { _, _, bounds ->
            clipShape.width  = bounds.width
            clipShape.height = bounds.height
        }
        clip = clipShape
        layoutBoundsProperty().addListener(boundsListener)
        properties[clipperId] = boundsListener
    }

    private fun Node.disableAutoClipping() {

        properties.remove(clipperId)?.apply{
            clip = null
            @Suppress("UNCHECKED_CAST")
            layoutBoundsProperty().removeListener(this as ChangeListener<Bounds>)
        }

    }

}

object MultipleSelectionModelExt {

    private val props = WeakHashMap<String, ObservableBinder>()

    private fun <T> MultipleSelectionModel<T>.id(list: ObservableList<T>) = "${this.hashCode()}${list.hashCode()}"

    fun <T> MultipleSelectionModel<T>.bindBidirectional(list: ObservableList<T>) {
        val binder = props.getOrPut(id(list)) { SelectionBinder(this, list) }
        binder.bind()
    }

    fun <T> MultipleSelectionModel<T>.unbindBidirectional(list: ObservableList<T>) {
        props.remove(id(list))?.unbind()
    }
}