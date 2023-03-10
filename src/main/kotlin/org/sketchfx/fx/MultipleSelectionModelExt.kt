package org.sketchfx.fx

import javafx.collections.ObservableList
import javafx.scene.control.MultipleSelectionModel
import java.util.*

object MultipleSelectionModelExt {

    private val props = WeakHashMap<String, ObservableBinder>()

    private fun <T> MultipleSelectionModel<T>.propID(list: ObservableList<T>) = "selection-binder-${this.hashCode()}-${list.hashCode()}"

    private fun <T> MultipleSelectionModel<T>.bindBidirectional(list: ObservableList<T>) {
        val binder = props.getOrPut(propID(list)) { SelectionBinder(this, list) }
        binder.bind()
    }

    private fun <T> MultipleSelectionModel<T>.unbindBidirectional(list: ObservableList<T>) {
        props.remove(propID(list))?.unbind()
    }

    fun <T> MultipleSelectionModel<T>.bidirectionalBindingLifecycle(list: ObservableList<T>): BindingLifecycle {
        return bindingLifecycle(
            bind = { bindBidirectional(list) },
            unbind = { unbindBidirectional(list) }
        )
    }
}