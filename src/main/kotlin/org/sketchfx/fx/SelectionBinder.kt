package org.sketchfx.fx

import javafx.beans.InvalidationListener
import javafx.collections.ObservableList
import javafx.scene.control.MultipleSelectionModel
import org.sketchfx.infra.RaceConditionResolver

class SelectionBinder<T> (private val fxModel: MultipleSelectionModel<T>, private val otherModel: ObservableList<T>): RaceConditionResolver() {

    private val selectionModelListener = InvalidationListener { _ ->
        guardLeft {
            otherModel.setAll(fxModel.selectedItems)
        }
    }

    private val otherListener = InvalidationListener { _ ->
        guardRight {
            fxModel.clearSelection()
            otherModel.forEach { fxModel.select(it) }
        }
    }

    fun bind() {
        otherModel.addListener(otherListener)
        fxModel.selectedItems.addListener(selectionModelListener)
    }

    fun unbind() {
        otherModel.removeListener(otherListener)
        fxModel.selectedItems.removeListener(selectionModelListener)
    }


}


