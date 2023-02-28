package org.sketchfx.fx

import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import java.util.function.UnaryOperator

class IntTextField: TextField() {

    companion object {
        @JvmStatic
        private val filter: UnaryOperator<TextFormatter.Change?> =
            UnaryOperator<TextFormatter.Change?> { change: TextFormatter.Change? ->
                val text: String = change?.text ?: ""
                if (text.matches("[0-9]*".toRegex())) {
                    return@UnaryOperator change
                }
                null
            }
    }

    init {
        textFormatter = TextFormatter<String>(filter)
    }

}