package org.sketchfx.fx

import javafx.beans.Observable
import javafx.collections.ObservableList
import javafx.scene.control.MultipleSelectionModel

class SelectionBinder<T>(private val fxModel: MultipleSelectionModel<T>, private val otherModel: ObservableList<T>) :
    ObservableBinder(fxModel, otherModel) {

    override fun asObservable(source: Any): Observable {
        return when (source) {
            fxModel -> fxModel.selectedItems
            otherModel -> otherModel
            else -> throw RuntimeException("Unknown observable source")
        }
    }

    override fun update(source: Any) {
        when (source) {
            fxModel -> otherModel.setAll(fxModel.selectedItems)
            otherModel -> {
                fxModel.clearSelection()
                otherModel.forEach { fxModel.select(it) }
            }

            else -> throw RuntimeException("Unknown source")
        }
    }

}






