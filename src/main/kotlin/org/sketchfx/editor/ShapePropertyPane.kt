package org.sketchfx.editor

import javafx.beans.InvalidationListener
import javafx.scene.layout.VBox
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.action.Action
import org.sketchfx.fx.action.asToolbar
import org.sketchfx.infra.Icons

class ShapePropertyPane( private val viewModel: CanvasViewModel): VBox() {


    private val alignmentActions = listOf(
        Action.of( buildGraphic = { Icons.ALIGN_LEFT.graphic()}).apply {},
        Action.of( buildGraphic = { Icons.ALIGN_CENTER.graphic()}).apply {},
        Action.of( buildGraphic = { Icons.ALIGN_RIGHT.graphic()}).apply {},
        Action.of( buildGraphic = { Icons.ALIGN_TOP.graphic()}).apply {},
        Action.of( buildGraphic = { Icons.ALIGN_MIDDLE.graphic()}).apply {},
        Action.of( buildGraphic = { Icons.ALIGN_BOTTOM.graphic()}).apply {},
    )

    init {
        styleClass.addAll("shape-property-pane", "small")


        viewModel.selection.items().addListener( InvalidationListener{ _ ->
            validate()
        })
        validate()

        children.setAll(
            alignmentActions.asToolbar()
        )


    }

    private fun validate() {
        alignmentActions.forEach{it.disabled = viewModel.selection.items().size < 2 }
    }


}