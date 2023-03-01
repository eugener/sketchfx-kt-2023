package org.sketchfx.fx

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter
import java.util.function.UnaryOperator

class IntTextField: TextField() {

    companion object {

        @JvmStatic
        private val intRegex = "-?[0-9]*".toRegex()


        @JvmStatic
        private val filter: UnaryOperator<TextFormatter.Change?> =
            UnaryOperator<TextFormatter.Change?> { change: TextFormatter.Change? ->
                val text: String = change?.text ?: ""
                if (text.matches(intRegex)) {
                    return@UnaryOperator change
                }
                null
            }
    }

    val valueProperty: IntegerProperty = SimpleIntegerProperty(0).apply {
        Bindings.bindBidirectional(textProperty(), this, IntStrConverter())
    }
    var value: Int by valueProperty.delegate()

    init {
        textFormatter = TextFormatter<String>(filter)
    }

}

class IntStrConverter: StringConverter<Number>() {

    override fun toString(value: Number?): String {
        return value?.toInt().toString()
    }

    override fun fromString(value: String?): Int {
        return value?.toInt() ?: 0
    }

}