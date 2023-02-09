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
        set(enable) {
            if (enable) {
                clip = Rectangle()
                val boundsListener = ChangeListener<Bounds> { _, _, bounds ->
                    (clip as Rectangle).width = bounds.width
                    (clip as Rectangle).height = bounds.height
                }
                layoutBoundsProperty().addListener(boundsListener)
                properties[clipperId] = boundsListener
            } else {
                properties.remove(clipperId)?.apply {
                    @Suppress("UNCHECKED_CAST")
                    layoutBoundsProperty().removeListener(this as ChangeListener<Bounds>)
                    clip = null
                }
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