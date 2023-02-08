package org.sketchfx.fx

import javafx.beans.InvalidationListener
import javafx.scene.control.MultipleSelectionModel
import org.sketchfx.infra.RaceConditionResolver
import org.sketchfx.infra.SelectionModel

class SelectionBinder<T> (private val fxModel: MultipleSelectionModel<T>, private val sketchModel: SelectionModel<T>): RaceConditionResolver() {

    private val selectionModelListener = InvalidationListener { _ ->
        guardLeft {
            sketchModel.set(fxModel.selectedItems)
        }
    }

    fun bind() {

        sketchModel.onChange { sel ->
            guardRight {
                fxModel.clearSelection()
                sel.forEach { fxModel.select(it) }
            }
        }

        fxModel.selectedItems.addListener(selectionModelListener)

    }

    fun unbind() {
        fxModel.selectedItems.removeListener(selectionModelListener)
    }


}

