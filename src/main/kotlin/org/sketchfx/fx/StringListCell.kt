package org.sketchfx.fx

import javafx.scene.control.ListCell

class StringListCell<T>(className: String): ListCell<T>() {

    init {
        styleClass.addAll(className)
        minWidth = 0.0;
        prefWidth = 1.0;
    }

    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)
        text = item?.toString()
    }
}