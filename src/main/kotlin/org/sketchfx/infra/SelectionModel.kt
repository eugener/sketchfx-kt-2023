package org.sketchfx.infra

import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.util.Callback

class SelectionModel<T>( extractor: Callback<T, Array<Observable>>) {

    private val selection = FXCollections.observableArrayList<T>(extractor)
    private var suppressChangeEvents = false

    fun items(): ObservableList<T> = selection
    fun clear(): Unit = selection.clear()

    fun add(vararg items: T): Boolean = selection.addAll(items.asSequence())

    fun set(vararg items: T) {
        selection.setAll(*items)
    }

    fun set(items: Collection<T>) {
        selection.setAll(items)
    }

    fun toggle(item: T) {
        if (!selection.remove(item)) {
            selection.add(item)
        }
    }

    fun contains(item: T): Boolean = selection.contains(item)


//    private fun <A> withChangeSuppression(block: () -> A): A {
//        try {
//            suppressChangeEvents = true
//            return block()
//        } finally {
//            suppressChangeEvents = false
//        }
//    }

}


