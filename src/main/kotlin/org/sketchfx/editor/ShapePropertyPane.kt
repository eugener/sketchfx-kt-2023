package org.sketchfx.editor

import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.sketchfx.canvas.Alignment
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.IntTextField
import org.sketchfx.fx.action.Action
import org.sketchfx.fx.action.asToolbar
import org.sketchfx.infra.Icons


class ShapePropertyPane(private val viewModel: CanvasViewModel) : VBox() {

    private val sizeBinding = Bindings.size(viewModel.selection.items())
    private val twoOrLessShapes = Bindings.lessThan(sizeBinding, 2)
    private val notSingleShape = Bindings.notEqual(1, sizeBinding)

    private val alignmentActions = Alignment.values().map(::alignAction)
    private val xPosTextField = IntTextField().apply {
        styleClass.add("prop-field")
        disableProperty().bind(notSingleShape)
    }
    private val yPosTextField = IntTextField().apply{
        styleClass.add("prop-field")
        disableProperty().bind(notSingleShape)
    }

    private val doubleConverter = DoubleStrConverter()

    init {
        styleClass.addAll("shape-property-pane", "small")
        children.setAll(
            alignmentActions.asToolbar(),
            HBox(xPosTextField,yPosTextField).apply {
                styleClass.setAll("sub-pane")
            }
        )

        viewModel.selection.items().addListener( InvalidationListener { _ ->
            val selectedShapes = viewModel.selection.items()
            if (selectedShapes.size >= 1) {
                val shape = selectedShapes[0]
                xPosTextField.textProperty().bindBidirectional(shape.layoutXProperty(), doubleConverter )
                yPosTextField.textProperty().bindBidirectional(shape.layoutYProperty(), doubleConverter )
            }
        })
    }

    private fun alignAction(alignment: Alignment): Action {
        return Action.of(buildGraphic = { alignment.getIcon().graphic() }).apply {
            disabledProperty.bind(twoOrLessShapes)
            action = { viewModel.alignSelection(alignment) }
        }
    }

    private fun Alignment.getIcon(): Icons {
        return when (this) {
            Alignment.LEFT   -> Icons.ALIGN_LEFT
            Alignment.CENTER -> Icons.ALIGN_CENTER
            Alignment.RIGHT  -> Icons.ALIGN_RIGHT
            Alignment.TOP    -> Icons.ALIGN_TOP
            Alignment.MIDDLE -> Icons.ALIGN_MIDDLE
            Alignment.BOTTOM -> Icons.ALIGN_BOTTOM
        }
    }

}

class DoubleStrConverter: StringConverter<Number>() {

    override fun toString(value: Number?): String {
        return value?.toInt().toString()
    }

    override fun fromString(value: String?): Double {
        return value?.toInt()?.toDouble() ?: 0.0
    }

}

