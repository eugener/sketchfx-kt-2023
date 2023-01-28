package org.sketchfx.infra

import javafx.beans.InvalidationListener
import javafx.collections.FXCollections

class SelectionModel<T> {

    private val selection = FXCollections.observableSet<T>()
    private var suppressChangeEvents = false

    fun items(): Set<T> = selection
    fun clear(): Unit = selection.clear()

    fun add(vararg items: T): Boolean = selection.addAll(items.asSequence())

    fun set(vararg items: T) {
        withChangeSuppression{ selection.clear() }
        selection.addAll(items.asSequence())
    }

    fun toggle(item: T) {
        if (!selection.remove(item)) {
            selection.add(item)
        }
    }

    fun contains(item: T): Boolean = selection.contains(item)

    fun onChange(action: (Set<T>) -> Unit) {
        selection.addListener( InvalidationListener {
            if (!suppressChangeEvents) {
                action(selection)
            }
        })
    }


    private fun <A> withChangeSuppression(block: () -> A): A {
        try {
            suppressChangeEvents = true
            return block()
        } finally {
            suppressChangeEvents = false
        }
    }

}