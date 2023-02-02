package org.sketchfx.fx

import javafx.beans.InvalidationListener
import javafx.scene.control.MultipleSelectionModel
import org.sketchfx.infra.SelectionModel
import java.util.concurrent.atomic.AtomicBoolean

class SelectionBinder<T> (val selectionModel: MultipleSelectionModel<T>, val other: SelectionModel<T>) {

    private val selfUpdate = AtomicBoolean(false)

    private val selectionModelListener = InvalidationListener { _ ->
        selfUpdate.set(true)
        try {
            other.set(selectionModel.selectedItems)
        } finally {
            selfUpdate.set(false)
        }
    }

    fun bind() {

        other.onChange { sel ->
            if (!selfUpdate.get()) {
                selectionModel.clearSelection()
                sel.forEach { selectionModel.select(it) }
            }
        }

        selectionModel.selectedItems.addListener(selectionModelListener)

    }

    fun unbind() {
        selectionModel.selectedItems.removeListener(selectionModelListener)
    }


}