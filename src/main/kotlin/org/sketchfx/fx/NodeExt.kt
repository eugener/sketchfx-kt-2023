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


    // Sets up a collection of bindings that will be bound to the scene when the node is added to a scene and unbound when removed from a scene.
    fun Node.setupSceneLifecycle(bindingLifecycles: Collection<BindingLifecycle>) {
        sceneProperty().addListener { _, _, newScene ->
            val op = if (newScene != null) BindingLifecycle::bind else BindingLifecycle::unbind
            bindingLifecycles.forEach(op)
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