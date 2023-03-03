package org.sketchfx.editor

import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.scene.layout.VBox
import org.sketchfx.canvas.Alignment
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.editor.props.ShapeBoundsPane
import org.sketchfx.fx.action.Action
import org.sketchfx.fx.action.asToolbar
import org.sketchfx.infra.Icons


class ShapePropertyPane(private val viewModel: CanvasViewModel) : VBox() {

    private val sizeBinding = Bindings.size(viewModel.selection.items())
    private val twoOrLessShapes = Bindings.lessThan(sizeBinding, 2)

    private val alignmentActions = Alignment.values().map(::alignAction)

    private val shapeBoundsPane = ShapeBoundsPane()


    init {
        styleClass.addAll("shape-property-pane", "small")
        children.setAll(
            alignmentActions.asToolbar(),
            shapeBoundsPane
        )

        viewModel.selection.items().addListener(InvalidationListener { _ ->
            shapeBoundsPane.boundShape = when (viewModel.selection.items().size) {
                1 -> viewModel.selection.items().first()
                else -> null
            }
            //viewModel.selection.items().firstOrNull()
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
            Alignment.LEFT -> Icons.ALIGN_LEFT
            Alignment.CENTER -> Icons.ALIGN_CENTER
            Alignment.RIGHT -> Icons.ALIGN_RIGHT
            Alignment.TOP -> Icons.ALIGN_TOP
            Alignment.MIDDLE -> Icons.ALIGN_MIDDLE
            Alignment.BOTTOM -> Icons.ALIGN_BOTTOM
        }
    }

}



