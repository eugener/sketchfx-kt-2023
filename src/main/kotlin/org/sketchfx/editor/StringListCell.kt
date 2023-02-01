package org.sketchfx.editor

import javafx.scene.control.ListCell

class StringListCell<T>(className: String): ListCell<T>() {

    init {
        styleClass.addAll(className)
    }

    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)
        text = item?.toString()
    }
}