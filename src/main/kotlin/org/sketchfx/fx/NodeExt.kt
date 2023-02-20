package org.sketchfx.fx

import javafx.beans.value.ChangeListener
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.shape.Rectangle

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

    fun Node.setupSceneLifecycle(lifecycleBindings: Collection<Binding>) {
        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                lifecycleBindings.forEach(Binding::bind)
            } else {
                lifecycleBindings.forEach(Binding::unbind)
            }
        }
    }

//    interface SceneLifecycle {
//        fun onSceneSet()
//        fun onSceneUnset()
//    }


//    fun Node.setupSceneLifecycle(node: SceneLifecycle) {
//        sceneProperty().addListener { _, _, newScene ->
//            if (newScene != null) {
//                node.onSceneSet()
//            } else {
//                node.onSceneUnset()
//            }
//        }
//    }


//    fun Node.setupSceneLifecycle() {
//        if ( this is SceneLifecycle) {
//            setupSceneLifecycle(this)
//        } else {
//            throw IllegalArgumentException("Node must implement SceneLifecycle")
//        }
//    }
}