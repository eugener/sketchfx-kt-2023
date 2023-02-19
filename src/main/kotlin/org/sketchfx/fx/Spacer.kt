package org.sketchfx.fx

import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class Spacer {

    companion object {
        fun horizontal() = Region().apply {
            HBox.setHgrow(this, Priority.SOMETIMES)
        }
    }

}