package org.sketchfx.editor

import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.sketchfx.canvas.Alignment
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.IntTextField
import org.sketchfx.fx.action.Action
import org.sketchfx.fx.action.asToolbar
import org.sketchfx.infra.Icons
import org.sketchfx.shape.Shape


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

    private val boundShape = SimpleObjectProperty<Shape?>()


    init {
        styleClass.addAll("shape-property-pane", "small")
        children.setAll(
            alignmentActions.asToolbar(),
            HBox(xPosTextField,yPosTextField).apply {
                styleClass.setAll("sub-pane")
            }
        )

        boundShape.addListener { _, oldShape, newShape ->
            oldShape?.let {
                xPosTextField.valueProperty.unbindBidirectional(it.layoutXProperty())
                yPosTextField.valueProperty.unbindBidirectional(it.layoutYProperty())
            }
            newShape?.let {
                xPosTextField.valueProperty.bindBidirectional(it.layoutXProperty())
                yPosTextField.valueProperty.bindBidirectional(it.layoutYProperty())
            }
        }

        viewModel.selection.items().addListener( InvalidationListener { _ ->
            boundShape.set(viewModel.selection.items().firstOrNull())
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



