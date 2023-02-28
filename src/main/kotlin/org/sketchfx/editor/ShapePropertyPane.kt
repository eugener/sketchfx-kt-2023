package org.sketchfx.editor

import javafx.beans.binding.Bindings
import javafx.scene.layout.VBox
import org.sketchfx.canvas.Alignment
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.action.Action
import org.sketchfx.fx.action.asToolbar
import org.sketchfx.infra.Icons
import org.sketchfx.shape.Shape

class ShapePropertyPane(private val viewModel: CanvasViewModel) : VBox() {

    private val sizeBinding = Bindings.size(viewModel.selection.items())
    private val alignmentBinding = Bindings.lessThan(sizeBinding, 2)

    private val alignmentActions = listOf(
        alignAction(Icons.ALIGN_LEFT){ _ -> viewModel.alignSelection(Alignment.LEFT) },
        alignAction(Icons.ALIGN_CENTER){},
        alignAction(Icons.ALIGN_RIGHT){_ -> viewModel.alignSelection(Alignment.RIGHT)},
        alignAction(Icons.ALIGN_TOP){},
        alignAction(Icons.ALIGN_MIDDLE){},
        alignAction(Icons.ALIGN_BOTTOM){}
    )

    init {
        styleClass.addAll("shape-property-pane", "small")
        children.setAll(
            alignmentActions.asToolbar()
        )
    }

    private fun alignAction( icon: Icons, align: (List<Shape>) -> Unit): Action {
        return Action.of(buildGraphic = { icon.graphic() }).apply {
            disabledProperty.bind(alignmentBinding)
            action = {
                align(viewModel.selection.items())
            }
        }
    }

}